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

 Date: 04/29/2016 21:22:21 PM
*/


-- ----------------------------
--  Table structure for `amount_withdrawal_history`
-- ----------------------------
DROP TABLE IF EXISTS `amount_withdrawal_history`;
CREATE TABLE `amount_withdrawal_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `distributors_id` bigint(20) DEFAULT NULL COMMENT '分销商',
  `operation_time` datetime DEFAULT NULL COMMENT '操作日期',
  `withdrawal_information` varchar(256) DEFAULT NULL COMMENT '提现信息',
  `amount_status` varchar(2) DEFAULT NULL COMMENT '佣金状态  0 未支付 1 已支付',
  PRIMARY KEY (`id`)
) ;
