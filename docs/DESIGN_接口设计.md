# 接口设计（M1 基础平台）

| 项目 | 内容 |
|:-----|:-----|
| 文档版本 | v1.0 |
| 编写日期 | 2026-09-21 |
| 文档状态 | 已评审（阶段 1 设计交付物） |
| 适用范围 | M1 基础平台（REQ-001 ~ REQ-008） |
| 关联文档 | `DESIGN_系统架构.md` / `DESIGN_数据模型.md` / `ADR/ADR-001-技术栈选型.md` |

---

## 1. 通用约定

### 1.1 统一响应体

所有接口返回 `application/json`，结构固定为：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| code | int | 业务状态码，0=成功，非 0=失败（见 1.2 错误码表） |
| message | string | 提示信息（成功为 "success"，失败为具体原因） |
| data | object/array/null | 业务数据；失败时为 null |

### 1.2 错误码表

| code | 含义 | HTTP 状态 | 说明 |
|:-----|:-----|:----------|:-----|
| 0 | 成功 | 200 | 操作成功 |
| 40000 | 参数错误 | 400 | 请求参数缺失或格式非法（含 Bean Validation 失败） |
| 40100 | 未认证 | 401 | token 缺失 / 无效 / 已过期 |
| 40101 | token 过期 | 401 | access token 过期，需用 refresh token 刷新 |
| 40102 | 登录失败 | 401 | 账号或密码错误 / 账号已被禁用（对外统一提示，不区分原因，防账号枚举） |
| 40300 | 无权限 | 403 | 当前用户缺少接口所需权限码 |
| 40400 | 资源不存在 | 404 | 目标资源不存在或已删除 |
| 40900 | 数据冲突 | 409 | 唯一性冲突（用户名 / 角色编码 / 环境标识 / 标签名重复） |
| 50000 | 系统错误 | 500 | 服务器内部异常（全局异常处理器兜底） |

> HTTP 状态码与业务 code 并存：HTTP 状态用于网关/代理层识别，业务 code 用于前端逻辑分支。

### 1.3 分页约定

- 请求参数：`page`（页码，从 1 开始，默认 1）、`size`（每页条数，默认 10，最大 100）。
- 响应 `data` 结构固定为：

```json
{
  "page": 1,
  "size": 10,
  "total": 42,
  "records": []
}
```

| 字段 | 类型 | 说明 |
|:-----|:-----|:-----|
| page | int | 当前页码 |
| size | int | 每页条数 |
| total | long | 总记录数 |
| records | array | 当前页数据列表 |

### 1.4 JWT 认证约定

| 项 | 约定 |
|:---|:-----|
| 请求头 | `Authorization: Bearer <access_token>` |
| access token | 有效期 **2 小时**，载荷含：`userId`、`username`、`roles`（角色编码数组）、`perms`（权限码数组） |
| refresh token | 有效期 **7 天**，仅用于调用刷新接口换取新 access token |
| 刷新流程 | access token 过期 → 前端带 refresh token 调 `POST /api/v1/auth/refresh` → 返回新 access token（+ 新 refresh token 滚动续期）→ 重放原请求 |
| 登出 | 前端清除本地 token（无状态 JWT，服务端不维护黑名单；M1 简化处理） |
| 白名单 | `POST /api/v1/auth/login`、`POST /api/v1/auth/refresh`、`GET /api/health` 无需认证 |
| 密钥 | `JWT_SECRET` 环境变量注入（≥32 字符），见 `环境搭建说明.md` |

### 1.5 API 前缀与版本

- 统一前缀：`/api/v1`（健康检查 `/api/health` 除外）。
- 权限校验：除白名单外，所有接口先过 JWT 认证，再按 `@RequirePermission("权限码")` 注解做 RBAC 校验（见 `DESIGN_核心时序图.md` 时序图②）。

---

## 2. M1 API 清单

> 权限码列：`-` 表示无需权限码（仅需登录或匿名）；需求ID列对应 `REQ_需求排期表.md` 的 REQ-001 ~ REQ-008。

### 2.1 认证模块（REQ-002）

| 方法 | 路径 | 说明 | 权限码 | 需求ID |
|:-----|:-----|:-----|:-------|:-------|
| POST | `/api/v1/auth/login` | 登录，签发 access + refresh token | 匿名 | REQ-002 |
| POST | `/api/v1/auth/logout` | 登出（前端清除 token） | 登录即可 | REQ-002 |
| POST | `/api/v1/auth/refresh` | 刷新 access token（refresh token 换新） | 匿名（凭 refresh token） | REQ-002 |
| GET | `/api/v1/auth/me` | 当前登录用户信息（含角色与权限码集合） | 登录即可 | REQ-002 |

### 2.2 用户管理（REQ-003）

| 方法 | 路径 | 说明 | 权限码 | 需求ID |
|:-----|:-----|:-----|:-------|:-------|
| GET | `/api/v1/system/users` | 用户分页列表（支持关键字/状态筛选） | `system:user:list` | REQ-003 |
| POST | `/api/v1/system/users` | 新建用户（含角色绑定） | `system:user:add` | REQ-003 |
| PUT | `/api/v1/system/users/{id}` | 编辑用户（含角色调整） | `system:user:edit` | REQ-003 |
| DELETE | `/api/v1/system/users/{id}` | 删除用户（逻辑删除 + 清理角色绑定） | `system:user:delete` | REQ-003 |
| PUT | `/api/v1/system/users/{id}/status` | 启用/禁用用户 | `system:user:edit` | REQ-003 |
| PUT | `/api/v1/system/users/{id}/password` | 重置密码（管理员指定新密码） | `system:user:resetPwd` | REQ-003 |

### 2.3 角色与权限（REQ-003）

| 方法 | 路径 | 说明 | 权限码 | 需求ID |
|:-----|:-----|:-----|:-------|:-------|
| GET | `/api/v1/system/roles` | 角色列表（含权限码集合） | `system:role:list` | REQ-003 |
| POST | `/api/v1/system/roles` | 新建角色 | `system:role:add` | REQ-003 |
| PUT | `/api/v1/system/roles/{id}` | 编辑角色 | `system:role:edit` | REQ-003 |
| DELETE | `/api/v1/system/roles/{id}` | 删除角色（清理权限绑定与用户绑定） | `system:role:delete` | REQ-003 |
| GET | `/api/v1/system/roles/{id}/permissions` | 查询角色已分配的权限 ID 列表 | `system:role:list` | REQ-003 |
| PUT | `/api/v1/system/roles/{id}/permissions` | 分配角色权限（全量覆盖） | `system:role:assignPerm` | REQ-003 |
| GET | `/api/v1/system/permissions/tree` | 权限树（菜单+按钮，供角色分配勾选） | `system:role:list` | REQ-003 |

### 2.4 操作日志（REQ-003 / PRD S-03）

| 方法 | 路径 | 说明 | 权限码 | 需求ID |
|:-----|:-----|:-----|:-------|:-------|
| GET | `/api/v1/system/logs` | 操作日志分页（支持模块/结果/时间范围筛选） | `system:log:list` | REQ-003 |

### 2.5 环境管理（REQ-005）

| 方法 | 路径 | 说明 | 权限码 | 需求ID |
|:-----|:-----|:-----|:-------|:-------|
| GET | `/api/v1/envs` | 环境分页列表（支持关键字/状态筛选） | `env:env:list` | REQ-005 |
| GET | `/api/v1/envs/all` | 全部启用环境（顶栏环境切换器下拉用） | `env:env:list` | REQ-005 |
| POST | `/api/v1/envs` | 新建环境 | `env:env:add` | REQ-005 |
| PUT | `/api/v1/envs/{id}` | 编辑环境 | `env:env:edit` | REQ-005 |
| DELETE | `/api/v1/envs/{id}` | 删除环境（逻辑删除 + 保留健康记录） | `env:env:delete` | REQ-005 |
| PUT | `/api/v1/envs/{id}/status` | 启用/停用环境 | `env:env:edit` | REQ-005 |
| POST | `/api/v1/envs/{id}/health-check` | 手动触发健康自检（HTTP 探测 base_url） | `env:env:health` | REQ-005 |
| GET | `/api/v1/envs/{id}/health-records` | 健康检查记录分页 | `env:env:health` | REQ-005 |

### 2.6 用例管理（REQ-006）

| 方法 | 路径 | 说明 | 权限码 | 需求ID |
|:-----|:-----|:-----|:-------|:-------|
| GET | `/api/v1/cases` | 用例分页列表（支持类型/优先级/标签/关键字筛选） | `case:case:list` | REQ-006 |
| GET | `/api/v1/cases/{id}` | 用例详情（含请求配置与断言） | `case:case:list` | REQ-006 |
| POST | `/api/v1/cases` | 新建用例 | `case:case:add` | REQ-006 |
| PUT | `/api/v1/cases/{id}` | 编辑用例（version 递增） | `case:case:edit` | REQ-006 |
| DELETE | `/api/v1/cases/{id}` | 删除用例（逻辑删除） | `case:case:delete` | REQ-006 |
| PUT | `/api/v1/cases/{id}/status` | 启用/停用用例 | `case:case:edit` | REQ-006 |
| GET | `/api/v1/cases/tags` | 标签列表（用例筛选与标签管理共用） | `case:tag:list` | REQ-006 |
| POST | `/api/v1/cases/tags` | 新建标签 | `case:tag:add` | REQ-006 |
| DELETE | `/api/v1/cases/tags/{id}` | 删除标签（同步清理用例 tag_ids） | `case:tag:delete` | REQ-006 |

### 2.7 工作台（REQ-007）

| 方法 | 路径 | 说明 | 权限码 | 需求ID |
|:-----|:-----|:-----|:-------|:-------|
| GET | `/api/v1/dashboard/overview` | 统计概览卡片（用例总数/环境总数/用户总数/健康环境数） | `dashboard:view` | REQ-007 |
| GET | `/api/v1/dashboard/trend` | 近 7 天趋势（每日用例新增数 + 健康检查通过率） | `dashboard:view` | REQ-007 |
| GET | `/api/v1/dashboard/env-status` | 环境健康状态列表（各环境最新一次检查结果） | `dashboard:view` | REQ-007 |
| GET | `/api/v1/dashboard/activities` | 最近动态（最近操作日志 + 健康检查记录） | `dashboard:view` | REQ-007 |

### 2.8 全局搜索（REQ-008）

| 方法 | 路径 | 说明 | 权限码 | 需求ID |
|:-----|:-----|:-----|:-------|:-------|
| GET | `/api/v1/search` | 跨模块聚合搜索（用例/环境/用户，按类型分组返回） | `search:global` | REQ-008 |

---

## 3. 关键接口示例

### 3.1 登录

**请求** `POST /api/v1/auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应** `200`

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "平台管理员",
      "avatar": null,
      "roles": ["admin"],
      "perms": ["dashboard:view", "tool:view", "env:env:list", "system:user:list", "..."]
    }
  }
}
```

**失败**：账号不存在、密码错误或账号被禁用 → `40102`（统一提示"账号或密码错误"，不区分原因以防账号枚举；账号被禁用可提示"账号已被禁用"）。

### 3.2 分页列表（以用户列表为例）

**请求** `GET /api/v1/system/users?page=1&size=10&keyword=ad&status=1`

**响应** `200`

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 1,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "平台管理员",
        "email": null,
        "phone": null,
        "status": 1,
        "roleIds": [1],
        "roleNames": ["管理员"],
        "lastLoginTime": "2026-09-21 10:00:00",
        "createdAt": "2026-09-21 09:00:00"
      }
    ]
  }
}
```

### 3.3 环境健康自检

**请求** `POST /api/v1/envs/1/health-check`

**响应** `200`

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "envId": 1,
    "envName": "测试环境",
    "checkType": "HTTP",
    "target": "http://test.example.com",
    "status": "UP",
    "latencyMs": 86,
    "errorMsg": null,
    "checkedAt": "2026-09-21 10:30:00"
  }
}
```

> 探测逻辑：对 `base_url` 发起 HTTP GET（超时 3s），2xx/3xx 视为 UP，其余视为 DOWN 并记录 `error_msg`。

### 3.4 全局搜索

**请求** `GET /api/v1/search?keyword=登录`

**响应** `200`

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "keyword": "登录",
    "cases": [
      { "id": 12, "name": "登录接口-正常流程", "type": "API", "level": "P0", "matchField": "case_name" }
    ],
    "envs": [
      { "id": 2, "name": "测试环境", "code": "test", "matchField": "env_name" }
    ],
    "users": [
      { "id": 3, "username": "zhangsan", "nickname": "张三", "matchField": "nickname" }
    ]
  }
}
```

> 搜索范围与匹配规则：用例按 `case_name`/`description` LIKE；环境按 `env_name`/`env_code`/`description` LIKE；用户按 `username`/`nickname` LIKE。每类最多返回 10 条，按类型分组展示，点击跳转对应详情页。

---

## 4. M2+ 扩展位

M2+ 新增模块接口沿用 `/api/v1` 前缀与本章全部通用约定（响应体/错误码/分页/JWT/RBAC），按模块新增 controller 即可；`tc_case` 的执行接口（如 `POST /api/v1/cases/{id}/execute`）与结果回写接口在 M2+ 接口测试模块实现，M1 仅预留用例执行状态字段。

---

*文档结束*