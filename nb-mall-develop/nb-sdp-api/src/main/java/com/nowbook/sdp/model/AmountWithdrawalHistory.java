package com.nowbook.sdp.model;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

//佣金明细表
public class AmountWithdrawalHistory extends PageModel{
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private Long distributorsId;//分销商

    @Getter
    @Setter
    private Date operationTime;//操作日期
    @Getter
    @Setter
    private String money;//
    @Getter
    @Setter
    private String payType;//
    @Getter
    @Setter
    private String bankAccountNumber;//
    @Getter
    @Setter
    private String bankSerialNumber;//
    @Getter
    @Setter
    private Date bankSysTime;//操作日期

    @Getter
    @Setter
    private DistributionInfo distributionInfo;//分销商bean


}