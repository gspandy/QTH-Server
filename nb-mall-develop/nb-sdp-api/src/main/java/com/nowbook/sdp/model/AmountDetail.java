package com.nowbook.sdp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
public class AmountDetail extends PageModel{
    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private String orderId;//订单号

    @Setter
    @Getter
    private String orderItemId;//订单号

    @Setter
    @Getter
    private Integer getAmount;//获得佣金金额

    @Setter
    @Getter
    private Long distributorsId;//分销商

    @Setter
    @Getter
    private String isComplete;//是否完成分成 0 未分成 1已分成

    @Setter
    @Getter
    private String  fromDistributorsId;

    @Setter
    @Getter
    private DistributionInfo distributionInfo;//分销商bean

    @Setter
    @Getter
    private Date operTime;
}