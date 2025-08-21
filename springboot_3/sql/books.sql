/*
 Navicat Premium Dump SQL

 Source Server         : demo
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44-log)
 Source Host           : localhost:3306
 Source Schema         : books

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44-log)
 File Encoding         : 65001

 Date: 17/08/2025 19:49:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NOT NULL,
  `num` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book
-- ----------------------------
INSERT INTO `book` VALUES (7, '三国11', 29.90, 100);
INSERT INTO `book` VALUES (8, '麦田里的守望者', 29.90, 100);
INSERT INTO `book` VALUES (10, '或者', 25.90, 100);
INSERT INTO `book` VALUES (18, '活着', 25.90, 100);
INSERT INTO `book` VALUES (19, '123', 25.90, 500);
INSERT INTO `book` VALUES (22, '三国1', 29.90, 100);
INSERT INTO `book` VALUES (23, '1111', 11.00, 123);
INSERT INTO `book` VALUES (26, 'a', 29.90, 100);
INSERT INTO `book` VALUES (29, 'aaa', 29.90, 100);
INSERT INTO `book` VALUES (30, '1111', 11.00, 123);
INSERT INTO `book` VALUES (31, '下班', 29.90, 100);
INSERT INTO `book` VALUES (32, '班', 29.90, 100);
INSERT INTO `book` VALUES (33, '11111', 29.90, 100);
INSERT INTO `book` VALUES (34, '1211', 29.90, 100);
INSERT INTO `book` VALUES (35, '12888', 29.90, 100);

SET FOREIGN_KEY_CHECKS = 1;
