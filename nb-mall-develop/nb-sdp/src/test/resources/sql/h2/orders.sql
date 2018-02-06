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

 Date: 04/29/2016 20:39:15 PM
*/


-- ----------------------------
--  Table structure for `orders`
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `origin_id` bigint(20) DEFAULT NULL COMMENT '原订单Id',
  `buyer_id` bigint(20) DEFAULT NULL COMMENT '买家id',
  `seller_id` bigint(20) DEFAULT NULL COMMENT '卖家id',
  `business` int(11) DEFAULT NULL,
  `status` smallint(6) DEFAULT NULL COMMENT '订单状态 0:等待买家付款,1:买家已付款,2:卖家已发货,3:交易完成,-1:买家关闭,-2:卖家关闭,-3:卖家退款',
  `type` smallint(6) DEFAULT NULL COMMENT '交易类型',
  `trade_info_id` bigint(20) DEFAULT NULL COMMENT '买家收货信息',
  `deliver_fee` int(11) DEFAULT NULL COMMENT '邮费',
  `payment_type` smallint(6) DEFAULT NULL COMMENT '付款类型',
  `payment_code` varchar(32) DEFAULT NULL COMMENT '付款账户',
  `fee` int(11) DEFAULT NULL COMMENT '订单总价',
  `channel` varchar(64) DEFAULT NULL COMMENT '支付渠道',
  `is_buying` bit(1) DEFAULT NULL COMMENT '是否抢购订单',
  `outer_code` varchar(32) DEFAULT NULL COMMENT '商家88码,冗余',
  `paid_at` datetime DEFAULT NULL COMMENT '付款时间',
  `delivered_at` datetime DEFAULT NULL COMMENT '发货时间',
  `done_at` datetime DEFAULT NULL COMMENT '完成时间',
  `canceled_at` datetime DEFAULT NULL COMMENT '交易关闭时间',
  `finished_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ;
