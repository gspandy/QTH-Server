package com.nowbook.trade.service;

import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.trade.model.OrderJobDayConfig;

import java.util.Map;

/**
 * Author:  wangmeng
 * Date: 2014-11-19
 */
public interface OrderJobOverDayConfigService {

    /**
     * 插入skuId设定表
     *
     * @param orderJobDayConfig sku设定对象
     * @return  订单列表
     */
    public Response<Boolean> create(OrderJobDayConfig orderJobDayConfig);

    /**
     * 根据skuId查找设定天数
     * @param skuId
     * @return  sku设定类
     */
    public Response<OrderJobDayConfig> findBySku(Long skuId);

    public Response<Paging<OrderJobDayConfig>> findBy(Map<String, Object> params);
}
