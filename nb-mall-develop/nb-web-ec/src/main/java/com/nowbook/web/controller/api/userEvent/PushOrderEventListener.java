package com.nowbook.web.controller.api.userEvent;

import com.google.common.eventbus.Subscribe;
import com.nowbook.notice.model.Notice;
import com.nowbook.notice.service.NoticeService;
import com.nowbook.trade.dto.RichOrderItem;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.trade.model.OrderLogisticsInfo;
import com.nowbook.trade.model.UserTradeInfo;
import com.nowbook.trade.service.OrderLogisticsInfoService;
import com.nowbook.trade.service.OrderQueryService;
import com.nowbook.trade.service.UserTradeInfoService;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.nowbook.web.components.Express100;
import com.nowbook.web.controller.api.enums.NoticeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dpzh
 * @create 2017-07-27 14:32
 * @description:<类文件描述>
 **/

@Slf4j
@Component
public class PushOrderEventListener {

    private final PushOrderEventBus eventBus;
    //订单查询服务
    private final OrderQueryService orderQueryService;
    //帐户服务
    private final AccountService<User> accountService;
    //订单物流信息服务
    private final OrderLogisticsInfoService orderLogisticsInfoService;
    //用户交易信息服务
    private UserTradeInfoService userTradeInfoService;
    //通知消息存储服务
    private NoticeService noticeService;

    @Autowired
    public PushOrderEventListener(PushOrderEventBus eventBus,
                                  OrderQueryService orderQueryService,
                                  AccountService<User> accountService,
                                  OrderLogisticsInfoService orderLogisticsInfoService,
                                  UserTradeInfoService userTradeInfoService,
                                  NoticeService noticeService

                                                    ) {
        this.eventBus = eventBus;
        this.orderQueryService = orderQueryService;
        this.accountService = accountService;
        this.orderLogisticsInfoService = orderLogisticsInfoService;
        this.userTradeInfoService = userTradeInfoService;
        this.noticeService=noticeService;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    @Subscribe
    public void sendPushContent(PushEvent pushEvent) {
        String type=pushEvent.getType();
        if((NoticeType.ORDER_SHIPPED.getNoticeType()).equals(type)){
            Order order=orderQueryService.findById(pushEvent.getOrderId()).getResult();
            UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
            User user=accountService.findUserById(order.getBuyerId()).getResult();
            OrderLogisticsInfo orderLogisticsInfo=orderLogisticsInfoService.findByOrderId(pushEvent.getOrderId()).getResult();
            if(isUserStatus(String.valueOf(order.getBuyerId()),"1")) {
                //String smsContent="亲爱的【"+user.getName()+"】，您的订单【"+smsEvent.getOrderId()+"】已发货";
                // smsContent="，为您安排【"+orderLogisticsInfo.getExpressName()+"】（快递单号：【"+orderLogisticsInfo.getExpressNo()+"】）送至府上，请检查后签收。";
                String pushContent = "亲爱的钱唐家人，您的订单【" + pushEvent.getOrderId() + "】已发货";
                if (orderLogisticsInfo != null) {
                    if (orderLogisticsInfo.getType().toString().equals("0")) {
                        pushContent = pushContent + "，为您安排【" + orderLogisticsInfo.getExpressName() + "】（快递单号：【" + orderLogisticsInfo.getExpressNo() + "】）送至府上，请检查后签收。";
                        Express100 express100=new Express100();
                        express100.subscriptionExpress(orderLogisticsInfo.getExpressCode(),orderLogisticsInfo.getExpressNo());
                    } else if (orderLogisticsInfo.getType().toString().equals("1")) {
                        pushContent = "亲爱的钱唐家人，您的订单【" + pushEvent.getOrderId() + "】已发货";
                    }
                }
                sendPush(pushContent,pushEvent.getOrderId(),order.getBuyerId(),0);
            }
        }else if((NoticeType.ORDER_APPLY_REFUND.getNoticeType()).equals(type)){
            OrderItem orderItem=orderQueryService.findOrderItemById(pushEvent.getOrderItemId()).getResult();
            Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
            UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
            long buyerId=orderItem.getBuyerId();
            User user=accountService.findUserById(buyerId).getResult();
            if(isUserStatus(String.valueOf(buyerId),"1")) {
                //sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getName()+"】，您的退款申请已审核通过，请留意您的账户信息。");
                String pushContent = "亲爱的钱唐家人，您的退款申请已审核通过，请留意您的账户信息。";
                sendPush(pushContent, pushEvent.getOrderId(), order.getBuyerId(),2);
            }
        }else if((NoticeType.ORDER_RETURNED_PURCHASE.getNoticeType()).equals(type)){
            OrderItem orderItem=orderQueryService.findOrderItemById(pushEvent.getOrderItemId()).getResult();
            long buyerId=orderItem.getBuyerId();
            User user=accountService.findUserById(buyerId).getResult();
            Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
            UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
            if(isUserStatus(String.valueOf(buyerId),"1")) {
                //sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getName()+"】，您的退货申请已审核通过，请留意您的账户信息。");
                String pushContent = "亲爱的钱唐家人，您的退货申请已审核通过，请留意您的账户信息。";
                sendPush(pushContent, pushEvent.getOrderId(), order.getBuyerId(),2);
            }
        }else if(NoticeType.ORDER_APPLY_REFUND_NOT_PASS.getNoticeType().equals(type)){
            OrderItem orderItem=orderQueryService.findOrderItemById(pushEvent.getOrderItemId()).getResult();
            long buyerId=orderItem.getBuyerId();
            User user=accountService.findUserById(buyerId).getResult();
            Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
            UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
            if(isUserStatus(String.valueOf(buyerId),"1")) {
                //退款申请被拒绝
                String pushContent = "亲爱的钱唐家人，您的退款申请被拒绝。";
                sendPush(pushContent, pushEvent.getOrderId(), order.getBuyerId(),2);
            }
        }else if(NoticeType.ORDER_RETURNED_PURCHASE_NOT_PASS.getNoticeType().equals(type)){
            OrderItem orderItem=orderQueryService.findOrderItemById(pushEvent.getOrderItemId()).getResult();
            long buyerId=orderItem.getBuyerId();
            Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
            UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getTradeInfoId()).getResult();
            if(isUserStatus(String.valueOf(buyerId),"1")) {
                //退货申请被拒绝
                String pushContent = "亲爱的钱唐家人，你的退货申请被拒绝。";
                sendPush(pushContent, pushEvent.getOrderId(), order.getBuyerId(),2);
            }
        }


    }
    //发出推送，将信息存入数据库
    private void sendPush(String content,Long orderId,Long buyerId,Integer type){
        Notice notice=new Notice();
        notice.setFromUser(0l);
        notice.setBusinessId(orderId);
        notice.setToUser(buyerId);
        notice.setContent(content);
        notice.setType(type);
        if(notice.getType()==0){
            BaseUser baseUser=new BaseUser();
            baseUser.setId(notice.getToUser());
            List<RichOrderItem> itemList=orderQueryService.findOrderItemsByOrderIdForComment(notice.getBusinessId(),baseUser).getResult();
            if(itemList.size()>0){
                String itemImage=itemList.get(0).getItemImage();
                Integer itemSize=itemList.size();
                notice.setItemImage(itemImage);
                notice.setItemSize(itemSize);
            }
            notice.setSubType(0);
        }

        //将通知消息存入数据库
        Long noticeId= noticeService.create(notice);
        if(noticeId!=0){
            try {
                noticeService.pushNotice(notice);
            } catch (Exception e) {
                log.error("Push notification failed", e);
            }
        }
    }

    private boolean isUserStatus(String userId,String userType){
        boolean flog=false;
        Map<String, Object> map=new HashMap<String, Object>();
        map.put("userType", userType);
        map.put("userId", userId);
        flog=orderQueryService.isUserStatus(map);
        return flog;
    }

}
