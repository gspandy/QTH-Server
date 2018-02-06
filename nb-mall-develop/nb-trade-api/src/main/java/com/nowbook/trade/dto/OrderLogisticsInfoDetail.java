package com.nowbook.trade.dto;

import com.nowbook.trade.model.OrderLogisticsInfo;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * 订单物流详细
 * Author: haolin
 * On: 9/25/14
 */
public class OrderLogisticsInfoDetail implements Serializable {

    @Getter @Setter
    private OrderLogisticsInfo orderLogisticsInfo;          //订单物流信息

    @Getter @Setter
    private String expressDetail;                           //快递详细信息, 物流为第三方快递时

    @Getter @Setter
    private Boolean isSepcial;                              //是否是特殊快递公司, true时expressDetail为快递信息url

    @Getter @Setter
    private String message;
}
