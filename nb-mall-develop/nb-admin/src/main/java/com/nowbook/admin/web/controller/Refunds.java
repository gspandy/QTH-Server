package com.nowbook.admin.web.controller;

import com.nowbook.admin.dto.RefundOrderDto;
import com.nowbook.alipay.dto.AlipayRefundData;
import com.nowbook.alipay.request.CallBack;
import com.nowbook.alipay.request.RefundRequest;
import com.nowbook.alipay.request.Token;
import com.nowbook.common.model.Response;
import com.nowbook.rlt.settle.model.AlipayTrans;
import com.nowbook.rlt.settle.service.SettlementService;
import com.nowbook.shop.model.Shop;
import com.nowbook.shop.service.ShopService;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.trade.service.OrderQueryService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.nowbook.web.misc.MessageSources;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.nowbook.rlt.settle.util.SettlementVerification.isPlain;
import static com.nowbook.user.util.UserVerification.isAdmin;
import static com.nowbook.user.util.UserVerification.isFinance;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-22 10:04 AM  <br>
 * Author:cheng
 */
@Slf4j
@Controller
@RequestMapping("/api/admin/refund")
public class Refunds {

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private Token token;

    @Value("#{app.alipayRefundSuffix}")
    private String notifyUrl;

    @RequestMapping(value = "/{orderId}/view", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<RefundOrderDto> view(@PathVariable(value = "orderId") Long orderId) {

        Response<RefundOrderDto> result = new Response<RefundOrderDto>();

        try {
            checkAdminOrFinancePrivilege();

            Order order = getOrder(orderId);

            RefundOrderDto refundOrderDto = new RefundOrderDto();
            refundOrderDto.setOrder(order);

            Response<List<OrderItem>> orderItemResponse = orderQueryService.findOrderItemByOrderId(order.getId());
            checkState(orderItemResponse.isSuccess(), orderItemResponse.getError());
            List<OrderItem> orderItems = orderItemResponse.getResult();
            refundOrderDto.setOrderItems(orderItems);

            User buyer = getUser(order.getBuyerId());
            refundOrderDto.setBuyer(buyer);
            User seller = getUser(order.getSellerId());
            refundOrderDto.setSeller(seller);

            Shop shop = getShopOf(seller.getId());
            refundOrderDto.setShop(shop);

            result.setResult(refundOrderDto);

        } catch (IllegalArgumentException e) {
            log.error("fail to query refundOrder with orderId:{}, error:{}", orderId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query refundOrder with orderId:{}, error:{}", orderId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query refundOrder with orderId:{}, cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.refund.view.fail"));
        }

        return result;
    }


    private User getUser(Long userId) {
        Response<User> userQueryResult = accountService.findUserById(userId);
        checkState(userQueryResult.isSuccess(), userQueryResult.getError());
        return userQueryResult.getResult();
    }

    private Shop getShopOf(Long userId) {
        Response<Shop> shopQueryResult = shopService.findByUserId(userId);
        checkState(shopQueryResult.isSuccess(), shopQueryResult.getError());
        return shopQueryResult.getResult();
    }


    private Order getOrder(Long orderId) {
        Response<Order> orderQueryResult = orderQueryService.findById(orderId);
        checkState(orderQueryResult.isSuccess(), orderQueryResult.getError());
        return orderQueryResult.getResult();
    }

    private OrderItem getOrderItem(Long orderItemId) {
        Response<OrderItem> orderItemQueryResult = orderQueryService.findOrderItemById(orderItemId);
        checkState(orderItemQueryResult.isSuccess(), orderItemQueryResult.getError());
        return orderItemQueryResult.getResult();
    }


    @RequestMapping(value = "/{orderId}/items/{orderItemId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<AlipayTrans>> viewTrans(@PathVariable(value = "orderId") Long orderId,
                                                 @PathVariable(value = "orderItemId") Long orderItemId) {
        Response<List<AlipayTrans>> result = new Response<List<AlipayTrans>>();

        try {
            checkAdminOrFinancePrivilege();

            Order order = getOrder(orderId);
            String merchantNo;

            if (isPlain(order)) {
                merchantNo = orderId.toString();
            } else {
                merchantNo = orderId + "," + orderItemId;
            }

            Response<List<AlipayTrans>> transResult = settlementService.findAlipayTransByMerchantNo(merchantNo);
            checkState(transResult.isSuccess(), transResult.getError());
            result.setResult(transResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to query alipayTrans with orderId:{}, orderItemId:{}, error:{}", orderId, orderItemId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query alipayTrans with orderId:{}, orderItemId:{}, error:{}", orderId, orderItemId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query alipayTrans with orderId:{}, orderItemId:{}, cause:{}", orderId, orderItemId, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.refund.view.fail"));
        }

        return result;
    }


    @RequestMapping(value = "/{orderItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> refund(@PathVariable("orderItemId") Long orderItemId,
                                    @RequestParam("paymentCode") String paymentCode,
                                    @RequestParam("amount") Integer amount) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            checkAdminOrFinancePrivilege();

            OrderItem orderItem = getOrderItem(orderItemId);
            checkState(amount <= orderItem.getFee(), "refund.amount.greater.than.fee");

            String batchNo = RefundRequest.toBatchNo(DateTime.now().toDate(), orderItem.getId());
            AlipayRefundData refund = new AlipayRefundData(paymentCode,
                    amount, "财务审核退款");

            CallBack notify = new CallBack("http://beta.nowbook.com/api/alipay/refund/record");
            Response<Boolean> refundByAlipay = RefundRequest.build(token).batch(batchNo)
                    .detail(Lists.newArrayList(refund)).notify(notify).refund();
            checkState(refundByAlipay.isSuccess(), refundByAlipay.getError());
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to refund with orderItemId:{}, amount:{}, cause:{}", orderItemId, amount, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to refund with orderItemId:{}, amount:{}, cause:{}", orderItemId, amount, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to refund with orderItemId:{}, amount:{}, cause:{}", orderItemId, amount, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.refund.fail"));
        }

        return result;
    }


    private void checkAdminOrFinancePrivilege() {
        BaseUser user = UserUtil.getCurrentUser();
        checkArgument(isAdmin(user) || isFinance(user), "user.has.no.permission");
    }


}
