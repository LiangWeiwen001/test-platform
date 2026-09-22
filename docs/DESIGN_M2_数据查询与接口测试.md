# 设计文档：M2 数据查询与接口测试（v0.1 草案）

| 项目 | 内容 |
|:-----|:-----|
| 文档版本 | v0.1（草案，待评审） |
| 编写日期 | 2026-09-21 |
| 文档状态 | 待评审（阶段 1 设计交付物） |
| 适用范围 | M2 迭代：日志查询（L）/ 数据库查询（DB）/ Redis 查询（R）/ 接口测试（API） |
| 关联文档 | `PRD.md`（2.2~2.6 节）/ `DESIGN_系统架构.md` / `DESIGN_数据模型.md` / `DESIGN_接口设计.md` |

---

## 1. 设计目标与约束

### 1.1 目标
1. 在 M1 骨架（认证/RBAC/环境/用例库）之上扩展 4 个 P0 模块，不动既有模块。
2. 数据源连接（DB/Redis/日志源）统一挂到环境（`td_environment` 扩展），配合 ENV-04 全局切换。
3. 全部查询默认只读；写操作（SQL 写/Redis 写）受权限码控制 + 二次确认 + 审计（S-03）。
4. 接口测试复用 M1 `tc_case` 用例库（TC-07 联动），新增集合/目录结构与执行引擎。

### 1.2 约束（来自 M1 ADR 与 PRD）
| 约束 | 内容 |
|:-----|:-----|
| 后端 | Java 21 / Spring Boot 3.2+ / MyBatis-Plus（沿用 M1） |
| 新增依赖 | mysql-connector-j（已有）/ PostgreSQL 驱动（pgjdbc）/ Lettuce（Spring Data Redis）/ spring-boot-starter-data-redis |
| 加密 | 数据源密码 AES-GCM 加密存储（密钥 `DS_SECRET` 环境变量，≥32 字符） |
| 接口风格 | 沿用 `/api/v1` 前缀 + 统一响应 `{code,message,data}` + `@RequirePermission` |
| 默认只读 | DB 数据源只读标记、Redis 默认只读，写操作需 `xxx:write` 权限码 |
| 环境关联 | 数据源/日志源/Redis 连接配置挂 `td_environment.id`，环境切换联动（ENV-04） |

### 1.3 范围（P0 首轮，P1/P2 留扩展位）
| 模块 | P0 落地 | P1/P2 扩展位 |
|:-----|:--------|:-------------|
| 日志查询 | 文件日志源接入、关键字检索、时间/级别过滤、详情查看 | L-05 上下文（traceId）、L-06 保存查询、L-07 导出、L-08 告警、ES/Loki 适配 |
| 数据库查询 | MySQL 数据源管理、SQL 执行（只读控制）、结果表格展示、执行记录 | PG/Oracle/SQLServer 驱动、导出、SQL 收藏、影响预览/审批 |
| Redis 查询 | 单机连接管理、Key 浏览/搜索、类型识别、数据查看（只读） | 集群/Sentinel、写操作、命令执行、审计 |
| 接口测试 | 集合-目录-用例三层、请求配置、认证（Basic/Bearer）、断言（状态码/包含）、执行与历史 | 变量体系、OpenAPI 导入、前后置脚本、报告、CI 触发/门禁 |

---

## 2. 数据模型（新增 6 张表，沿用 DDL 规范）

> DDL 规范同 M1：InnoDB/utf8mb4/逻辑删除 deleted/uk_ 含 deleted/无外键。前缀：`td_`（测试环境类）/ `api_`（接口测试类）。

### 2.1 `td_datasource` — 数据库数据源
| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| id | bigint | 主键 |
| env_id | bigint | 关联环境（外键意图 → td_environment.id） |
| ds_name | varchar(50) | 数据源名称 |
| db_type | varchar(20) | mysql / postgresql / oracle / sqlserver |
| host | varchar(100) | 主机 |
| port | int | 端口 |
| database_name | varchar(100) | 库名 |
| username | varchar(100) | 账号 |
| password_enc | varchar(255) | 密码（AES-GCM 加密） |
| readonly | tinyint | 1=只读（禁止写 SQL）/ 0=可写 |
| connect_timeout | int | 连接超时（秒，默认 5） |
| status | tinyint | 1启用 / 0停用 |
| created_by / created_at / updated_at / deleted | - | 公共字段 |

唯一索引 `uk_ds_env_name(env_id, ds_name, deleted)`。

### 2.2 `td_log_source` — 日志源
| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| id / env_id | bigint | 主键 / 关联环境 |
| source_name | varchar(50) | 日志源名称 |
| source_type | varchar(20) | file（文件）/ es（预留）/ loki（预留） |
| path_pattern | varchar(255) | 文件路径或通配符（如 `/var/log/app/*.log`） |
| host | varchar(100) | 文件所在主机（预留，本机为 localhost） |
| charset | varchar(20) | 编码（默认 utf-8） |
| status | tinyint | 1启用 / 0停用 |
| 公共字段 | - | 同上 |

唯一索引 `uk_ls_env_name(env_id, source_name, deleted)`。

### 2.3 `td_redis_conn` — Redis 连接
| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| id / env_id | bigint | 主键 / 关联环境 |
| conn_name | varchar(50) | 连接名称 |
| mode | varchar(20) | standalone（单机）/ cluster（预留）/ sentinel（预留） |
| host | varchar(100) | 主机 |
| port | int | 端口 |
| password_enc | varchar(255) | 密码（加密） |
| db_index | int | 默认 db（0-15） |
| timeout | int | 超时（秒，默认 3） |
| readonly | tinyint | 1=只读（禁止写命令）/ 0=可写 |
| status | tinyint | 1启用 / 0停用 |
| 公共字段 | - | 同上 |

唯一索引 `uk_rc_env_name(env_id, conn_name, deleted)`。

### 2.4 `api_collection` — 接口测试集合（三层结构：集合 → 目录 → 用例）
| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| id | bigint | 主键 |
| parent_id | bigint | 父级 ID（0=顶层集合；目录的父=集合，用例的父=目录） |
| node_type | char(1) | C=集合 / D=目录 / C2=用例（或用 case_id 关联 tc_case） |
| name | varchar(100) | 节点名称 |
| case_id | bigint | 用例节点关联 tc_case.id（复用 M1 用例库） |
| sort | int | 同级排序 |
| created_by / created_at / updated_at / deleted | - | 公共字段 |

### 2.5 `api_exec_record` — 接口执行记录
| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| id | bigint | 主键 |
| case_id | bigint | 关联 tc_case.id |
| collection_id | bigint | 关联集合（批量执行用） |
| env_id | bigint | 执行环境 |
| request_snapshot | json | 实际请求（method/url/headers/body） |
| response_snapshot | json | 响应（status/headers/body） |
| assertions | json | 断言结果 [{name,pass,detail}] |
| success | tinyint | 1通过 / 0失败 |
| cost_ms | int | 耗时 |
| error_msg | varchar(500) | 失败原因 |
| executed_by / executed_at | - | 执行人/时间（executed_at datetime） |

### 2.6 `td_exec_log` — 数据查询执行记录（DB SQL + Redis 命令统一审计）
| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| id | bigint | 主键 |
| target_type | varchar(20) | db / redis |
| target_id | bigint | 数据源/Redis 连接 ID |
| operation | varchar(20) | query（查询）/ execute（写） |
| content | text | SQL 语句或 Redis 命令（写操作脱敏） |
| success | tinyint | 1成功 / 0失败 |
| error_msg | varchar(500) | 失败原因 |
| cost_ms | int | 耗时 |
| executed_by / created_at | - | 执行人/时间 |

> 审计表只增不改（无 updated_at / deleted），PRD 3.2 安全需求。

---

## 3. 接口设计（REST，沿用 /api/v1 + @RequirePermission）

### 3.1 数据库查询（模块权限前缀 `query:db`）
| 方法 | 路径 | 说明 | 权限码 |
|:-----|:-----|:-----|:-------|
| GET | `/api/v1/query/db/datasources` | 数据源分页列表 | `query:db:list` |
| POST | `/api/v1/query/db/datasources` | 新建数据源（密码加密存储） | `query:db:add` |
| PUT | `/api/v1/query/db/datasources/{id}` | 编辑数据源 | `query:db:edit` |
| DELETE | `/api/v1/query/db/datasources/{id}` | 删除数据源 | `query:db:delete` |
| POST | `/api/v1/query/db/datasources/{id}/test` | 连接测试 | `query:db:list` |
| POST | `/api/v1/query/db/datasources/{id}/execute` | 执行 SQL（body: {sql}；只读源/写语句按权限拦截） | `query:db:query`（查询）/ `query:db:write`（写） |
| GET | `/api/v1/query/db/exec-logs` | 执行记录分页 | `query:db:list` |

### 3.2 日志查询（模块权限前缀 `query:log`）
| 方法 | 路径 | 说明 | 权限码 |
|:-----|:-----|:-----|:-------|
| GET | `/api/v1/query/log/sources` | 日志源列表 | `query:log:list` |
| POST | `/api/v1/query/log/sources` | 新建日志源 | `query:log:add` |
| PUT / DELETE | `/api/v1/query/log/sources/{id}` | 编辑 / 删除 | `query:log:edit/delete` |
| GET | `/api/v1/query/log/search` | 检索（params: sourceId, keyword, level, startTime, endTime, page, size） | `query:log:query` |
| GET | `/api/v1/query/log/{id}/context` | 上下文（P1 扩展位，本版不做） | - |

### 3.3 Redis 查询（模块权限前缀 `query:redis`）
| 方法 | 路径 | 说明 | 权限码 |
|:-----|:-----|:-----|:-------|
| GET | `/api/v1/query/redis/conns` | 连接列表 | `query:redis:list` |
| POST | `/api/v1/query/redis/conns` | 新建连接 | `query:redis:add` |
| PUT / DELETE | `/api/v1/query/redis/conns/{id}` | 编辑 / 删除 | `query:redis:edit/delete` |
| POST | `/api/v1/query/redis/conns/{id}/test` | 连接测试 | `query:redis:list` |
| GET | `/api/v1/query/redis/conns/{id}/keys` | Key 列表（params: dbIndex, pattern, cursor, size） | `query:redis:query` |
| GET | `/api/v1/query/redis/conns/{id}/keys/{key}` | Key 值查看（类型识别） | `query:redis:query` |
| POST | `/api/v1/query/redis/conns/{id}/command` | 执行命令（默认仅只读命令；写命令需 `query:redis:write`） | `query:redis:query` / `query:redis:write` |

### 3.4 接口测试（模块权限前缀 `api`）
| 方法 | 路径 | 说明 | 权限码 |
|:-----|:-----|:-----|:-------|
| GET | `/api/v1/api/collections` | 集合树（三层） | `api:case:list` |
| POST | `/api/v1/api/collections` | 新建节点（集合/目录） | `api:case:add` |
| PUT / DELETE | `/api/v1/api/collections/{id}` | 编辑 / 删除节点 | `api:case:edit/delete` |
| GET | `/api/v1/api/cases/{caseId}` | 用例详情（复用 tc_case） | `api:case:list` |
| POST | `/api/v1/api/cases/{caseId}/execute` | 执行单条用例（body: {envId}） | `api:case:execute` |
| POST | `/api/v1/api/collections/{id}/execute` | 执行集合（批量） | `api:case:execute` |
| GET | `/api/v1/api/exec-records` | 执行记录分页 | `api:case:list` |
| GET | `/api/v1/api/exec-records/{id}` | 记录详情（含请求/响应/断言） | `api:case:list` |

### 3.5 权限码种子（追加到 sys_permission，admin 全量、test 查询+执行、dev 只读、ops 环境维护）
| 权限码 | admin | test | dev | ops |
|:-------|:-----:|:----:|:---:|:---:|
| `query:db:*`（list/add/edit/delete/query/write） | ✅ | query/write | query | - |
| `query:log:*`（list/add/edit/delete/query） | ✅ | query | query | add/edit |
| `query:redis:*`（list/add/edit/delete/query/write） | ✅ | query | query | add/edit |
| `api:case:*`（list/add/edit/delete/execute） | ✅ | 全 | list | - |

---

## 4. 架构要点

### 4.1 数据库查询引擎
- 动态连接：每请求按数据源 ID 用 `DriverManager` 或短生命周期 `HikariDataSource` 建连（不缓存长连接，避免连接泄漏；可用简单 LRU 缓存，容量 10）。
- 只读控制：`datasource.readonly=1` 或 SQL 首词为 INSERT/UPDATE/DELETE/DDL 且无 `query:db:write` 权限 → 拦截返回 40300。
- 查询超时：`Statement.setQueryTimeout(seconds)`（默认 30s）。
- 多语句：分号拆分逐条执行，仅返回最后一条结果集（首版简化）。
- 结果：`{columns:[...], rows:[[...]], rowCount, costMs}`，前端表格化。

### 4.2 Redis 查询引擎
- 客户端：Spring Data Redis + Lettuce（`RedisConnectionFactory` 动态创建，Lettuce 单机模式）。
- Key 浏览：`SCAN cursor MATCH pattern COUNT 100` 分页（避免 KEYS 阻塞）。
- 类型识别：`TYPE key` → 按类型读取（String→GET / Hash→HGETALL / List→LRANGE 0 -1 / Set→SMEMBERS / ZSet→ZRANGE WITHSCORES，大 key 首版全量+前端限制展示行数）。
- 写命令拦截：默认只放行只读命令白名单（GET/HGET/.../SCAN/TYPE/TTL）；写命令需 `query:redis:write` 权限。

### 4.3 日志查询引擎（文件源）
- 首版支持 `source_type=file`：按 `path_pattern` 用 `Files.walk` + `Files.readAllLines` 读取（或 `tail` 式增量读），内存过滤关键字/级别/时间。
- 行格式解析：正则提取时间戳 + 级别（DEBUG/INFO/WARN/ERROR）+ 内容；`ERROR` 级别行高亮。
- 分页：按行号游标分页（`fromLine` / `size`），返回 `{lines:[{lineNo, timestamp, level, content}], total, hasMore}`。
- 性能注意：首版适用于中小日志文件（<100MB）；大文件/ES/Loki 在 P1 扩展（`source_type=es/loki` 预留）。

### 4.4 接口测试执行引擎
- 请求构建：从 `tc_case.request_config`（JSON：method/url/headers/query/body）构建 `HttpURLConnection` 或 `java.net.http.HttpClient`（超时 15s，跟随重定向 NORMAL）。
- 认证：请求头直接支持 `Authorization: Bearer/Basic`；Cookie 从响应 Set-Cookie 解析存入执行上下文（P1 做 Token 自动获取）。
- 断言：从 `tc_case.expected_result`（JSON：`{statusCode?, bodyContains?, jsonPathEquals?}`）逐条执行，全部通过=success。
- 执行记录：成功/失败、请求/响应快照、断言明细、耗时写入 `api_exec_record`。
- 环境：`envId` 解析 BaseURL 替换用例 URL 中的 `{{baseUrl}}` 占位符（ENV-04 联动）。

### 4.5 安全
- 数据源/Redis 密码：AES-GCM 加密（密钥 `DS_SECRET` 环境变量，默认 dev 值仅限本地），返回给前端一律脱敏（`password_enc` 不回传）。
- 全部执行操作写审计日志（`td_exec_log` + 复用 `sys_operation_log` 切面）。
- SQL 注入防护：平台自身接口参数校验；SQL 语句本身不校验（平台定位就是执行 SQL，靠权限+只读控制兜底）。

---

## 5. 前端页面规划

| 模块 | 页面 | 要点 |
|:-----|:-----|:-----|
| 数据库查询 | `views/query/db/DatasourceList.vue`（数据源 CRUD）+ `views/query/db/SqlConsole.vue`（SQL 编辑器：el-input textarea + 执行 + 结果 el-table + 耗时/行数 + 执行记录抽屉） | 只读数据源禁用写按钮 |
| 日志查询 | `views/query/log/SourceList.vue`（日志源 CRUD）+ `views/query/log/SearchView.vue`（检索条件栏 + 结果 el-table 行号/时间/级别 tag/内容 + 详情抽屉） | 级别彩色 tag |
| Redis 查询 | `views/query/redis/ConnList.vue`（连接 CRUD）+ `views/query/redis/KeyBrowser.vue`（db 选择 + 搜索框 + Key 列表 + 值查看按类型渲染） | 只读连接禁用写命令 |
| 接口测试 | `views/api/CollectionTree.vue`（左侧集合树：集合/目录/用例，右键新建/编辑/删除/执行）+ `views/api/CaseEditor.vue`（右侧用例编辑：请求配置 tab + 断言 tab + 执行按钮）+ `views/api/ExecHistory.vue`（执行记录列表+详情） | 复用 M1 用例库 |

菜单挂载：左侧新增「数据查询」分组（子菜单：数据库/日志/Redis）+「接口测试」顶级菜单；权限码控制显示（同 M1 动态菜单机制）。

---

## 6. 开发任务拆分（TASK-M2-01 ~ M2-12，P0 首轮）

| 任务 | 内容 | 验收标准 |
|:-----|:-----|:---------|
| M2-01 | 后端：td_datasource/td_log_source/td_redis_conn 表 + 实体/Mapper + 权限种子追加 | 建表 + 种子可查 |
| M2-02 | 后端：DB 数据源 CRUD + 连接测试 + AES 加解密 | curl 通过 |
| M2-03 | 后端：SQL 执行引擎（只读控制/超时/结果集） | curl 通过 |
| M2-04 | 前端：数据源管理页 + SQL 控制台 | 浏览器可用 |
| M2-05 | 后端：Redis 连接 CRUD + Lettuce 动态连接 + Key 浏览/值查看 | curl 通过 |
| M2-06 | 前端：Redis 连接页 + Key 浏览器 | 浏览器可用 |
| M2-07 | 后端：文件日志源 CRUD + 检索接口 | curl 通过 |
| M2-08 | 前端：日志源管理页 + 检索视图 | 浏览器可用 |
| M2-09 | 后端：api_collection/api_exec_record 表 + 集合树 CRUD + 执行引擎（单条/集合） | curl 通过 |
| M2-10 | 前端：集合树 + 用例编辑 + 执行历史 | 浏览器可用 |
| M2-11 | 菜单/权限/环境联动收尾（数据查询分组菜单 + 环境切换联动） | 浏览器可用 |
| M2-12 | 端到端验证 + README 更新 + tag v0.2 + push | 全链路跑通 |

---

*文档结束（草案，待评审）*