-- ============================================
-- Prometheus 技能时间银行 - 数据库初始化脚本
-- 数据库: prometheus_skill_bank
-- 表数量: 15 张
-- MySQL 8.0+
-- ============================================

CREATE DATABASE IF NOT EXISTS prometheus_skill_bank
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE prometheus_skill_bank;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '用户ID，雪花算法',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名，唯一',
    `password` VARCHAR(255) NOT NULL COMMENT '密码，BCrypt加密',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    `balance` INT NOT NULL DEFAULT 0 COMMENT '时间币余额',
    `frozen_balance` INT NOT NULL DEFAULT 0 COMMENT '冻结时间币',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 技能分类表
-- ============================================
CREATE TABLE `skill_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能分类表';

-- ============================================
-- 3. 技能货架表
-- ============================================
CREATE TABLE `skill` (
    `id` BIGINT NOT NULL COMMENT '技能ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者用户ID',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `title` VARCHAR(100) NOT NULL COMMENT '技能标题',
    `description` TEXT COMMENT '技能描述',
    `price` INT NOT NULL COMMENT '定价（时间币/小时）',
    `available_time` VARCHAR(200) DEFAULT NULL COMMENT '可用时间段描述',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    `status` TINYINT NOT NULL DEFAULT 2 COMMENT '状态: 0下架 1上架 2待审核 3已拒绝',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    `order_count` INT NOT NULL DEFAULT 0 COMMENT '完成订单数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_title` (`title`),
    FULLTEXT KEY `ft_title_desc` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能货架表';

-- ============================================
-- 4. 需求悬赏表
-- ============================================
CREATE TABLE `bounty` (
    `id` BIGINT NOT NULL COMMENT '悬赏ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '悬赏标题',
    `description` TEXT COMMENT '需求描述',
    `reward` INT NOT NULL COMMENT '悬赏金额（时间币）',
    `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1已发布 2已接单 3已完成 4已过期',
    `applicant_id` BIGINT DEFAULT NULL COMMENT '接单人用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求悬赏表';

-- ============================================
-- 5. 悬赏申请表
-- ============================================
CREATE TABLE `bounty_application` (
    `id` BIGINT NOT NULL COMMENT '申请ID',
    `bounty_id` BIGINT NOT NULL COMMENT '悬赏ID',
    `applicant_id` BIGINT NOT NULL COMMENT '申请人用户ID',
    `message` VARCHAR(500) DEFAULT NULL COMMENT '申请留言',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1待确认 2已接受 3已拒绝',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bounty_applicant` (`bounty_id`, `applicant_id`),
    KEY `idx_bounty_id` (`bounty_id`),
    KEY `idx_applicant_id` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='悬赏申请表';

-- ============================================
-- 6. 订单表
-- ============================================
CREATE TABLE `skill_order` (
    `id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号，唯一',
    `buyer_id` BIGINT NOT NULL COMMENT '买方用户ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖方用户ID',
    `skill_id` BIGINT DEFAULT NULL COMMENT '关联技能ID',
    `bounty_id` BIGINT DEFAULT NULL COMMENT '关联悬赏ID（可选）',
    `amount` INT NOT NULL COMMENT '订单金额（时间币）',
    `frozen_amount` INT NOT NULL DEFAULT 0 COMMENT '冻结金额',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1待确认 2进行中 3待确认完成 4已完成 5已取消',
    `buyer_confirm` TINYINT NOT NULL DEFAULT 0 COMMENT '买方确认完成: 0未确认 1已确认',
    `seller_confirm` TINYINT NOT NULL DEFAULT 0 COMMENT '卖方确认完成: 0未确认 1已确认',
    `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 7. 时间币流水表
-- ============================================
CREATE TABLE `time_transaction` (
    `id` BIGINT NOT NULL COMMENT '流水ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: INCOME/EXPENSE/FREEZE/UNFREEZE/GIFT',
    `amount` INT NOT NULL COMMENT '金额（时间币）',
    `balance_after` INT NOT NULL COMMENT '交易后余额',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间币流水表';

-- ============================================
-- 8. 评价表（双盲评价）
-- ============================================
CREATE TABLE `review` (
    `id` BIGINT NOT NULL COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '评价人用户ID',
    `target_id` BIGINT NOT NULL COMMENT '被评价人用户ID',
    `score` TINYINT NOT NULL COMMENT '综合评分 1-5',
    `punctuality_score` TINYINT NOT NULL DEFAULT 5 COMMENT '按时 1-5',
    `communication_score` TINYINT NOT NULL DEFAULT 5 COMMENT '沟通 1-5',
    `professional_score` TINYINT NOT NULL DEFAULT 5 COMMENT '专业 1-5',
    `attitude_score` TINYINT NOT NULL DEFAULT 5 COMMENT '态度 1-5',
    `comment` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `is_visible` TINYINT NOT NULL DEFAULT 0 COMMENT '是否可见: 0不可见(双盲) 1可见',
    `visible_time` DATETIME DEFAULT NULL COMMENT '可见时间（创建后7天）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_reviewer` (`order_id`, `reviewer_id`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_visible` (`is_visible`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表（双盲评价）';

-- ============================================
-- 9. 通知表
-- ============================================
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` VARCHAR(30) NOT NULL COMMENT '通知类型: ORDER/SYSTEM/REVIEW/BOUNTY',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
    `target_id` BIGINT DEFAULT NULL COMMENT '关联目标ID',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================
-- 10. 申诉表
-- ============================================
CREATE TABLE `appeal` (
    `id` BIGINT NOT NULL COMMENT '申诉ID',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `user_id` BIGINT NOT NULL COMMENT '申诉人用户ID',
    `reason` VARCHAR(500) NOT NULL COMMENT '申诉原因',
    `evidence` TEXT DEFAULT NULL COMMENT '证据描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1待处理 2处理中 3已处理',
    `result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
    `handled_by` BIGINT DEFAULT NULL COMMENT '处理人（管理员ID）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申诉表';

-- ============================================
-- 11. 公告表
-- ============================================
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `publisher_id` BIGINT NOT NULL COMMENT '发布人（管理员ID）',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0否 1是',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '阅读量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_publisher` (`publisher_id`),
    KEY `idx_top_time` (`is_top`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ============================================
-- 12. 用户关注表
-- ============================================
CREATE TABLE `user_follow` (
    `id` BIGINT NOT NULL COMMENT '关注ID',
    `follower_id` BIGINT NOT NULL COMMENT '关注者用户ID',
    `following_id` BIGINT NOT NULL COMMENT '被关注者用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- ============================================
-- 13. 订单聊天消息表
-- ============================================
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL COMMENT '消息ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送人用户ID',
    `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `message_type` VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/FILE',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_sender` (`sender_id`),
    KEY `idx_order_time` (`order_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单聊天消息表';

-- ============================================
-- 14. 用户技能标签表（雷达图维度数据）
-- ============================================
CREATE TABLE `user_skill_tag` (
    `id` BIGINT NOT NULL COMMENT '标签ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tag_name` VARCHAR(30) NOT NULL COMMENT '标签名称（按时/沟通/专业/态度）',
    `score` DECIMAL(2,1) NOT NULL DEFAULT 5.0 COMMENT '评分 1.0-5.0',
    `review_count` INT NOT NULL DEFAULT 0 COMMENT '评价次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tag` (`user_id`, `tag_name`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户技能标签表（雷达图维度）';

-- ============================================
-- 15. 私信消息表
-- ============================================
CREATE TABLE `private_message` (
    `id` BIGINT NOT NULL COMMENT '消息ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认技能分类
INSERT INTO skill_category (name, icon, sort_order) VALUES
('编程开发', 'code', 1),
('设计创意', 'design', 2),
('语言学习', 'language', 3),
('音乐艺术', 'music', 4),
('运动健身', 'sport', 5),
('学术辅导', 'academic', 6),
('生活技能', 'life', 7),
('职场咨询', 'career', 8);

-- 插入管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO user (id, username, password, email, role, balance, bio) VALUES
(1, 'admin', '$2a$10$NOC9Loma7cBqsM4.rdpA8ukPRVmhn7Hnfr35Dkz3beIHhPzMyVviG', 'admin@prometheus.com', 'ADMIN', 9999, '系统管理员');

-- 插入测试用户 (密码: 123456)
INSERT INTO user (id, username, password, email, role, balance, bio) VALUES
(2, 'testuser', '$2a$10$nt81daPSNAW0rfMR72Z8XOJb6PfELzUzwApfiIa3Yhb0drL2Y8pjG', 'test@qq.com', 'USER', 20, '测试用户，体验时间银行');

-- 新人礼包流水
INSERT INTO time_transaction (id, user_id, order_id, type, amount, balance_after, remark) VALUES
(1, 2, NULL, 'GIFT', 20, 20, '注册新人礼包：赠送20时间币');
