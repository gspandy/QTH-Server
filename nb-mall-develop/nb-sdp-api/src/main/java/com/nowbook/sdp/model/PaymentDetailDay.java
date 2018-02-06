package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Created by Administrator on 2016/9/6.
 * 给用户打款明细类
 */
@ToString
public class PaymentDetailDay extends PageModel {
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String idNo; //编号

    @Getter
    @Setter
    private Long fee;     //周结算累计

    @Getter
    @Setter
    private Double commissionRate;   //第三方手续费率

    @Getter
    @Setter
    private Long commissionFee;   //第三方手续费

    @Getter
    @Setter
    private Long totalFee;   //周结算

    @Getter
    @Setter
    private Integer totalNum;     //总笔数

    @Getter
    @Setter
    private Integer successNum;     //成功笔数

    @Getter
    @Setter
    private Integer failNum;     //失败笔数

    @Setter
    @Getter
    private Date createStartAt;

    @Setter
    @Getter
    private Date createEndAt;
}
