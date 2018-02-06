package com.nowbook.web.controller.api.userEvent;

import com.nowbook.common.model.Response;
import com.nowbook.common.utils.JsonMapper;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.trade.service.OrderQueryService;
import com.nowbook.trade.service.OrdersPopularizeService;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Author: yjgsjone@163.com
 */
@Slf4j
@Component
public class PaySuccessEventListener {

    private final UserEventBus eventBus;

    private final OrdersPopularizeService ordersPopularizeService;

    private final OrderQueryService orderQueryService;

    private final static String YIQIFA_CPS_COOKIE_NAME = "yiqifaCps";

    public final static JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Autowired
    public PaySuccessEventListener(UserEventBus eventBus, OrdersPopularizeService ordersPopularizeService, OrderQueryService orderQueryService) {
        this.eventBus = eventBus;
        this.ordersPopularizeService = ordersPopularizeService;
        this.orderQueryService = orderQueryService;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    /**
     * 向亿起发推送订单状态信息
     * @param paySuccessEvent
     */
    @Subscribe
    @SuppressWarnings("unused")
    public void sentOrderStatusToYQF(PaySuccessEvent paySuccessEvent) {
        try {

            HttpServletRequest request = paySuccessEvent.getRequest();
            Cookie[] cookies = request.getCookies();

            String tradeNos = request.getParameter("out_trade_no");
            List<String> identities = Splitter.on(",").splitToList(tradeNos);
            Set<Long> ids = convertToLong(identities);

            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                // 检查Cookie中是否包含亿起发推广信息
                if (Objects.equal(cookieName, YIQIFA_CPS_COOKIE_NAME)) {
                    String cookieValue = cookie.getValue();
                    Map mapCookie = JSON_MAPPER.fromJson(cookieValue, Map.class);

                    for (Long id : ids) {
                        Response<List<OrderItem>> listResponse = orderQueryService.findSubsByOrderId(id);
                        Response<Order> orderResponse = orderQueryService.findById(id);
                        if (listResponse.isSuccess() && orderResponse.isSuccess()) {
                            List<OrderItem> orderItemList = listResponse.getResult();
                            Order order = orderResponse.getResult();
                            for (OrderItem orderItem:orderItemList) {
                                // 订单id
                                mapCookie.put("orderId", id);
                                // 子订单id
                                mapCookie.put("orderSubId", orderItem.getId());
                                ordersPopularizeService.sendYqfOrderStatus(mapCookie, orderItem, order);
                            }
                        }
                    }
                }

            }

        } catch (IllegalStateException e) {
            log.error("fail to send data to yiqifa with registerEvent:{}, error:{}",
                    paySuccessEvent, e.getMessage());
        } catch (Exception e) {
            log.error("fail to send data to yiqifa with registerEvent:{}, error:{}",
                    paySuccessEvent, Throwables.getStackTraceAsString(e));
        }
    }

    private Set<Long> convertToLong(List<String> identities) {
        Set<Long> ids = Sets.newHashSet();
        for (String identity : identities) {
            ids.add(Long.valueOf(identity));
        }
        return ids;
    }

}
