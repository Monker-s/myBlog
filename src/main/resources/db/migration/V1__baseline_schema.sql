-- ==========================================
-- myBlog baseline schema
-- 数据库：MySQL 8.0+
-- 认证方案：JWT Bearer Token（无状态认证）
-- 说明：当前基线不再创建 user_sessions 表
-- ==========================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET time_zone = '+00:00';

CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `role` TINYINT NOT NULL DEFAULT 1 COMMENT '1=普通用户, 99=管理员',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常, 0=禁用',
  `last_login_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_users_email` (`email`),
  CHECK (`role` IN (1, 99)),
  CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户账号表';

CREATE TABLE `admin_whitelist` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `granted_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_whitelist_user_id` (`user_id`),
  CONSTRAINT `fk_admin_whitelist_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员白名单';

CREATE TABLE `categories` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `slug` VARCHAR(50) NOT NULL,
  `parent_id` BIGINT UNSIGNED DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_categories_slug` (`slug`),
  KEY `idx_categories_parent_sort` (`parent_id`, `sort_order`),
  CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章分类树';

CREATE TABLE `tags` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(30) NOT NULL,
  `slug` VARCHAR(30) NOT NULL,
  `post_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tags_name` (`name`),
  UNIQUE KEY `uk_tags_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签主数据';

CREATE TABLE `posts` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `slug` VARCHAR(200) NOT NULL,
  `summary` VARCHAR(500) DEFAULT NULL,
  `cover_url` VARCHAR(500) DEFAULT NULL,
  `content_html` LONGTEXT NOT NULL,
  `toc_json` JSON DEFAULT NULL COMMENT '目录结构 JSON',
  `author_id` BIGINT UNSIGNED NOT NULL,
  `category_id` BIGINT UNSIGNED DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=草稿, 1=发布, 2=隐藏',
  `is_pinned` TINYINT NOT NULL DEFAULT 0,
  `pinned_at` DATETIME(3) DEFAULT NULL,
  `published_at` DATETIME(3) DEFAULT NULL,
  `like_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `view_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `comment_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '逻辑删除时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_posts_slug` (`slug`),
  KEY `idx_posts_list_query` (`status`, `is_pinned`, `updated_at` DESC, `id`),
  KEY `idx_posts_category_query` (`category_id`, `status`, `updated_at` DESC, `id`),
  KEY `idx_posts_author_query` (`author_id`, `status`),
  FULLTEXT KEY `ft_posts_search` (`title`, `summary`),
  CONSTRAINT `fk_posts_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_posts_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章主表';

CREATE TABLE `post_tags` (
  `post_id` BIGINT UNSIGNED NOT NULL,
  `tag_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`post_id`, `tag_id`),
  KEY `idx_post_tags_tag_posts` (`tag_id`, `post_id`),
  CONSTRAINT `fk_post_tags_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_post_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章与标签映射';

CREATE TABLE `post_likes` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_likes_user_post` (`user_id`, `post_id`),
  CONSTRAINT `fk_post_likes_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_post_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章点赞记录';

CREATE TABLE `post_view_dedup_daily` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `dedup_date` DATE NOT NULL,
  `dedup_key_hash` VARCHAR(64) NOT NULL COMMENT '用户或访客指纹哈希',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_view_dedup_daily_post_date_key` (`post_id`, `dedup_date`, `dedup_key_hash`),
  CONSTRAINT `fk_post_view_dedup_daily_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章浏览去重明细';

CREATE TABLE `comments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `parent_id` BIGINT UNSIGNED DEFAULT NULL,
  `depth` TINYINT NOT NULL DEFAULT 1,
  `content` TEXT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常, 0=隐藏',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_comments_post_thread` (`post_id`, `parent_id`, `created_at`),
  KEY `idx_comments_user_created_at` (`user_id`, `created_at` DESC),
  CHECK (`depth` IN (1, 2)),
  CONSTRAINT `fk_comments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='两级评论表';

CREATE TABLE `notifications` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `post_id` BIGINT UNSIGNED DEFAULT NULL,
  `content_hash` VARCHAR(64) NOT NULL COMMENT '内容指纹，用于防重',
  `title` VARCHAR(100) DEFAULT NULL,
  `body` VARCHAR(500) DEFAULT NULL,
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `read_at` DATETIME(3) DEFAULT NULL,
  `payload_json` JSON DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notifications_user_type_post_hash` (`user_id`, `type`, `post_id`, `content_hash`),
  KEY `idx_notifications_user_unread` (`user_id`, `is_read`, `created_at` DESC, `id`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_notifications_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内通知表';

CREATE TABLE `site_decorations` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `config_json` JSON NOT NULL,
  `is_active` TINYINT NOT NULL DEFAULT 0,
  `active_guard` TINYINT GENERATED ALWAYS AS (CASE WHEN `is_active` = 1 THEN 1 ELSE NULL END) STORED,
  `version_tag` VARCHAR(30) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_site_decorations_active` (`is_active`),
  UNIQUE KEY `uk_site_decorations_single_active` (`active_guard`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点动态装扮配置';

CREATE TABLE `uv_dedup_daily` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stat_date` DATE NOT NULL,
  `ip_prefix_hash` VARCHAR(32) NOT NULL,
  `ua_hash` VARCHAR(64) NOT NULL,
  `visitor_hash` VARCHAR(64) NOT NULL COMMENT '访客指纹哈希',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uv_dedup_daily_date_visitor` (`stat_date`, `visitor_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点 UV 去重原始明细';

CREATE TABLE `uv_daily_stats` (
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `uv_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `pv_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日 UV/PV 聚合结果';

DELIMITER //

CREATE TRIGGER `trg_comments_depth_limit_insert`
BEFORE INSERT ON `comments`
FOR EACH ROW
BEGIN
  IF NEW.parent_id IS NULL OR NEW.parent_id = 0 THEN
    SET NEW.parent_id = NULL;
    SET NEW.depth = 1;
  ELSE
    SELECT `depth` INTO @parent_depth FROM `comments` WHERE `id` = NEW.parent_id LIMIT 1;
    IF @parent_depth IS NULL OR @parent_depth <> 1 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '二级回复必须挂载在一级评论下';
    END IF;
    SET NEW.depth = 2;
  END IF;
END //

CREATE TRIGGER `trg_comments_depth_limit_update`
BEFORE UPDATE ON `comments`
FOR EACH ROW
BEGIN
  IF NEW.parent_id IS NULL OR NEW.parent_id = 0 THEN
    SET NEW.parent_id = NULL;
    SET NEW.depth = 1;
  ELSE
    SELECT `depth` INTO @parent_depth_update FROM `comments` WHERE `id` = NEW.parent_id LIMIT 1;
    IF @parent_depth_update IS NULL OR @parent_depth_update <> 1 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '二级回复必须挂载在一级评论下';
    END IF;
    SET NEW.depth = 2;
  END IF;
END //

CREATE TRIGGER `trg_users_role_admin_check_insert`
AFTER INSERT ON `users`
FOR EACH ROW
BEGIN
  IF NEW.role = 99 THEN
    IF NOT EXISTS (SELECT 1 FROM `admin_whitelist` WHERE `user_id` = NEW.id) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '管理员用户必须先存在于 admin_whitelist 中';
    END IF;
  END IF;
END //

CREATE TRIGGER `trg_users_role_admin_check_update`
BEFORE UPDATE ON `users`
FOR EACH ROW
BEGIN
  IF NEW.role = 99 THEN
    IF NOT EXISTS (SELECT 1 FROM `admin_whitelist` WHERE `user_id` = NEW.id) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '管理员用户必须先存在于 admin_whitelist 中';
    END IF;
  END IF;
END //

DELIMITER ;

SET FOREIGN_KEY_CHECKS = 1;
