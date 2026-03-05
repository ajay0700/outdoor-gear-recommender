-- 计划表：需求描述与提取关键词；用户偏好表：扩展画像维度
-- 执行前请确认表已存在；若列已存在会报错，可忽略或注释对应行

USE outdoor_gear;
SET NAMES utf8mb4;

ALTER TABLE `trip_plan` ADD COLUMN `requirement_text` VARCHAR(500) DEFAULT NULL COMMENT '用户输入的需求描述（自由文本）';
ALTER TABLE `trip_plan` ADD COLUMN `extracted_keywords` VARCHAR(255) DEFAULT NULL COMMENT '系统提取的关键词，逗号分隔';

ALTER TABLE `user_preference` ADD COLUMN `preferred_destinations` VARCHAR(255) DEFAULT NULL COMMENT '偏好目的地';
ALTER TABLE `user_preference` ADD COLUMN `preferred_categories` VARCHAR(255) DEFAULT NULL COMMENT '偏好装备分类';
ALTER TABLE `user_preference` ADD COLUMN `preferred_tags` VARCHAR(255) DEFAULT NULL COMMENT '偏好标签';
ALTER TABLE `user_preference` ADD COLUMN `difficulty_preference` VARCHAR(50) DEFAULT NULL COMMENT '难度偏好';
