package com.nowbook.trade.dto;

import com.nowbook.coupons.model.NbCouOrderItem;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-28
 */
public class RichOrderSellerView extends RichOrder implements Serializable {
    private static final long serialVersionUID = -7200184251253134847L;

    @Getter
    @Setter
    private Long sellerId;

    @Getter
    @Setter
    private String sellerName;

    @Getter
    @Setter
    private Long buyerId;

    @Getter
    @Setter
    private String buyerName;

    @Getter
    @Setter
    private String mobile;

    @Getter
    @Setter
    private Boolean canDeliver; //是否能进行发货操作

    //add by zf 2014-08-22
    @Getter
    @Setter
    private List<NbCouOrderItem> couOrderList; //优惠券

}
