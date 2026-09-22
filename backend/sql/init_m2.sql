-- ============================================================
-- M2 数据查询与接口测试模块：建表 + 权限种子
-- 6 张新表：td_datasource / td_log_source / td_redis_conn /
--           api_collection / api_exec_record / td_exec_log
-- 沿用 DDL 规范：InnoDB / utf8mb4 / 逻辑删除 deleted / uk_ 含 deleted / 无外键
-- ============================================================
USE test_platform;

-- ------------------------------------------------------------
-- 1. td_datasource 数据库数据源
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `td_datasource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `env_id` bigint NOT NULL COMMENT '关联环境（外键意图 → td_environment.id）',
  `ds_name` varchar(50) NOT NULL COMMENT '数据源名称',
  `db_type` varchar(20) NOT NULL COMMENT 'mysql/postgresql/oracle/sqlserver',
  `host` varchar(100) NOT NULL COMMENT '主机',
  `port` int NOT NULL DEFAULT 3306 COMMENT '端口',
  `database_name` varchar(100) NOT NULL COMMENT '库名',
  `username` varchar(100) NOT NULL COMMENT '账号',
  `password_enc` varchar(255) DEFAULT NULL COMMENT '密码（AES-GCM 加密）',
  `readonly` tinyint NOT NULL DEFAULT 1 COMMENT '1=只读（禁止写 SQL）/0=可写',
  `connect_timeout` int NOT NULL DEFAULT 5 COMMENT '连接超时（秒）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用/0停用',
  `created_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人（外键意图 → sys_user.id）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常/1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ds_env_name` (`env_id`, `ds_name`, `deleted`),
  KEY `idx_ds_env_id` (`env_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库数据源';

-- ------------------------------------------------------------
-- 2. td_log_source 日志源
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `td_log_source` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `env_id` bigint NOT NULL COMMENT '关联环境（外键意图 → td_environment.id）',
  `source_name` varchar(50) NOT NULL COMMENT '日志源名称',
  `source_type` varchar(20) NOT NULL DEFAULT 'file' COMMENT 'file（文件）/es（预留）/loki（预留）',
  `path_pattern` varchar(255) NOT NULL COMMENT '文件路径或通配符（如 /var/log/app/*.log）',
  `host` varchar(100) DEFAULT 'localhost' COMMENT '文件所在主机（预留）',
  `charset` varchar(20) NOT NULL DEFAULT 'utf-8' COMMENT '编码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用/0停用',
  `created_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ls_env_name` (`env_id`, `source_name`, `deleted`),
  KEY `idx_ls_env_id` (`env_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日志源';

-- ------------------------------------------------------------
-- 3. td_redis_conn Redis 连接
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `td_redis_conn` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `env_id` bigint NOT NULL COMMENT '关联环境（外键意图 → td_environment.id）',
  `conn_name` varchar(50) NOT NULL COMMENT '连接名称',
  `mode` varchar(20) NOT NULL DEFAULT 'standalone' COMMENT 'standalone（单机）/cluster（预留）/sentinel（预留）',
  `host` varchar(100) NOT NULL COMMENT '主机',
  `port` int NOT NULL DEFAULT 6379 COMMENT '端口',
  `password_enc` varchar(255) DEFAULT NULL COMMENT '密码（AES-GCM 加密）',
  `db_index` int NOT NULL DEFAULT 0 COMMENT '默认 db（0-15）',
  `timeout` int NOT NULL DEFAULT 3 COMMENT '超时（秒）',
  `readonly` tinyint NOT NULL DEFAULT 1 COMMENT '1=只读（禁止写命令）/0=可写',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1启用/0停用',
  `created_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rc_env_name` (`env_id`, `conn_name`, `deleted`),
  KEY `idx_rc_env_id` (`env_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Redis 连接';

-- ------------------------------------------------------------
-- 4. api_collection 接口测试集合（集合 → 目录 → 用例 三层）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `api_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级 ID（0=顶层集合；目录的父=集合，用例的父=目录）',
  `node_type` char(2) NOT NULL DEFAULT 'C' COMMENT 'C=集合/D=目录/C2=用例',
  `name` varchar(100) NOT NULL COMMENT '节点名称',
  `case_id` bigint DEFAULT NULL COMMENT '用例节点关联 tc_case.id（复用 M1 用例库）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '同级排序',
  `created_by` bigint NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_ac_parent_id` (`parent_id`),
  KEY `idx_ac_case_id` (`case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口测试集合树';

-- ------------------------------------------------------------
-- 5. api_exec_record 接口执行记录（只增不改，无 deleted）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `api_exec_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `case_id` bigint NOT NULL COMMENT '关联 tc_case.id',
  `collection_id` bigint DEFAULT NULL COMMENT '关联集合（批量执行用）',
  `env_id` bigint NOT NULL COMMENT '执行环境',
  `request_snapshot` json DEFAULT NULL COMMENT '实际请求（method/url/headers/body）',
  `response_snapshot` json DEFAULT NULL COMMENT '响应（status/headers/body）',
  `assertions` json DEFAULT NULL COMMENT '断言结果 [{name,pass,detail}]',
  `success` tinyint NOT NULL DEFAULT 0 COMMENT '1通过/0失败',
  `cost_ms` int NOT NULL DEFAULT 0 COMMENT '耗时（毫秒）',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `executed_by` bigint NOT NULL DEFAULT 0 COMMENT '执行人（外键意图 → sys_user.id）',
  `executed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  PRIMARY KEY (`id`),
  KEY `idx_aer_case_id` (`case_id`),
  KEY `idx_aer_collection_id` (`collection_id`),
  KEY `idx_aer_executed_at` (`executed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口执行记录（只增不改）';

-- ------------------------------------------------------------
-- 6. td_exec_log 数据查询执行记录（DB SQL + Redis 命令统一审计，只增不改）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `td_exec_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `target_type` varchar(20) NOT NULL COMMENT 'db/redis',
  `target_id` bigint NOT NULL COMMENT '数据源/Redis 连接 ID',
  `operation` varchar(20) NOT NULL COMMENT 'query（查询）/execute（写）',
  `content` text COMMENT 'SQL 语句或 Redis 命令（写操作脱敏）',
  `success` tinyint NOT NULL DEFAULT 1 COMMENT '1成功/0失败',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `cost_ms` int NOT NULL DEFAULT 0 COMMENT '耗时（毫秒）',
  `executed_by` bigint NOT NULL DEFAULT 0 COMMENT '执行人（外键意图 → sys_user.id）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  PRIMARY KEY (`id`),
  KEY `idx_tel_target` (`target_type`, `target_id`),
  KEY `idx_tel_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据查询执行审计（只增不改）';

-- ============================================================
-- 权限种子（新增 M2 权限码）
-- ============================================================
-- 顶级菜单：数据查询
INSERT INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, deleted)
SELECT 0, '数据查询', 'query:menu', 'M', '/query', 'Search', 40, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'query:menu');

SET @qmenu := (SELECT id FROM sys_permission WHERE perm_code = 'query:menu');

-- 数据库查询子菜单 + 按钮
INSERT INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, deleted)
SELECT @qmenu, '数据库查询', 'query:db:menu', 'M', '/query/db', 'Coin', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'query:db:menu');

SET @db_menu := (SELECT id FROM sys_permission WHERE perm_code = 'query:db:menu');

INSERT IGNORE INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, sort, status, deleted) VALUES
(@db_menu, '数据源列表', 'query:db:list', 'M', '/query/db', 1, 1, 0),
(@db_menu, '新建数据源', 'query:db:add', 'B', NULL, 2, 1, 0),
(@db_menu, '编辑数据源', 'query:db:edit', 'B', NULL, 3, 1, 0),
(@db_menu, '删除数据源', 'query:db:delete', 'B', NULL, 4, 1, 0),
(@db_menu, 'SQL 查询', 'query:db:query', 'B', NULL, 5, 1, 0),
(@db_menu, 'SQL 写操作', 'query:db:write', 'B', NULL, 6, 1, 0);

-- Redis 查询子菜单 + 按钮
INSERT INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, deleted)
SELECT @qmenu, 'Redis 查询', 'query:redis:menu', 'M', '/query/redis', 'Aim', 2, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'query:redis:menu');

SET @redis_menu := (SELECT id FROM sys_permission WHERE perm_code = 'query:redis:menu');

INSERT IGNORE INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, sort, status, deleted) VALUES
(@redis_menu, '连接列表', 'query:redis:list', 'M', '/query/redis', 1, 1, 0),
(@redis_menu, '新建连接', 'query:redis:add', 'B', NULL, 2, 1, 0),
(@redis_menu, '编辑连接', 'query:redis:edit', 'B', NULL, 3, 1, 0),
(@redis_menu, '删除连接', 'query:redis:delete', 'B', NULL, 4, 1, 0),
(@redis_menu, '数据查看', 'query:redis:query', 'B', NULL, 5, 1, 0),
(@redis_menu, '写操作', 'query:redis:write', 'B', NULL, 6, 1, 0);

-- 日志查询子菜单 + 按钮
INSERT INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, deleted)
SELECT @qmenu, '日志查询', 'query:log:menu', 'M', '/query/log', 'Document', 3, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'query:log:menu');

SET @log_menu := (SELECT id FROM sys_permission WHERE perm_code = 'query:log:menu');

INSERT IGNORE INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, sort, status, deleted) VALUES
(@log_menu, '日志源列表', 'query:log:list', 'M', '/query/log', 1, 1, 0),
(@log_menu, '新建日志源', 'query:log:add', 'B', NULL, 2, 1, 0),
(@log_menu, '编辑日志源', 'query:log:edit', 'B', NULL, 3, 1, 0),
(@log_menu, '删除日志源', 'query:log:delete', 'B', NULL, 4, 1, 0),
(@log_menu, '日志检索', 'query:log:query', 'B', NULL, 5, 1, 0);

-- 顶级菜单：接口测试
INSERT INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, icon, sort, status, deleted)
SELECT 0, '接口测试', 'api:menu', 'M', '/api', 'Promotion', 50, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE perm_code = 'api:menu');

SET @api_menu := (SELECT id FROM sys_permission WHERE perm_code = 'api:menu');

INSERT IGNORE INTO sys_permission (parent_id, perm_name, perm_code, perm_type, path, sort, status, deleted) VALUES
(@api_menu, '集合管理', 'api:case:list', 'M', '/api', 1, 1, 0),
(@api_menu, '新建用例', 'api:case:add', 'B', NULL, 2, 1, 0),
(@api_menu, '编辑用例', 'api:case:edit', 'B', NULL, 3, 1, 0),
(@api_menu, '删除用例', 'api:case:delete', 'B', NULL, 4, 1, 0),
(@api_menu, '执行用例', 'api:case:execute', 'B', NULL, 5, 1, 0);

-- ============================================================
-- 角色权限绑定：admin 全部新权限；test 查询+执行（含 write）；dev 只读查询；ops 环境维护
-- ============================================================
-- admin：全部新权限
INSERT INTO sys_role_permission (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM sys_role r CROSS JOIN sys_permission p
WHERE r.role_code = 'admin'
  AND p.perm_code IN ('query:db:list','query:db:add','query:db:edit','query:db:delete','query:db:query','query:db:write',
  'query:redis:list','query:redis:add','query:redis:edit','query:redis:delete','query:redis:query','query:redis:write',
  'query:log:list','query:log:add','query:log:edit','query:log:delete','query:log:query',
  'api:case:list','api:case:add','api:case:edit','api:case:delete','api:case:execute')
  AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- test：查询+执行（含 write）+ api 全
INSERT INTO sys_role_permission (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM sys_role r CROSS JOIN sys_permission p
WHERE r.role_code = 'test'
  AND p.perm_code IN ('query:db:list','query:db:query','query:db:write',
  'query:redis:list','query:redis:query','query:redis:write',
  'query:log:list','query:log:query',
  'api:case:list','api:case:add','api:case:edit','api:case:execute')
  AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- dev：只读查询
INSERT INTO sys_role_permission (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM sys_role r CROSS JOIN sys_permission p
WHERE r.role_code = 'dev'
  AND p.perm_code IN ('query:db:list','query:redis:list','query:log:list')
  AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- ops：同 dev 只读查询 + 日志源维护
INSERT INTO sys_role_permission (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM sys_role r CROSS JOIN sys_permission p
WHERE r.role_code = 'ops'
  AND p.perm_code IN ('query:db:list','query:redis:list','query:log:list','query:log:add','query:log:edit','query:log:delete')
  AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- ============================================================
-- 种子数据（演示用）：为 dev 环境创建一个 MySQL 数据源（指向平台自身 test_platform）
-- 密码为空（password_enc=NULL），连接时需用户在页面编辑填入
-- ============================================================
INSERT INTO td_datasource (env_id, ds_name, db_type, host, port, database_name, username, password_enc, readonly, connect_timeout, status, created_by)
SELECT id, '平台本地库', 'mysql', 'localhost', 3306, 'test_platform', 'tp', NULL, 1, 5, 1, 1
FROM td_environment WHERE env_code = 'dev'
AND NOT EXISTS (SELECT 1 FROM td_datasource WHERE env_id = (SELECT id FROM td_environment WHERE env_code = 'dev') AND ds_name = '平台本地库' AND deleted = 0);

-- ============================================================
-- 结束
-- ============================================================