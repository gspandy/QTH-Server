package com.nowbook.sdp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

//佣金明细表
@ToString
@EqualsAndHashCode
public class AmountWithdrawalHistoryForQuery {
    @Setter
    @Getter
    private Long id;
    @Setter
    @Getter
    private String shopName;//
    @Setter
    @Getter
    private Date operationTime;//



}