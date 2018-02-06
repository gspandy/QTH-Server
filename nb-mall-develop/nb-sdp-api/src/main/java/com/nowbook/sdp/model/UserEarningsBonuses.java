package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class UserEarningsBonuses extends PageModel{

    private static final long serialVersionUID = 6404288790255635091L;

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long userId;//用户ID

    @Setter
    @Getter
    private Long fromId;//来自ID

    @Setter
    @Getter
    private Integer type;//类型 同userWalletSummary里的

    @Setter
    @Getter
    private Long orderItemId;//订单id

    @Setter
    @Getter
    private Long money;//钱数

    @Setter
    @Getter
    private Integer moneyType;//类型，1：收益，2：奖金

    @Setter
    @Getter
    private Integer status;//状态 1：待结算 2：已结算

    @Setter
    @Getter
    private String itemName;//商品名

    @Setter
    @Getter
    private Double fee;//订单金额

    @Setter
    @Getter
    private String orderId;//订单号

    @Setter
    @Getter
    private String theStatus;//订单状态

    @Setter
    @Getter
    private String mobile;//手机号

    @Setter
    @Getter
    private Date startAt;//结算时间

    @Setter
    @Getter
    private Date endAt;//结算时间

}