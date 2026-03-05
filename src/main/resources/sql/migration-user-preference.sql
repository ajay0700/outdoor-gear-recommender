-- 用户偏好表（用于冷启动：注册时采集偏好，供内容推荐使用）
USE outdoor_gear;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_preference` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `season` VARCHAR(50) DEFAULT NULL COMMENT '偏好季节：春/夏/秋/冬/四季',
  `activity_type` VARCHAR(100) DEFAULT NULL COMMENT '偏好活动类型：徒步/露营/登山等',
  `budget_min` DECIMAL(10,2) DEFAULT NULL COMMENT '预算下限',
  `budget_max` DECIMAL(10,2) DEFAULT NULL COMMENT '预算上限',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好表';
