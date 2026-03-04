-- 角色申请表（阶段三 3.2 专家/装备管理员申请与审核）
-- 若已执行过 schema.sql，可单独执行本脚本添加 role_apply 表
USE outdoor_gear;

CREATE TABLE IF NOT EXISTS `role_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_code` VARCHAR(50) NOT NULL COMMENT 'ROLE_EXPERT（专家可申请；装备管理员由管理员授予）',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '申请理由',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待审核 1-已通过 2-已拒绝',
  `admin_note` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
  `reviewed_by` BIGINT DEFAULT NULL,
  `reviewed_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色申请表';
