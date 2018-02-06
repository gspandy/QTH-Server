/*
 Navicat Premium Data Transfer

 Source Server         : jlkj_mall
 Source Server Type    : MySQL
 Source Server Version : 50173
 Source Host           : 120.27.50.31
 Source Database       : e_shops

 Target Server Type    : MySQL
 Target Server Version : 50173
 File Encoding         : utf-8

 Date: 04/29/2016 20:41:01 PM
*/


-- ----------------------------
--  Table structure for `distributors`
-- ----------------------------
DROP TABLE IF EXISTS `distributors`;
CREATE TABLE `distributors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `shop_name` varchar(64) DEFAULT NULL COMMENT '店铺名称',
  `open_shop_time` datetime DEFAULT NULL COMMENT '开店时间',
  `open_status` varchar(2) DEFAULT NULL COMMENT '店铺状态',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上级id',
  PRIMARY KEY (`id`)
) ;
