-- 户外运动装备推荐系统 - 建表脚本
-- 数据库：outdoor_gear，字符集 utf8mb4
-- 执行前请先创建库：CREATE DATABASE IF NOT EXISTS outdoor_gear DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户与权限
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL COMMENT 'ROLE_ADMIN/ROLE_USER/ROLE_GEAR_ADMIN/ROLE_EXPERT',
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-正常',
  `points` INT NOT NULL DEFAULT 0,
  `level` INT NOT NULL DEFAULT 1,
  `last_login_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

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

-- ----------------------------
-- 2. 装备分类与标签
-- ----------------------------
CREATE TABLE IF NOT EXISTS `gear_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `parent_id` BIGINT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='装备分类表';

CREATE TABLE IF NOT EXISTS `gear_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `type` VARCHAR(50) DEFAULT NULL COMMENT 'FEATURE/SCENE/SEASON',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签字典表';

CREATE TABLE IF NOT EXISTS `gear_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `category_id` BIGINT NOT NULL,
  `brand` VARCHAR(100) DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `weight` DECIMAL(10,2) DEFAULT NULL,
  `season` VARCHAR(50) DEFAULT NULL,
  `scene` VARCHAR(100) DEFAULT NULL,
  `comfort_temperature` VARCHAR(50) DEFAULT NULL,
  `max_users` INT DEFAULT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  `cover_image` VARCHAR(255) DEFAULT NULL,
  `image_list` TEXT DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-下架 1-上架',
  `created_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_price` (`price`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='装备主表';

CREATE TABLE IF NOT EXISTS `gear_item_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `gear_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gear_tag` (`gear_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='装备-标签关联表';

-- ----------------------------
-- 3. 出行计划
-- ----------------------------
CREATE TABLE IF NOT EXISTS `trip_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `destination` VARCHAR(100) DEFAULT NULL,
  `start_date` DATE DEFAULT NULL,
  `end_date` DATE DEFAULT NULL,
  `days` INT DEFAULT NULL,
  `people_count` INT DEFAULT NULL,
  `budget` DECIMAL(10,2) DEFAULT NULL,
  `season` VARCHAR(50) DEFAULT NULL,
  `activity_type` VARCHAR(50) DEFAULT NULL,
  `difficulty_level` TINYINT DEFAULT NULL COMMENT '1-5',
  `note` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-草稿 1-进行中 2-已结束',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出行计划表';

CREATE TABLE IF NOT EXISTS `trip_plan_gear` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `plan_id` BIGINT NOT NULL,
  `gear_id` BIGINT NOT NULL,
  `source` VARCHAR(20) DEFAULT 'RECOMMEND' COMMENT 'RECOMMEND/MANUAL',
  `quantity` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_gear_id` (`gear_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划-装备关联表';

-- ----------------------------
-- 4. 用户行为与订单
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `gear_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_gear` (`user_id`,`gear_id`),
  KEY `idx_gear_id` (`gear_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

CREATE TABLE IF NOT EXISTS `user_cart_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `gear_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `selected` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_gear_id` (`gear_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车明细表';

CREATE TABLE IF NOT EXISTS `gear_rating` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `gear_id` BIGINT NOT NULL,
  `score` TINYINT NOT NULL COMMENT '1-5',
  `comment` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_gear` (`user_id`,`gear_id`),
  KEY `idx_gear_id` (`gear_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='装备评分表';

CREATE TABLE IF NOT EXISTS `gear_browse_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `gear_id` BIGINT NOT NULL,
  `plan_id` BIGINT DEFAULT NULL,
  `action` VARCHAR(20) DEFAULT NULL COMMENT 'VIEW/CLICK_RECOMMEND',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_gear_id` (`gear_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览/行为日志表';

CREATE TABLE IF NOT EXISTS `order_main` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已取消 3-已完成',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `gear_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_gear_id` (`gear_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

CREATE TABLE IF NOT EXISTS `recommend_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `plan_id` BIGINT DEFAULT NULL,
  `user_id` BIGINT NOT NULL,
  `algorithm` VARCHAR(50) DEFAULT NULL COMMENT 'CONSTRAINT/CF/CONTENT/HYBRID',
  `result_json` MEDIUMTEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐结果快照表';

-- ----------------------------
-- 5. 社区
-- ----------------------------
CREATE TABLE IF NOT EXISTS `community_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` MEDIUMTEXT DEFAULT NULL,
  `images` TEXT DEFAULT NULL,
  `view_count` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `comment_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-待审核 1-正常 2-屏蔽',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

CREATE TABLE IF NOT EXISTS `community_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `like_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

CREATE TABLE IF NOT EXISTS `community_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

CREATE TABLE IF NOT EXISTS `community_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_type` VARCHAR(20) NOT NULL COMMENT 'POST/COMMENT',
  `target_id` BIGINT NOT NULL,
  `reporter_id` BIGINT NOT NULL,
  `reason` VARCHAR(255) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待处理 1-已忽略 2-已处理',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

SET FOREIGN_KEY_CHECKS = 1;
