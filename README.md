# 测试平台（test-platform）

> 一句话定位：面向测试/开发/运维的统一测试平台——查日志、查数据库、查 Redis、接口测试、性能测试、工具集，MVP 先跑通 M1 基础平台。

## 技术栈

| 层 | 选型 |
|:---|:-----|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router |
| 后端 | Java 17 + Spring Boot 3 + MyBatis-Plus + MySQL 8 |
| 认证 | JWT（登录态）+ RBAC 菜单级权限 |
| 部署 | 本地开发：前端 Vite dev server + 后端内嵌 Tomcat |

> 选型理由见 `docs/ADR/ADR-001-技术栈选型.md`

## 快速开始

### 前置依赖
- Node.js ≥ 18、JDK 17、Maven ≥ 3.8、MySQL 8

### 启动步骤
1. 建库：执行 `backend/sql/init.sql`（建库 + 初始化表 + 种子数据）
2. 后端：`cd backend && mvn spring-boot:run`（默认 8080，配置见 `application.yml`）
3. 前端：`cd frontend && npm install && npm run dev`（默认 5173，代理 /api → 8080）
4. 浏览器打开 `http://localhost:5173`，默认账号 `admin / admin123`

> 详见 `docs/环境搭建说明.md`

## 目录结构

```
test-platform/
├── frontend/          # Vue3 前端
├── backend/           # Spring Boot 后端
├── docs/              # 项目文档（PRD / 管理表 / ADR / CHANGELOG）
│   ├── PRD.md         # 需求文档（v1.5，16 模块）
│   ├── REQ_需求排期表.md
│   ├── DEV_开发进度表.md
│   ├── BUG_优化Bug记录表.md
│   ├── ADR/           # 架构决策记录
│   └── 环境搭建说明.md
└── .gitignore
```

## 常用命令

| 命令 | 说明 |
|:-----|:-----|
| `cd frontend && npm run dev` | 启动前端开发服务器 |
| `cd backend && mvn spring-boot:run` | 启动后端 |
| `cd backend && mvn test` | 后端单测 |
| `scripts/push-github.ps1` | 攒批推送 GitHub（多 IP 轮换重试） |

## 文档索引

- 需求：`docs/PRD.md`（完整 16 模块；MVP = M1，见第 6 章里程碑）
- 管理表：需求排期 / 开发进度 / Bug 记录（三表联动）
- 决策：`docs/ADR/`（技术栈等关键选型）
- 变更：`docs/CHANGELOG.md`

## 状态

- 阶段：0 需求定义 ✅ → 1 设计 ✅ → 2 开发（M1 完成）→ 3 测试验收（进行中）
- MVP：M1 基础平台已全部实现 ✅

## M1 已实现功能（v0.1）

| 模块 | 功能 |
|:-----|:-----|
| 登录认证 | JWT 登录/登出/刷新/当前用户，RBAC 菜单+按钮级权限，v-perm 指令 |
| 工作台 | 统计卡片（用户/环境/用例/健康环境）+ 近 7 日趋势 + 环境状态 + 操作动态 |
| 工具菜单 | JSON 格式化、加解密（MD5/SHA256/Base64）、时间戳互转、正则测试、URL 工具、随机数据、二维码、文本工具（可插拔注册表） |
| 环境管理 | 环境 CRUD + 启用/停用 + 健康自检（3s 超时探测写记录）+ 健康历史 |
| 用例管理 | 用例 CRUD + 类型/级别/标签筛选 + 版本递增 + 标签管理（含用例计数） |
| 系统管理 | 用户管理（CRUD/禁用/重置密码）+ 角色权限（角色 CRUD + 权限树分配） |
| 全局搜索 | 顶栏搜索框，跨用例/环境/用户 LIKE 搜索，分组展示可跳转 |

- 里程碑：v0.1（M1 完成）→ v0.2（M2 数据查询类）→ 后续 M3-M5