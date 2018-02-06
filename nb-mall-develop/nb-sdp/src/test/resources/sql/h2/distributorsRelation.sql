-- -----------------------------------------------------
-- Table `distributors_relation`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `distributors_relation`;

CREATE TABLE `distributors_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `distributors_id` bigint(20) NOT NULL COMMENT '分销商id',
  `parent_ids` varchar(2000) NOT NULL COMMENT '分销商所有父级编号',
  `distribution_level` varchar(2) DEFAULT '0' COMMENT '分销商等级',
  PRIMARY KEY (`id`)
);