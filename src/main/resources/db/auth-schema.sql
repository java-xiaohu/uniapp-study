-- ============================================================
-- 登录认证体系表结构（库：ai，手动执行）
-- 密码均为 BCrypt 哈希；deleted 对齐 MyBatis-Plus 全局逻辑删除配置
-- ============================================================

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    nickname    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    email       VARCHAR(100) NULL COMMENT '邮箱',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code   VARCHAR(50) NOT NULL COMMENT '角色编码，如 ADMIN/USER',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    remark      VARCHAR(200) NULL COMMENT '备注',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统角色';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户角色关联';

-- ------------------------------------------------------------
-- 种子数据：角色 + 管理员账号 admin / admin123
-- ------------------------------------------------------------
INSERT INTO sys_role (role_code, role_name, remark)
VALUES ('ADMIN', '管理员', '拥有全部权限'),
       ('USER', '普通用户', '默认注册角色')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO sys_user (username, password, nickname)
VALUES ('admin', '$2a$10$catL0jSzKE70oYTA/GDLguPe7DKJKcR13VJk4WlcPEvqAec5k/9.e', '管理员')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);