-- -----------------------------------------------------
-- Table `amount_detail`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `amount_detail`;

CREATE TABLE `amount_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_id` varchar(64) DEFAULT NULL COMMENT '订单id',
  `get_amount` double DEFAULT NULL COMMENT '获得佣金金额',
  `distributors_id` bigint(20) DEFAULT NULL COMMENT '分销商',
  `is_complete` varchar(2) DEFAULT NULL COMMENT '是否完成分成 0 未分成 1已分成',
  PRIMARY KEY (`id`)
);