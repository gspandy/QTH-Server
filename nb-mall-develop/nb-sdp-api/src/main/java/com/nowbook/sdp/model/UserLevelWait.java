package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class UserLevelWait extends PageModel{
    private static final long serialVersionUID = 6404288790255635091L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Integer level;//目标等级

    @Getter
    @Setter
    private Integer isSelect;//类型 1：只查询是否提交过申请 2：正常提交

    @Getter
    @Setter
    private Integer type;//类型 1：缴费 2：推荐 

    @Getter
    @Setter
    private Long money;//所需金钱

    @Getter
    @Setter
    private Integer num;//所需人数

    @Getter
    @Setter
    private Integer status;//状态 1：等待升级 2：升级成功 3：升级失败

    @Getter
    @Setter
    private String payCode;//流水号

    @Getter
    @Setter
    private Integer payType;//支付方式

    @Getter
    @Setter
    private String reason;//原因

    @Getter
    @Setter
    private String userIdListString;//升级到合伙人时需要选的3个人

    @Getter
    @Setter
    private List<Long> userIdList;//升级到合伙人时需要选的3个人

    @Getter
    @Setter
    private Long balance;//线下升级时的余额
    @Getter
    @Setter
    private Long advance;//线下升级时的预存款
    @Getter
    @Setter
    private Long deposit;//线下升级时的保证金
}
