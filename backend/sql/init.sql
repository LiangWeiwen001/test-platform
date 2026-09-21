-- ============================================================
-- 测试平台数据库初始化脚本（M1 基础平台）
-- 权威来源：docs/DESIGN_数据模型.md 第 3 章（DDL）+ 第 4 章（种子数据）
-- 规范：InnoDB / utf8mb4 / utf8mb4_unicode_ci / bigint 自增主键 /
--       created_at/updated_at 默认 CURRENT_TIMESTAMP / 逻辑删除 deleted /
--       uk_ 唯一索引（含 deleted）/ idx_ 普通索引 / 无外键
-- 可重复执行：CREATE TABLE IF NOT EXISTS + INSERT IGNORE（依赖唯一索引去重）
-- ============================================================

CREATE DATABASE IF NOT EXISTS `test_platform`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `test_platform`;

-- ------------------------------------------------------------
-- 1. sys_user 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt 加密密码',
  `nickname` varchar(50) NOT NULL COMMENT '昵称/姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像 URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 / 0禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录 IP',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 / 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. sys_role 角色表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码（admin/test/dev/ops）',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 / 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 / 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ------------------------------------------------------------
-- 3. sys_user_role 用户-角色关联表（无逻辑删除）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户 ID（外键意图 → sys_user.id）',
  `role_id` bigint NOT NULL COMMENT '角色 ID（外键意图 → sys_role.id）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- ------------------------------------------------------------
-- 4. sys_permission 权限表（菜单 + 按钮）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级 ID（0=顶级菜单；外键意图 → sys_permission.id）',
  `perm_name` varchar(50) NOT NULL COMMENT '权限/菜单名称',
  `perm_code` varchar(100) NOT NULL COMMENT '权限码（如 system:user:list）',
  `perm_type` char(1) NOT NULL DEFAULT 'M' COMMENT '类型：M菜单 / B按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '前端路由路径（菜单用）',
  `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序号（同级升序）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 / 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 / 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`, `deleted`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表（菜单+按钮）';

-- ------------------------------------------------------------
-- 5. sys_role_permission 角色-权限关联表（无逻辑删除）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色 ID（外键意图 → sys_role.id）',
  `permission_id` bigint NOT NULL COMMENT '权限 ID（外键意图 → sys_permission.id）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- ------------------------------------------------------------
-- 6. sys_operation_log 操作审计日志表（只增不改，无 updated_at/deleted）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '操作人 ID（外键意图 → sys_user.id）',
  `username` varchar(50) NOT NULL COMMENT '操作人账号（冗余，防用户删除后丢失）',
  `module` varchar(50) NOT NULL COMMENT '所属模块（auth/system/env/case/dashboard/search）',
  `operation` varchar(100) NOT NULL COMMENT '操作描述（如"新增用户"）',
  `method` varchar(10) NOT NULL COMMENT 'HTTP 方法（GET/POST/PUT/DELETE）',
  `url` varchar(255) NOT NULL COMMENT '请求路径',
  `params` text COMMENT '请求参数（JSON 字符串，敏感字段脱敏）',
  `result` tinyint NOT NULL DEFAULT 1 COMMENT '结果：1成功 / 0失败',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作 IP',
  `cost_ms` int NOT NULL DEFAULT 0 COMMENT '耗时（毫秒）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表（只增不改）';

-- ------------------------------------------------------------
-- 7. td_environment 测试环境表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `td_environment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `env_code` varchar(50) NOT NULL COMMENT '环境标识（dev/test/prod）',
  `env_name` varchar(50) NOT NULL COMMENT '环境名称',
  `base_url` varchar(255) DEFAULT NULL COMMENT '被测系统基础地址（健康自检探测目标）',
  `description` varchar(255) DEFAULT NULL COMMENT '环境描述',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 / 0停用',
  `created_by` bigint NOT NULL COMMENT '创建人（外键意图 → sys_user.id）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 / 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_env_code` (`env_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试环境表';

-- ------------------------------------------------------------
-- 8. td_environment_health 环境健康检查记录表（只增不改，无 updated_at/deleted）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `td_environment_health` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `env_id` bigint NOT NULL COMMENT '环境 ID（外键意图 → td_environment.id）',
  `check_type` varchar(20) NOT NULL COMMENT '检查类型（HTTP/MySQL/Redis）',
  `target` varchar(255) NOT NULL COMMENT '检测目标（URL/连接串，脱敏存储）',
  `status` varchar(10) NOT NULL COMMENT '结果：UP / DOWN',
  `latency_ms` int NOT NULL DEFAULT 0 COMMENT '响应耗时（毫秒）',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `checked_by` bigint NOT NULL COMMENT '触发人（外键意图 → sys_user.id；定时任务为 0）',
  `checked_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
  PRIMARY KEY (`id`),
  KEY `idx_env_id` (`env_id`),
  KEY `idx_checked_at` (`checked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境健康检查记录表（只增不改）';

-- ------------------------------------------------------------
-- 9. tc_case 测试用例表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tc_case` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `case_name` varchar(200) NOT NULL COMMENT '用例名称',
  `case_type` varchar(20) NOT NULL DEFAULT 'API' COMMENT '用例类型：API接口/PERF性能/DB数据库/COMMON通用',
  `case_level` varchar(10) NOT NULL DEFAULT 'P1' COMMENT '优先级：P0/P1/P2',
  `description` text COMMENT '用例描述',
  `request_config` json DEFAULT NULL COMMENT '请求配置（接口用例：method/url/headers/body）',
  `expected_result` json DEFAULT NULL COMMENT '预期结果（断言配置）',
  `tag_ids` varchar(255) DEFAULT NULL COMMENT '标签 ID 列表（逗号分隔，如 "1,2,3"）',
  `version` varchar(20) NOT NULL DEFAULT 'v1.0' COMMENT '用例版本（编辑时递增）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 / 0停用',
  `created_by` bigint NOT NULL COMMENT '创建人（外键意图 → sys_user.id）',
  `updated_by` bigint NOT NULL COMMENT '最后修改人（外键意图 → sys_user.id）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 / 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_case_type` (`case_type`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试用例表';

-- ------------------------------------------------------------
-- 10. tc_case_tag 用例标签表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tc_case_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `tag_color` varchar(20) NOT NULL DEFAULT '#409EFF' COMMENT '标签颜色（Element Plus 色值）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 / 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用例标签表';

-- ============================================================
-- 种子数据（数据模型文档第 4 章）
-- INSERT IGNORE 依赖唯一索引去重，脚本可重复执行
-- ============================================================

-- 4.1 用户与角色
-- admin 用户：BCrypt(admin123) = $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
INSERT IGNORE INTO `sys_user`
  (`id`, `username`, `password`, `nickname`, `email`, `phone`, `avatar`, `status`)
VALUES
  (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '平台管理员', 'admin@testplatform.local', NULL, NULL, 1);

INSERT IGNORE INTO `sys_role`
  (`id`, `role_code`, `role_name`, `description`, `status`)
VALUES
  (1, 'admin', '管理员', '系统管理员，拥有全部权限', 1),
  (2, 'test',  '测试',   '测试人员，负责用例维护与环境健康自检', 1),
  (3, 'dev',   '开发',   '开发人员，只读查看环境与用例', 1),
  (4, 'ops',   '运维',   '运维人员，负责环境维护与健康自检', 1);

-- admin 用户绑定 admin 角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 4.2 权限树（sys_permission，M=菜单 / B=按钮）
-- 固定 id 1-27，parent_id 引用保证树形结构
INSERT IGNORE INTO `sys_permission`
  (`id`, `parent_id`, `perm_name`, `perm_code`, `perm_type`, `path`, `icon`, `sort`, `status`)
VALUES
  (1,  0, '工作台',   'dashboard:view',     'M', '/dashboard',    'Odometer',  1, 1),
  (2,  0, '工具菜单', 'tool:view',          'M', '/tool',         'Tools',     2, 1),
  (3,  0, '环境管理', 'env:env:list',       'M', '/env',          'Monitor',   3, 1),
  (4,  0, '用例管理', 'case:case:list',     'M', '/case',         'Document',  4, 1),
  (5,  0, '系统管理', 'system:menu',        'M', '/system',       'Setting',   5, 1),
  (6,  5, '用户管理', 'system:user:list',   'M', '/system/user',  'User',      1, 1),
  (7,  6, '新建用户', 'system:user:add',    'B', NULL,            NULL,        1, 1),
  (8,  6, '编辑用户', 'system:user:edit',   'B', NULL,            NULL,        2, 1),
  (9,  6, '删除用户', 'system:user:delete', 'B', NULL,            NULL,        3, 1),
  (10, 6, '重置密码', 'system:user:resetPwd','B', NULL,           NULL,        4, 1),
  (11, 5, '角色权限', 'system:role:list',   'M', '/system/role',  'Avatar',    2, 1),
  (12, 11, '新建角色', 'system:role:add',   'B', NULL,            NULL,        1, 1),
  (13, 11, '编辑角色', 'system:role:edit',  'B', NULL,            NULL,        2, 1),
  (14, 11, '删除角色', 'system:role:delete','B', NULL,            NULL,        3, 1),
  (15, 11, '分配权限', 'system:role:assignPerm','B', NULL,        NULL,        4, 1),
  (16, 5, '操作日志', 'system:log:list',    'M', '/system/log',   'Tickets',   3, 1),
  (17, 3, '新建环境', 'env:env:add',        'B', NULL,            NULL,        1, 1),
  (18, 3, '编辑环境', 'env:env:edit',       'B', NULL,            NULL,        2, 1),
  (19, 3, '删除环境', 'env:env:delete',     'B', NULL,            NULL,        3, 1),
  (20, 3, '健康自检', 'env:env:health',     'B', NULL,            NULL,        4, 1),
  (21, 4, '新建用例', 'case:case:add',      'B', NULL,            NULL,        1, 1),
  (22, 4, '编辑用例', 'case:case:edit',     'B', NULL,            NULL,        2, 1),
  (23, 4, '删除用例', 'case:case:delete',   'B', NULL,            NULL,        3, 1),
  (24, 4, '标签管理', 'case:tag:list',      'B', NULL,            NULL,        4, 1),
  (25, 4, '新建标签', 'case:tag:add',       'B', NULL,            NULL,        5, 1),
  (26, 4, '删除标签', 'case:tag:delete',    'B', NULL,            NULL,        6, 1),
  (27, 0, '全局搜索', 'search:global',      'M', NULL,            'Search',    6, 1);

-- 4.3 角色-权限绑定（sys_role_permission）
-- admin：全部权限（1-27）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `sys_permission` WHERE `deleted` = 0;

-- test：工作台 / 工具菜单 / 环境查看 / 健康自检 / 用例全部（含标签维护）/ 全局搜索
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
  (2, 1), (2, 2), (2, 3), (2, 20),
  (2, 4), (2, 21), (2, 22), (2, 23), (2, 24), (2, 25), (2, 26),
  (2, 27);

-- dev：工作台 / 工具菜单 / 环境查看 / 用例查看 / 标签查看 / 全局搜索
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
  (3, 1), (3, 2), (3, 3), (3, 4), (3, 24), (3, 27);

-- ops：工作台 / 工具菜单 / 环境全部（含健康自检）/ 用例查看 / 标签查看 / 全局搜索
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
  (4, 1), (4, 2), (4, 3), (4, 17), (4, 18), (4, 19), (4, 20),
  (4, 4), (4, 24), (4, 27);

-- 4.4 环境种子数据（created_by = 1 即 admin）
INSERT IGNORE INTO `td_environment`
  (`id`, `env_code`, `env_name`, `base_url`, `description`, `sort`, `status`, `created_by`)
VALUES
  (1, 'dev',  '开发环境', 'http://localhost:8081',  '本地开发环境', 1, 1, 1),
  (2, 'test', '测试环境', 'http://test.example.com', '测试环境',     2, 1, 1),
  (3, 'prod', '生产环境', 'http://prod.example.com', '生产环境',     3, 1, 1);

USE `test_platform`;