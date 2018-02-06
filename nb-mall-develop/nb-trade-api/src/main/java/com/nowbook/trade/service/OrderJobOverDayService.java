package com.nowbook.trade.service;

import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.trade.model.OrderJobOverDay;

import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-10
 */
public interface OrderJobOverDayService {

    /**
     * 根据id列表查找订单列表
     * @param orderIds 订单列表
     * @return  订单列表
     */
    public Response<Paging<OrderJobOverDay>> findByOrderIds(List<Long> orderIds);

    /**
     * 根据id列表查找订单列表
     * @param orderJobOverDay 检索对象
     * @return  订单列表
     */
    public Response<Paging<OrderJobOverDay>> findBy(OrderJobOverDay orderJobOverDay);

    /**
     * 更新Order状态
     */
    Response<Boolean> updateStatusByOrderIds(List<Long> orderIds);

    /**
     * 创建订单Job状态表
     */
    Response<Boolean> create(OrderJobOverDay orderJobOverDay);

    /**
     * 更新订单Job状态表
     */
    Response<Boolean> update(OrderJobOverDay orderJobOverDay);

}
