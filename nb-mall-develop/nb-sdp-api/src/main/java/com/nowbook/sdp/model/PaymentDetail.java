package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2016/9/6.
 * 给用户打款明细类
 */
@ToString
public class PaymentDetail extends PageModel {
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String idNo; //编号

    @Getter
    @Setter
    private Long userId;     //用户Id

    @Getter
    @Setter
    private Long money;   //金额

    @Getter
    @Setter
    private Long earnings;   //收入部分

    @Getter
    @Setter
    private Long bonuses;   //奖金部分

    @Getter
    @Setter
    private Long deposit;   //保证金扣除部分

    @Getter
    @Setter
    private Long payMoney;     //支付方式 1：支付宝 2：微信 3：银联

    @Getter
    @Setter
    private Integer payType;     //支付方式 1：支付宝 2：微信 3：银联

    @Getter
    @Setter
    private String payId;   //打款账号

    @Getter
    @Setter
    private String payName;   //打款姓名

    @Getter
    @Setter
    private Integer payStatus;      //打款状态：1:未路过，2：已路过

    @Getter
    @Setter
    private String payCode;      //打款流水号

    @Getter
    @Setter
    private Integer payResult;      //打款状态：1:成功，2：失败

    @Getter
    @Setter
    private String reason;      //失败原因

    @Getter
    @Setter
    private Date payAt;    //打款时间

    @Getter
    @Setter
    private Date createStartAt;

    @Getter
    @Setter
    private Date createEndAt;

    @Getter
    @Setter
    private Date payStartAt;

    @Getter
    @Setter
    private Date payEndAt;

    @Getter
    @Setter
    private String mobile;    // 用户手机号
}
