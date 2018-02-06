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
DROP TABLE IF EXISTS `order_sdp`;
create table order_sdp
(
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
   order_id             bigint(20),
   distributor_id       bigint(20),
   primary key (id)
);

