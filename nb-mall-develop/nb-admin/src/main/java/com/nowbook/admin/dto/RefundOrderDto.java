package com.nowbook.admin.dto;

import com.nowbook.shop.model.Shop;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.user.model.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-22 10:06 AM  <br>
 * Author:cheng
 */
@ToString
public class RefundOrderDto implements Serializable {

    private static final long serialVersionUID = -4425237663941448446L;

    @Getter
    @Setter
    private Order order;                        // 订单信息

    @Getter
    @Setter
    private List<OrderItem> orderItems;         // 子订单信息

    @Getter
    @Setter
    private User buyer;                         // 买家信息

    @Getter
    @Setter
    private User seller;                        // 卖家信息

    @Getter
    @Setter
    private Shop shop;                          // 店铺信息

}
