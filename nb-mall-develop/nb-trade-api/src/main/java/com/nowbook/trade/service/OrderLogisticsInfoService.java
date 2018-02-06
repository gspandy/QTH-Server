package com.nowbook.trade.service;

import com.nowbook.common.model.Response;
import com.nowbook.trade.model.OrderLogisticsInfo;

/**
 * 订单物流信息服务
 * Author: haolin
 * On: 9/23/14
 */
public interface OrderLogisticsInfoService {

    /**
     * 查询订单的物流信息
     * @param orderId 订单id
     * @return 订单的物流信息
     */
    Response<OrderLogisticsInfo> findByOrderId(Long orderId);
}
