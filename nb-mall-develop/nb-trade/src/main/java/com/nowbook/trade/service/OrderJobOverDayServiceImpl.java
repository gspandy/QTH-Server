package com.nowbook.trade.service;

import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.trade.dao.OrderJobDayDao;
import com.nowbook.trade.model.OrderJobOverDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-12-02
 */
@Service
public class OrderJobOverDayServiceImpl implements OrderJobOverDayService {

    private final static Logger log = LoggerFactory.getLogger(OrderJobOverDayServiceImpl.class);

    @Autowired
    private OrderJobDayDao orderJobDayDao;

    @Override
    public Response<Paging<OrderJobOverDay>> findByOrderIds(List<Long> orderIds) {
        Response<Paging<OrderJobOverDay>> result = new Response<Paging<OrderJobOverDay>>();
        if (orderIds.isEmpty()) {
            log.warn("ids is empty, return directly");
            return result;
        }
        try {
            Paging<OrderJobOverDay> orderJobOverDayPaging = orderJobDayDao.findByOrderIds(orderIds);
            result.setResult(orderJobOverDayPaging);
        } catch (Exception e) {
            log.error("failed to find OrderJobOverDay by orderIds, cause:", e);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }

        return result;
    }

    @Override
    public Response<Paging<OrderJobOverDay>> findBy(OrderJobOverDay orderJobOverDay) {
        Response<Paging<OrderJobOverDay>> result = new Response<Paging<OrderJobOverDay>>();

        try {
            Paging<OrderJobOverDay> orderJobOverDayPaging = orderJobDayDao.findBy(orderJobOverDay);
            result.setResult(orderJobOverDayPaging);

        } catch (Exception e) {
            log.error("failed to find OrderJobOverDay by criteria, cause:", e);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }

        return result;
    }

    @Override
    public Response<Boolean> updateStatusByOrderIds(List<Long> orderIds) {
        Response<Boolean> result = new Response<Boolean>();

        try {

            orderJobDayDao.updateByOrderIds(orderIds);
            result.setResult(true);

        } catch (Exception e) {
            log.error("failed to update OrderJobOverDay by orderIds:{} , cause:", orderIds, e);
            result.setResult(false);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }

        return result;
    }

    @Override
    public Response<Boolean> create(OrderJobOverDay orderJobOverDay) {

        Response<Boolean> result = new Response<Boolean>();

        try {

            Long orderJobDayId = orderJobDayDao.create(orderJobOverDay);
            result.setResult(true);

        } catch (Exception e) {
            log.error("failed to update OrderJobOverDay by orderJobDayId:{} , cause:", orderJobOverDay, e);
            result.setResult(false);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }


        return result;
    }

    @Override
    public Response<Boolean> update(OrderJobOverDay orderJobOverDay) {
        Response<Boolean> result = new Response<Boolean>();

        try {

            orderJobDayDao.update(orderJobOverDay);
            result.setResult(true);

        } catch (Exception e) {
            log.error("failed to update OrderJobOverDay by orderJobDayId:{} , cause:", orderJobOverDay, e);
            result.setResult(false);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }


        return result;
    }
}
