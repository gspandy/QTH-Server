package com.nowbook.trade.dto;

import com.nowbook.sdp.model.AmountDetail;
import com.nowbook.sdp.model.DistributionInfoForQuery;
import com.google.common.base.Objects;
import com.nowbook.coupons.model.NbCouOrderItem;
import com.nowbook.trade.model.UserTradeInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-11
 */
public class RichOrderBuyerView extends RichOrder implements Serializable {

    private static final long serialVersionUID = -6508586529830576828L;
    @Getter
    @Setter
    private Long shopId;


    @Getter
    @Setter
    private String siteName;

    @Getter
    @Setter
    private Long siteId;

    @Getter
    @Setter
    private String shopImage;

    @Getter
    @Setter
    private Boolean canConfirm; //是否能进行确认收货操作

    @Getter
    @Setter
    private Boolean canComment; // 是否能评论订单，不持久化



    @Getter
    @Setter
    private List<NbCouOrderItem> couponList; // 优惠券使用情况

    @Getter
    @Setter
    private String systemDate; // 当前系统时间

    @Getter
    @Setter
    private Boolean buyingCanPay;   //如果是抢购订单是否可以付款
    @Getter
    @Setter
    private Integer deliverType;     //配送方式：0 物流配送 1 到店自提

    @Getter
    @Setter
    private DistributionInfoForQuery distributionInfo;

    @Getter
    @Setter
    private AmountDetail amountDetail;

    @Getter
    @Setter
    private UserTradeInfo userTradeInfo;

    @Getter
    @Setter
    private Integer isBalance;
    @Getter
    @Setter
    private String ntalkerId; //企业编号，用来调用客服

    @Override
    public int hashCode() {
        return Objects.hashCode(siteId, orderItems);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof RichOrderBuyerView)) {
            return false;
        }
        RichOrderBuyerView that = (RichOrderBuyerView) o;
        return Objects.equal(this.siteId, that.siteId) && Objects.equal(this.orderItems, that.orderItems);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("sellerId", siteId).add("sellerName", siteName)
                .add("orderItems", orderItems).omitNullValues().toString();
    }

}
