mysqldump: [Warning] Using a password on the command line interface can be insecure.
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: prometheus_skill_bank
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `prometheus_skill_bank`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `prometheus_skill_bank` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `prometheus_skill_bank`;

--
-- Table structure for table `announcement`
--

DROP TABLE IF EXISTS `announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `publisher_id` bigint NOT NULL COMMENT '发布人（管理员ID）',
  `is_top` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶: 0否 1是',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '阅读量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_publisher` (`publisher_id`),
  KEY `idx_top_time` (`is_top`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement`
--

LOCK TABLES `announcement` WRITE;
/*!40000 ALTER TABLE `announcement` DISABLE KEYS */;
/*!40000 ALTER TABLE `announcement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `appeal`
--

DROP TABLE IF EXISTS `appeal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appeal` (
  `id` bigint NOT NULL COMMENT '申诉ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `user_id` bigint NOT NULL COMMENT '申诉人用户ID',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申诉原因',
  `evidence` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '证据描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 1待处理 2处理中 3已处理',
  `result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理结果',
  `handled_by` bigint DEFAULT NULL COMMENT '处理人（管理员ID）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申诉表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appeal`
--

LOCK TABLES `appeal` WRITE;
/*!40000 ALTER TABLE `appeal` DISABLE KEYS */;
/*!40000 ALTER TABLE `appeal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bounty`
--

DROP TABLE IF EXISTS `bounty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bounty` (
  `id` bigint NOT NULL COMMENT '悬赏ID',
  `user_id` bigint NOT NULL COMMENT '发布者用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '悬赏标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '需求描述',
  `reward` int NOT NULL COMMENT '悬赏金额（时间币）',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 1已发布 2已接单 3已完成 4已过期',
  `applicant_id` bigint DEFAULT NULL COMMENT '接单人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求悬赏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bounty`
--

LOCK TABLES `bounty` WRITE;
/*!40000 ALTER TABLE `bounty` DISABLE KEYS */;
/*!40000 ALTER TABLE `bounty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bounty_application`
--

DROP TABLE IF EXISTS `bounty_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bounty_application` (
  `id` bigint NOT NULL COMMENT '申请ID',
  `bounty_id` bigint NOT NULL COMMENT '悬赏ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人用户ID',
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申请留言',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 1待确认 2已接受 3已拒绝',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bounty_applicant` (`bounty_id`,`applicant_id`),
  KEY `idx_bounty_id` (`bounty_id`),
  KEY `idx_applicant_id` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='悬赏申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bounty_application`
--

LOCK TABLES `bounty_application` WRITE;
/*!40000 ALTER TABLE `bounty_application` DISABLE KEYS */;
/*!40000 ALTER TABLE `bounty_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL COMMENT '消息ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `sender_id` bigint NOT NULL COMMENT '发送人用户ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/FILE',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读: 0未读 1已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_order_time` (`order_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单聊天消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型: ORDER/SYSTEM/REVIEW/BOUNTY',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知内容',
  `target_id` bigint DEFAULT NULL COMMENT '关联目标ID',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读: 0未读 1已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` bigint NOT NULL COMMENT '评价ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `reviewer_id` bigint NOT NULL COMMENT '评价人用户ID',
  `target_id` bigint NOT NULL COMMENT '被评价人用户ID',
  `score` tinyint NOT NULL COMMENT '综合评分 1-5',
  `punctuality_score` tinyint NOT NULL DEFAULT '5' COMMENT '按时 1-5',
  `communication_score` tinyint NOT NULL DEFAULT '5' COMMENT '沟通 1-5',
  `professional_score` tinyint NOT NULL DEFAULT '5' COMMENT '专业 1-5',
  `attitude_score` tinyint NOT NULL DEFAULT '5' COMMENT '态度 1-5',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评价内容',
  `is_visible` tinyint NOT NULL DEFAULT '0' COMMENT '是否可见: 0不可见(双盲) 1可见',
  `visible_time` datetime DEFAULT NULL COMMENT '可见时间（创建后7天）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_reviewer` (`order_id`,`reviewer_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_visible` (`is_visible`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表（双盲评价）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skill`
--

DROP TABLE IF EXISTS `skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `skill` (
  `id` bigint NOT NULL COMMENT '技能ID',
  `user_id` bigint NOT NULL COMMENT '发布者用户ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '技能描述',
  `price` int NOT NULL COMMENT '定价（时间币/小时）',
  `available_time` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '可用时间段描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0下架 1上架',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
  `order_count` int NOT NULL DEFAULT '0' COMMENT '完成订单数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_title` (`title`),
  FULLTEXT KEY `ft_title_desc` (`title`,`description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能货架表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skill`
--

LOCK TABLES `skill` WRITE;
/*!40000 ALTER TABLE `skill` DISABLE KEYS */;
/*!40000 ALTER TABLE `skill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skill_category`
--

DROP TABLE IF EXISTS `skill_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `skill_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类图标URL',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序，越小越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skill_category`
--

LOCK TABLES `skill_category` WRITE;
/*!40000 ALTER TABLE `skill_category` DISABLE KEYS */;
INSERT INTO `skill_category` VALUES (1,'编程开发','code',1,'2026-05-05 13:38:41','2026-05-05 14:10:13'),(2,'设计创意','design',2,'2026-05-05 13:38:41','2026-05-05 14:10:13'),(3,'语言学习','language',3,'2026-05-05 13:38:41','2026-05-05 14:10:13'),(4,'音乐艺术','music',4,'2026-05-05 13:38:41','2026-05-05 14:10:13'),(5,'运动健身','sport',5,'2026-05-05 13:38:41','2026-05-05 14:10:13'),(6,'学术辅导','academic',6,'2026-05-05 13:38:41','2026-05-05 14:10:13'),(7,'生活技能','life',7,'2026-05-05 13:38:41','2026-05-05 14:10:13'),(8,'职场咨询','career',8,'2026-05-05 13:38:41','2026-05-05 14:10:13');
/*!40000 ALTER TABLE `skill_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skill_order`
--

DROP TABLE IF EXISTS `skill_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `skill_order` (
  `id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号，唯一',
  `buyer_id` bigint NOT NULL COMMENT '买方用户ID',
  `seller_id` bigint NOT NULL COMMENT '卖方用户ID',
  `skill_id` bigint DEFAULT NULL COMMENT '关联技能ID',
  `bounty_id` bigint DEFAULT NULL COMMENT '关联悬赏ID（可选）',
  `amount` int NOT NULL COMMENT '订单金额（时间币）',
  `frozen_amount` int NOT NULL DEFAULT '0' COMMENT '冻结金额',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 1待确认 2进行中 3待确认完成 4已完成 5已取消',
  `buyer_confirm` tinyint NOT NULL DEFAULT '0' COMMENT '买方确认完成: 0未确认 1已确认',
  `seller_confirm` tinyint NOT NULL DEFAULT '0' COMMENT '卖方确认完成: 0未确认 1已确认',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skill_order`
--

LOCK TABLES `skill_order` WRITE;
/*!40000 ALTER TABLE `skill_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `skill_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `time_transaction`
--

DROP TABLE IF EXISTS `time_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `time_transaction` (
  `id` bigint NOT NULL COMMENT '流水ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: INCOME/EXPENSE/FREEZE/UNFREEZE/GIFT',
  `amount` int NOT NULL COMMENT '金额（时间币）',
  `balance_after` int NOT NULL COMMENT '交易后余额',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间币流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `time_transaction`
--

LOCK TABLES `time_transaction` WRITE;
/*!40000 ALTER TABLE `time_transaction` DISABLE KEYS */;
INSERT INTO `time_transaction` VALUES (1,2,NULL,'GIFT',20,20,'注册新人礼包：赠送20时间币','2026-05-05 13:38:41');
/*!40000 ALTER TABLE `time_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '用户ID，雪花算法',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名，唯一',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码，BCrypt加密',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0禁用 1正常',
  `balance` int NOT NULL DEFAULT '0' COMMENT '时间币余额',
  `frozen_balance` int NOT NULL DEFAULT '0' COMMENT '冻结时间币',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个人简介',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$NOC9Loma7cBqsM4.rdpA8ukPRVmhn7Hnfr35Dkz3beIHhPzMyVviG','admin@prometheus.com',NULL,NULL,'ADMIN',1,9999,0,'系统管理员','2026-05-05 16:09:13','2026-05-05 16:10:23'),(2,'testuser','$2a$10$nt81daPSNAW0rfMR72Z8XOJb6PfELzUzwApfiIa3Yhb0drL2Y8pjG','test@qq.com',NULL,NULL,'USER',1,20,0,'测试用户','2026-05-05 16:09:13','2026-05-05 16:11:09'),(2051544692229984258,'test99','$2a$10$Wuqtn0qVkaDZ8HJnp1kVFOjIS1QUM8OukeZdb1v5EAUbqZS2QY/r6','test99@test.com',NULL,NULL,'USER',1,100,0,NULL,'2026-05-05 14:09:05','2026-05-05 14:09:05'),(2051553856297664514,'yykxyyds','$2a$10$/WctNxZPGIxnEkwgQ8JQSe.TMAZz0kzq4u7bnFUln.tiuJ4O7BLp2','17889882185@163.com','',NULL,'USER',1,100,0,'大家好，我是郑朴的小弟','2026-05-05 14:45:30','2026-05-05 14:45:30');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_follow`
--

DROP TABLE IF EXISTS `user_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` bigint NOT NULL COMMENT '关注ID',
  `follower_id` bigint NOT NULL COMMENT '关注者用户ID',
  `following_id` bigint NOT NULL COMMENT '被关注者用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`,`following_id`),
  KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_follow`
--

LOCK TABLES `user_follow` WRITE;
/*!40000 ALTER TABLE `user_follow` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_follow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_skill_tag`
--

DROP TABLE IF EXISTS `user_skill_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_skill_tag` (
  `id` bigint NOT NULL COMMENT '标签ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称（按时/沟通/专业/态度）',
  `score` decimal(2,1) NOT NULL DEFAULT '5.0' COMMENT '评分 1.0-5.0',
  `review_count` int NOT NULL DEFAULT '0' COMMENT '评价次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`,`tag_name`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户技能标签表（雷达图维度）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_skill_tag`
--

LOCK TABLES `user_skill_tag` WRITE;
/*!40000 ALTER TABLE `user_skill_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_skill_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'prometheus_skill_bank'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-05 16:12:11
