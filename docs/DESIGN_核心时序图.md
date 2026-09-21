# 核心时序图（M1 基础平台）

| 项目 | 内容 |
|:-----|:-----|
| 文档版本 | v1.0 |
| 编写日期 | 2026-09-21 |
| 文档状态 | 已评审（阶段 1 设计交付物） |
| 适用范围 | M1 基础平台（REQ-001 ~ REQ-008） |
| 关联文档 | `DESIGN_系统架构.md` / `DESIGN_接口设计.md` / `DESIGN_数据模型.md` |

> 本文件覆盖 M1 五条核心业务流程：登录认证、RBAC 权限校验、用例管理、环境健康自检、全局搜索。所有时序图均为 Mermaid `sequenceDiagram`，可直接渲染。

---

## 1. 登录认证（JWT 签发 + 前端存储 + 路由守卫）

> 对应 REQ-002。覆盖：登录接口校验 → 双 token 签发 → 前端存储 → 路由守卫放行。

```mermaid
sequenceDiagram
    participant U as 用户
    participant V as 前端 SPA（登录页）
    participant G as 路由守卫 guard.js
    participant S as Pinia user store
    participant C as AuthController
    participant J as JwtUtil
    participant D as sys_user 表

    U->>V: 输入账号密码，点击登录
    V->>C: POST /api/v1/auth/login {username, password}
    C->>D: 按 username 查询用户（deleted=0）
    D-->>C: 用户记录（含 BCrypt 密码）
    C->>C: BCrypt 校验密码 + 校验 status=1
    alt 校验通过
        C->>J: 生成 access token（2h）+ refresh token（7d）
        J-->>C: 双 token（载荷含 userId/username/roles/perms）
        C->>D: 更新 last_login_time / last_login_ip
        C-->>V: 200 {code:0, data:{accessToken, refreshToken, user}}
        V->>S: 存储 token 到 localStorage + Pinia
        S->>S: 记录用户信息与权限码集合
        S->>G: 触发动态路由注册（按权限码过滤菜单）
        G-->>U: 跳转工作台 /dashboard
    else 校验失败
        C-->>V: 40000（账号或密码错误）/ 40300（账号被禁用）
        V-->>U: 表单错误提示
    end
```

**路由守卫补充流程**（访问受保护路由时的校验）：

```mermaid
sequenceDiagram
    participant U as 用户
    participant G as 路由守卫 guard.js
    participant S as Pinia user store
    participant C as AuthController
    participant V as 目标页面

    U->>G: 访问受保护路由（如 /env）
    G->>S: 检查 access token 是否存在
    alt token 不存在
        G-->>U: 重定向 /login?redirect=/env
    else token 存在
        G->>S: 用户信息与权限是否已加载
        alt 未加载
            G->>C: GET /api/v1/auth/me（Bearer token）
            C-->>G: 用户信息 + 权限码集合
            G->>S: 缓存用户态 + 注册动态路由
        end
        G->>G: 校验目标路由所需权限码（如 env:env:list）
        alt 有权限
            G-->>V: 放行进入页面
        else 无权限
            G-->>U: 跳转 403 页
        end
    end
```

---

## 2. RBAC 权限校验（后端拦截器 + 前端按钮级控制）

> 对应 REQ-003。覆盖：前端 v-perm 按钮级控制 + 后端 JWT 过滤器 + 权限拦截器双重防线。

```mermaid
sequenceDiagram
    participant U as 用户
    participant V as 前端页面（v-perm 指令）
    participant F as JwtAuthFilter
    participant I as PermissionInterceptor
    participant C as Controller（@RequirePermission）
    participant S as Service
    participant D as 数据库

    Note over V: 前端按钮级控制（体验层）
    V->>V: 渲染按钮时执行 v-perm="system:user:add"
    V->>V: 检查 Pinia 权限码集合是否包含该码
    alt 无权限码
        V-->>U: 按钮隐藏/禁用（不渲染）
    end

    Note over F: 后端接口鉴权（安全层，最终防线）
    U->>F: 请求 POST /api/v1/system/users（带 Authorization: Bearer token）
    F->>F: 解析并校验 JWT 签名与有效期
    alt token 无效/过期
        F-->>U: 401 {code:40100} 未认证
    else token 有效
        F->>F: 解析载荷 → 注入 LoginUser 上下文（userId/roles/perms）
        F->>I: 进入权限拦截器
        I->>I: 读取接口注解 @RequirePermission("system:user:add")
        I->>I: 比对 LoginUser.perms 是否包含该权限码
        alt 权限不足
            I-->>U: 403 {code:40300} 无权限
        else 有权限
            I->>C: 放行调用 Controller
            C->>S: 执行业务逻辑（含 AOP 审计日志切面）
            S->>D: 数据读写
            D-->>S: 结果
            S-->>C: 业务数据
            C-->>U: 200 {code:0, data}
        end
    end
```

---

## 3. 用例管理流程（创建 / 编辑 / 执行 / 结果回写）

> 对应 REQ-006。创建与编辑为 M1 落地范围；执行与结果回写由 M2+ 接口测试模块联动实现（M1 仅预留执行状态字段），图中以注释标注扩展位。

```mermaid
sequenceDiagram
    participant U as 测试人员
    participant V as 用例管理页面
    participant C as CaseController
    participant S as CaseService
    participant T as 标签表 tc_case_tag
    participant D as 用例表 tc_case

    Note over U,D: 创建用例（M1）
    U->>V: 填写用例（名称/类型/优先级/请求配置/断言/标签）
    V->>C: POST /api/v1/cases（Bearer token）
    C->>S: 保存用例（校验 case_type/case_level）
    S->>T: 校验 tag_ids 是否存在
    T-->>S: 标签校验结果
    S->>D: INSERT tc_case（version=v1.0, status=1）
    D-->>S: 用例 ID
    S-->>C: 用例详情
    C-->>V: 200 {code:0, data: 用例详情}
    V-->>U: 列表刷新，提示"创建成功"

    Note over U,D: 编辑用例（M1）
    U->>V: 修改用例并保存
    V->>C: PUT /api/v1/cases/{id}
    C->>S: 更新用例（version 递增 v1.0→v1.1）
    S->>D: UPDATE tc_case（updated_by=当前用户）
    D-->>S: 更新成功
    S-->>C: 用例详情
    C-->>V: 200 {code:0, data: 用例详情}
    V-->>U: 提示"保存成功"

    Note over U,D: 执行用例（M2+ 接口测试模块联动，M1 仅预留执行状态字段）
    U->>V: 点击"执行"
    V->>C: POST /api/v1/cases/{id}/execute（M2+ 提供）
    C->>S: 调用接口测试执行引擎（M2+ 实现）
    S-->>C: 执行结果（通过/失败/耗时/断言明细）
    C-->>V: 200 执行结果
    V->>C: 结果回写（M2+ 提供）
    C->>S: 更新用例最近执行状态
    S->>D: UPDATE tc_case（执行状态字段）
    V-->>U: 展示执行结果与报告入口
```

---

## 4. 环境健康自检（定时 + 手动触发）

> 对应 REQ-005（ENV-07 数据源健康自检）。M1 实现：手动触发 + 页面加载时自动触发；定时任务（Spring @Scheduled）为 M1 可选增强，图中一并给出。

```mermaid
sequenceDiagram
    participant T as 定时任务（@Scheduled，可选）
    participant U as 运维人员
    participant V as 环境管理页面
    participant C as EnvironmentController
    participant S as EnvironmentHealthService
    participant E as 被测环境（base_url）
    participant D as td_environment_health 表

    Note over T,D: 定时自检（可选，如每 5 分钟扫描启用环境）
    T->>S: 扫描 status=1 的环境列表
    S->>E: HTTP GET 探测 base_url（超时 3s）
    alt 探测成功（2xx/3xx）
        E-->>S: 响应
        S->>D: INSERT 健康记录（status=UP, latency_ms, checked_by=0）
    else 探测失败/超时
        E-->>S: 无响应/错误
        S->>D: INSERT 健康记录（status=DOWN, error_msg）
    end

    Note over U,D: 手动触发（M1 主路径）
    U->>V: 点击"健康自检"按钮
    V->>C: POST /api/v1/envs/{id}/health-check
    C->>S: 执行单环境探测
    S->>E: HTTP GET 探测 base_url（超时 3s）
    alt 探测成功
        E-->>S: 响应
        S->>D: INSERT 健康记录（status=UP, checked_by=当前用户）
        S-->>C: {status:UP, latencyMs, errorMsg:null}
    else 探测失败
        E-->>S: 无响应/错误
        S->>D: INSERT 健康记录（status=DOWN, error_msg）
        S-->>C: {status:DOWN, latencyMs, errorMsg}
    end
    C-->>V: 200 检测结果
    V-->>U: 展示检测结果与诊断提示（红色 DOWN 标记）
    U->>V: 点击"健康记录"查看历史
    V->>C: GET /api/v1/envs/{id}/health-records?page=1&size=10
    C->>S: 分页查询健康记录
    S->>D: SELECT（按 checked_at 倒序）
    D-->>S: 记录列表
    S-->>C: 分页数据
    C-->>V: 200 {page, size, total, records}
    V-->>U: 展示健康历史列表
```

---

## 5. 全局搜索（跨模块聚合）

> 对应 REQ-008（PRD S-08）。跨用例 / 环境 / 用户三类资源聚合搜索，按类型分组返回。

```mermaid
sequenceDiagram
    participant U as 用户
    participant V as 顶栏全局搜索框
    participant C as SearchController
    participant S as SearchService
    participant TC as tc_case 表
    participant ENV as td_environment 表
    participant USR as sys_user 表

    U->>V: 输入关键字（防抖 300ms 后触发）
    V->>C: GET /api/v1/search?keyword=登录
    C->>S: 聚合查询（并行执行三类检索）
    par 并行检索
        S->>TC: case_name/description LIKE '%登录%'（deleted=0, 限10条）
        TC-->>S: 用例结果集
        S->>ENV: env_name/env_code/description LIKE '%登录%'（deleted=0, 限10条）
        ENV-->>S: 环境结果集
        S->>USR: username/nickname LIKE '%登录%'（deleted=0, 限10条）
        USR-->>S: 用户结果集
    end
    S->>S: 按类型分组 + 标注匹配字段
    S-->>C: {cases:[], envs:[], users:[]}
    C-->>V: 200 {code:0, data:{keyword, cases, envs, users}}
    V-->>U: 分组展示搜索结果
    U->>V: 点击某条结果
    V-->>U: 跳转对应详情页（用例详情/环境详情/用户详情）
```

---

*文档结束*