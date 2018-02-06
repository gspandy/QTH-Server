-- -----------------------------------------------------
-- Table `amount_withdrawal_history`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `amount_withdrawal_history`;

CREATE TABLE IF NOT EXISTS `amount_withdrawal_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `distributors_id` BIGINT NULL COMMENT '分销商',
  `operation_time` DATETIME NULL COMMENT '操作日期',
  `withdrawal_information` VARCHAR(256) NULL COMMENT '提现信息',
  `amount_status` VARCHAR(2) NULL COMMENT '佣金状态  0 未支付 1 已支付',
  PRIMARY KEY (`id`));