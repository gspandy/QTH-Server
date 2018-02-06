package com.nowbook.restful.controller;

import com.google.common.base.*;
import com.nowbook.alipay.Bank;
import com.nowbook.alipay.exception.BankNotFoundException;
import com.nowbook.alipay.request.CallBack;
import com.nowbook.alipay.request.PayRequest;
import com.nowbook.alipay.request.Token;
import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.JsonMapper;
import com.nowbook.exception.JsonResponseException;
import com.nowbook.item.dto.FullItem;
import com.nowbook.item.model.Sku;
import com.nowbook.item.service.ItemService;
import com.nowbook.restful.controller.testPostOrder.bin.demo.src.demo.HttpRequest;
import com.nowbook.restful.controller.testPostOrder.bin.demo.src.util.MD5;
import com.nowbook.restful.dto.NbResponse;
import com.nowbook.restful.util.NSSessionUID;
import com.nowbook.restful.util.Signatures;
import com.nowbook.rlt.code.model.ActivityBind;
import com.nowbook.rlt.code.model.ActivityCode;
import com.nowbook.rlt.code.model.ActivityDefinition;
import com.nowbook.rlt.code.service.ActivityBindService;
import com.nowbook.rlt.code.service.ActivityCodeService;
import com.nowbook.rlt.code.service.ActivityDefinitionService;
import com.nowbook.rlt.presale.dto.PreOrderPreSale;
import com.nowbook.rlt.presale.service.PreSaleService;
import com.nowbook.rlt.settle.service.SettlementService;
import com.nowbook.session.AFSession;
import com.nowbook.session.AFSessionManager;
import com.nowbook.trade.dto.*;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderComment;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.trade.model.OrderLogisticsInfo;
import com.nowbook.trade.service.*;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.base.UserUtil;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.nowbook.web.components.Express100;
import com.nowbook.web.misc.MessageSources;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nowbook.common.utils.Arguments.*;
import static com.nowbook.rlt.settle.util.SettlementVerification.needSettlementAfterSuccess;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-19 1:19 PM  <br>
 * Author:cheng
 */
@Controller
@Slf4j
@RequestMapping("/api/extend/order")
public class NSOrders {

    private final static JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();

    private final static JavaType javaType= jsonMapper.createCollectionType(
            ArrayList.class, OrderComment.class);


    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private OrderCommentService orderCommentService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private ActivityDefinitionService activityDefinitionService;

    @Autowired
    private ActivityBindService activityBindService;

    @Autowired
    private ActivityCodeService activityCodeService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private Token token;

    @Autowired
    private OrderLogisticsInfoService orderLogisticsInfoService;

    @Autowired
    private Express100 express100;

    @Value("#{app.alipayNotifySuffix}")
    private String notifyUrl;

    @Value("#{app.restkey}")
    private String key;

    @Value(value = "#{app.express100Key}")
    private String express100Key;

    @Value(value = "#{app.customer}")
    private String customer;


    private final AFSessionManager sessionManager = AFSessionManager.instance();

    private static final Splitter splitter = Splitter.on(" ").trimResults().omitEmptyStrings();


    private OrderDescription getOrderDescription(List<Long> ids) {
        try {

            Response<OrderDescription> descQueryResult = orderQueryService.getDescriptionOfOrders(ids);
            checkState(descQueryResult.isSuccess(), descQueryResult.getError());
            return descQueryResult.getResult();

        } catch (Exception e) {
            log.warn("fail to get order desc", e);
            return new OrderDescription();
        }

    }


    /**
     * 支付接口
     * @param orderId           订单号, 必填
//     * @param sessionId         会话id, 必填
     * @param forwardUrl        前台跳转页面, 必填
     * @param title             商品标题, 必填
     * @param bank              如果要使用银行网关，需要传入银行的code, 选填, 默认支付宝
     * @param qr                是否启用二维码支付，选填, 默认不启用
//     * @param uid               用户id, 必填
    //     * @param channel           渠道, 必填
    //     * @param sign              签名, 必填
     * @return                  支付宝即时到帐网关URL
     */
    @RequestMapping(value = "/pay", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private NbResponse<String> pay(@RequestParam("orderId") Long orderId,
//                                      @RequestParam("session") String sessionId,
                                      @RequestParam("forward") String forwardUrl,
                                      @RequestParam(value = "title", defaultValue = "订单", required = false) String title,
                                      @RequestParam(value = "bank", required = false) String bank,
                                      @RequestParam(value = "qr", required = false) Boolean qr,
//                                      @RequestParam("channel") String channel,
//                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {

        NbResponse<String> result = new NbResponse<String>();
        BaseUser baseUser=new BaseUser();
        try {
            checkArgument(notEmpty(forwardUrl), "forward.can.not.be.empty");
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            Preconditions.checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> uidGetResult = NSSessionUID.checkLogin(session, uid);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            baseUser=UserUtil.getCurrentUser();

            Response<Order> orderGetResult = orderQueryService.findById(orderId);
            checkState(orderGetResult.isSuccess(), orderGetResult.getError());
            Order order = orderGetResult.getResult();
            checkState(equalWith(order.getBuyerId(),baseUser.getId()), "order.not.belong.to.current.user");


            // 构建支付宝请求
            CallBack notify = new CallBack(notifyUrl);
            // 前台通知地址
            CallBack forward = new CallBack(forwardUrl);
            String tradeNo = order.getId() + "";



            OrderDescription description = getOrderDescription(Lists.newArrayList(order.getId()));


            PayRequest payRequest = PayRequest.build(token).title(description.getTitle())
                    .content(description.getContent())
                    .outerTradeNo(tradeNo).total(order.getFee())
                    .notify(notify).forward(forward);

            if (notNull(qr) && qr) {
                payRequest.enableQrCode();
            }

            if (!Strings.isNullOrEmpty(bank)) {
                try {
                    payRequest.defaultBank(Bank.from(bank));
                } catch (BankNotFoundException e) {
                    // ignore
                }
            }

            result.setResult(payRequest.url());

        } catch (IllegalArgumentException e) {
            log.error("fail to create alipay url with orderId:{}, title:{}, bank:{},, error:{}",
                    orderId, title, bank,  e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to create alipay url with orderId:{}, title:{}, bank:{}, error:{}",
                    orderId, title, bank,  e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to create alipay url with orderId:{}, title:{}, bank:{}",
                    orderId, title, bank, e);
            result.setError(messageSources.get("order.pay.fail"));
        }

        return result;
    }

    /**
     * 添加商品评价
     *
     * @param orderId            订单Id, 必填
     * @param json          json 字符串形势的评价对象, 必填
//     * @param uid           用户的id, 必填
//     * @param sessionId     会话id, 必填
    //     * @param channel       渠道, 必填
    //     * @param sign          签名, 必填
     * @return              操作状态
     */
    @RequestMapping(value = "/comment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> comment( @RequestParam("orderId") Long orderId,
                                          @RequestParam("data") String json,
//                                          @RequestParam("uid") Long uid,
//                                          @RequestParam("session") String sessionId,
//                                          @RequestParam("channel") String channel,
//                                          @RequestParam("sign") String sign,
                                          HttpServletRequest request) {
        NbResponse<String> result = new NbResponse<String>();
        BaseUser baseUser=new BaseUser();
        try {
            checkArgument(notEmpty(json), "data.can.not.be.empty");
            List<OrderComment> comments = new ArrayList<OrderComment>();
            for(Object object:JSONArray.fromObject(json)){
                JSONObject jsonObject = JSONObject.fromObject(object);
                OrderComment orderComment = new OrderComment();
                orderComment.setComment(jsonObject.get("comment").toString());
                orderComment.setOrderItemId(Long.valueOf(jsonObject.get("orderItemId").toString()));
                orderComment.setRDescribe(Integer.valueOf(jsonObject.get("rDescribe").toString()));
                orderComment.setRExpress(Integer.valueOf(jsonObject.get("rExpress").toString()));
                orderComment.setRQuality(Integer.valueOf(jsonObject.get("rQuality").toString()));
                orderComment.setRService(Integer.valueOf(jsonObject.get("rService").toString()));
                comments.add(orderComment);
            }
            baseUser=UserUtil.getCurrentUser();
            checkArgument(comments.size() > 0, "item.comment.can.not.be.empty");

            Response<User> userGetResult =  accountService.findUserById(baseUser.getId());
            checkArgument(userGetResult.isSuccess(), userGetResult.getError());

            Response<List<Long>> commentCreateResult = orderCommentService.create(orderId, comments, userGetResult.getResult().getId());
            checkState(commentCreateResult.isSuccess(), commentCreateResult.getError());
            result.setResult("success");

        } catch (IllegalArgumentException e) {
            log.error("fail to create comment with itemId:{}, data:{}, userId:{}, error:{}",
                    orderId, json, baseUser.getId(), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to create comment with itemId:{}, data:{}, userId:{}, error:{}",
                    orderId, json, baseUser.getId(), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to create comment with itemId:{}, data:{}, userId:{}",
                    orderId, json, baseUser.getId(),  e);
            result.setError(messageSources.get("item.comment.create.fail"));
        }

        return result;
    }


    /**
     * 获取单个订单信息
     *
     * @param orderId       订单id, 必填
//     * @param uid           用户id, 必填
//     * @param sessionId     会话id, 必填
    //     * @param channel       渠道, 必填
    //     * @param sign          签名, 必填
     * @return 订单信息
     */
    @RequestMapping(value = "/detail", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<NbOrder> detail(@RequestParam("orderId") Long orderId,
//                                            @RequestParam("uid") Long uid,
//                                            @RequestParam("session") String sessionId,
//                                            @RequestParam("channel") String channel,
//                                            @RequestParam(value = "sign") String sign,
                                            HttpServletRequest request) {

        NbResponse<NbOrder> result = new NbResponse<NbOrder>();
        BaseUser baseUser=new BaseUser();
        try {

//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
//            checkState(idGet.isSuccess(), idGet.getError());

            Response<NbOrder> orderQueryResult = orderQueryService.findNbOrderById(orderId);
            checkState(orderQueryResult.isSuccess(), orderQueryResult.getError());
            NbOrder nbOrder = orderQueryResult.getResult();
//            baseUser=UserUtil.getCurrentUser();

//            checkState(equalWith(nbOrder.getBuyerId(),baseUser.getId()), "order.not.belong.to.current.user");

            result.setResult(nbOrder, key);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to query order detail with orderId:{}, userId:{},  error:{}",
                    orderId, baseUser.getId(), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query order detail with orderId:{}, userId:{},error:{}",
                    orderId, baseUser.getId(),  e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query order detail with orderId:{}, userId:{}",
                    orderId, baseUser.getId(), e);
            result.setError(messageSources.get("order.query.fail"));
        }

        return result;
    }



    /**
     * 获取下单信息(订单确认页)
     *
     * @param skus          sku的json字符串, 必填
//     * @param uid           用户id, 必填
//     * @param sessionId     会话id, 必填
    //     * @param channel       渠道, 必填
    //     * @param sign          签名, 必填
     * @return 订单信息
     */
    @RequestMapping(value = "/pre", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<PreOrder>> preOrder(@RequestParam(value = "skus") String skus,
//                                                  @RequestParam("uid") Long uid,
//                                                  @RequestParam("session") String sessionId,
//                                                  @RequestParam("channel") String channel,
//                                                  @RequestParam(value = "sign") String sign,
                                                  HttpServletRequest request) {
        NbResponse<List<PreOrder>> result = new NbResponse<List<PreOrder>>();
        BaseUser baseUser=new BaseUser();
        try {

            checkArgument(notEmpty(skus), "skus.can.not.be.empty");
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
//            checkState(idGet.isSuccess(), idGet.getError());


            baseUser= UserUtil.getCurrentUser();//获取当前登陆用户

            Response<List<PreOrder>> preQueryResult = cartService.preOrder(baseUser,skus);
            checkState(preQueryResult.isSuccess(), preQueryResult.getError());

            result.setResult(preQueryResult.getResult(), key);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to pre order detail with skus:{}, userId:{},error:{}",
                    skus, baseUser.getId(), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to pre order with skus:{}, userId:{}, error:{}",
                    skus,  baseUser.getId(),  e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to pre order with skus:{}, userId:{}",
                    skus,  baseUser.getId(),  e);
            result.setError(messageSources.get("order.query.fail"));
        }

        return result;
    }

    /**
     * 确认订单装状态，原始状态必须是卖家已发货
     *
     * @param oid           订单id, 必填
//     * @param sessionId     会话id, 必填
//     * @param channel       渠道, 必填
//     * @param sign          签名, 必填
     * @return              订单id
     */
    @RequestMapping(value = "/buyer/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> confirm(@RequestParam("orderId") Long oid,
//                                       @RequestParam("session") String sessionId,
//                                       @RequestParam("channel") String channel,
//                                       @RequestParam("sign") String sign,
                                       HttpServletRequest request) {
        NbResponse<String> result = new NbResponse<String>();
        BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");

            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            // 校验用户是否存在
//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
//            Long uid = uidGetResult.getResult();


            baseUser=UserUtil.getCurrentUser();
            Response<Order> orderGetResult = orderQueryService.findById(oid);
            checkState(orderGetResult.isSuccess(), orderGetResult.getError());
            Order order = orderGetResult.getResult();
            // 校验订单的所有权
            checkState(equalWith(order.getBuyerId(), baseUser.getId()), "order.not.belong.to.current.user");
            // 校验订单的状态
            checkState(equalWith(order.getStatus(), Order.Status.DELIVERED.value()), "order.status.incorrect");

            // 确认订单
            Response<Boolean> confirmedResult = orderWriteService.confirm(order, baseUser.getId());
            checkState(confirmedResult.isSuccess(), confirmedResult.getError());
            result.setResult("success");

        } catch (IllegalArgumentException e) {
            log.error("failed to confirm order with oid:{},  error:{}", oid, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("failed to confirm order with oid:{},  error:{}", oid, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to confirm order with oid:{}", oid, e);
            result.setError(messageSources.get("order.confirm.fail"));
        }

        return result;
    }

    /**
     * 卖家撤销订单，原始状态必须为待支付状态
     *
     * @param oid           订单id, 必填
//     * @param sessionId     会话id, 必填
//     * @param channel       渠道, 必填
//     * @param sign          签名, 必填
     * @return              订单id
     */
    @RequestMapping(value = "/buyer/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> cancel(@RequestParam("orderId") Long oid,
//                                      @RequestParam("session") String sessionId,
//                                      @RequestParam("channel") String channel,
//                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {
        NbResponse<String> result = new NbResponse<String>();
        BaseUser baseUser=new BaseUser();
        try {
//            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

//            AFSession session = new AFSession(sessionManager, request, sessionId);
//            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
//            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
//            Long uid = uidGetResult.getResult();

            baseUser=UserUtil.getCurrentUser();
            Long uid=baseUser.getId();
            Response<Order> orderGetResult = orderQueryService.findById(oid);
            checkState(orderGetResult.isSuccess(), orderGetResult.getError());
            Order order = orderGetResult.getResult();

            // 校验订单的所有权
            checkState(equalWith(order.getBuyerId(), uid), "order.not.belong.to.current.user");
            // 校验订单的状态
            checkState(equalWith(order.getStatus(), Order.Status.WAIT_FOR_PAY.value()), "order.status.incorrect");

            BaseUser buyer = new BaseUser();
            buyer.setId(uid);
            buyer.setTypeEnum(BaseUser.TYPE.BUYER);
            Response<Boolean> canceledResult = orderWriteService.cancelOrder(order, buyer);
            checkState(canceledResult.isSuccess(), canceledResult.getError());
            result.setResult("success");

        } catch (IllegalArgumentException e) {
            log.error("failed to cancel order id:{}, error:{}", oid, e);
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("failed to cancel order id:{},  error:{}", oid, e);
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to cancel order id:{}", oid, e);
            result.setError(messageSources.get("order.cancel.fail"));
        }

        return result;
    }

    /**
     * 预售下单预览页
     * @param skus 以json格式保存的Map<Long,Integer> key为skuId，value为sku数量
     * @param regionId 地区id
     */
    @RequestMapping(value = "/preSale/preOrder", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<PreOrderPreSale> preSalePreOrder(@RequestParam("skus") String skus,
                                                          @RequestParam("regionId") Integer regionId,
                                                          @RequestParam("channel") String channel,
                                                          @RequestParam("sign") String sign,
                                                          @RequestParam("session") String sessionId,
                                                          HttpServletRequest request) {
        NbResponse<PreOrderPreSale> result = new NbResponse<PreOrderPreSale>();

        try {

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            // 需要登录验证
            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<User> userR = accountService.findUserById(uidGetResult.getResult());
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={},error code={}",uidGetResult.getResult(),userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }
            User user = userR.getResult();

            Response<PreOrderPreSale> preOrderPreSaleR = preSaleService.preOrderPreSale(skus, regionId, user);
            if(!preOrderPreSaleR.isSuccess()) {
                log.error("fail to find preSale preOrder by skus={}, regionId={}, error code:{}",
                        skus, regionId, preOrderPreSaleR.getError());
                result.setError(messageSources.get(preOrderPreSaleR.getError()));
                return result;
            }

            result.setResult(preOrderPreSaleR.getResult());
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to find preSale preOrder by skus={},regionId={},channel={},sign={},cause:{}",
                    skus, regionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to find preSale preOrder by skus={},regionId={},channel={},sign={},cause:{}",
                    skus, regionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to find preSale preOrder by skus={},regionId={},channel={},sign={},cause:{}",
                    skus, regionId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.preOrder.fail"));
            return result;
        }
    }

    /**
     * 预售订单确认收货
     * @return 操作订单id
     */
    @RequestMapping(value = "/preSale/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> preSaleConfirm(@RequestParam("id") Long oid,
                                              @RequestParam("session") String sessionId,
                                              @RequestParam("channel") String channel,
                                              @RequestParam("sign") String sign,
                                              HttpServletRequest request) {
        NbResponse<Long> result = new NbResponse<Long>();

        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();

            Response<User> userR = accountService.findUserById(uid);
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", uid, userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }

            //这里不在做权限校验，因为service会校验
            Response<Order> orderR = orderQueryService.findById(oid);
            if(!orderR.isSuccess()) {
                log.error("fail to find order by id={}, error code;{}", oid, orderR.getError());
                result.setError(messageSources.get(orderR.getError()));
                return result;
            }

            Response<Boolean> orderUpdateR = orderWriteService.confirm(orderR.getResult(),uid);
            if(!orderUpdateR.isSuccess()) {
                log.error("fail to confirm order by id={}, buyerId={}, error code:{}",
                        oid, uid, orderUpdateR.getError());
                result.setError(messageSources.get(orderR.getError()));
                return result;
            }

            // 确认收货后需要创建结算信息
            try {
                Order confirmedOrder = getOrder(oid);
//                createSettlementAfterConfirm(confirmedOrder);
            } catch (IllegalStateException e) {
                log.error("fail to create settlement of Order(id:{}), code:{}", oid, e.getMessage());
            }


            result.setResult(oid);
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to confirm preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to confirm preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to confirm preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.confirm.fail"));
            return result;
        }
    }

    private Order getOrder(Long orderId) {
        Response<Order> orderR = orderQueryService.findById(orderId);
        checkState(orderR.isSuccess(), orderR.getError());
        return orderR.getResult();
    }


//    private void createSettlementAfterConfirm(Order order) {
//        //普通订单-货到付款-交易成功
//        if (needSettlementAfterSuccess(order)) {
//            Response<Long> createResult = settlementService.generate(order.getId());
//            checkState(createResult.isSuccess(), createResult.getError());
//        }
//    }

    /**
     * 取消预售订单,买家取消或者卖家取消
     * 操作订单id
     */
    @RequestMapping(value = "/preSale/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> preSaleCancel(@RequestParam("id") String id,
                                             @RequestParam("session") String sessionId,
//                                             @RequestParam("channel") String channel,
//                                             @RequestParam("sign") String sign,
                                             HttpServletRequest request) {
        NbResponse<Long> result = new NbResponse<Long>();
        Long oid = Long.valueOf(id);
        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.ve   rify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();

            Response<User> userR = accountService.findUserById(uid);
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", uid, userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }

            //这里不在做权限校验，因为service会校验
            Response<Order> orderR = orderQueryService.findById(oid);
            if(!orderR.isSuccess()) {
                log.error("fail to find order by id={}, error code;{}", oid, orderR.getError());
                result.setError(messageSources.get(orderR.getError()));
                return result;
            }

            Response<Boolean> updateR = orderWriteService.cancelOrder(orderR.getResult(),userR.getResult());
            if(!updateR.isSuccess()) {
                log.error("fail to cancel order orderId={}, userId={}, error code:{}",
                        oid, uid, updateR.getError());
                result.setError(messageSources.get(updateR.getError()));
                return result;
            }

            //从预售订单列表移除
            Response<Boolean> removeR = preSaleService.removePreSaleOrder(oid);
            if (!removeR.isSuccess()) {
                log.error("fail to remove preSale order id={} from list, error code:{}",
                        oid, removeR.getError());
            }

            //对于分仓的预售还要恢复库存
            Response<Boolean> storageR = preSaleService.recoverPreSaleStorageIfNecessary(oid);
            if(!storageR.isSuccess()){
                log.error("failed to recover storage for order(id={}), error code:{}", oid, storageR.getError());
            }

            //恢复预售购买限制
            Response<Boolean> preSaleBuyLimitR = preSaleService.recoverPreSaleBuyLimitIfNecessary(orderR.getResult());
            if(!preSaleBuyLimitR.isSuccess()) {
                log.error("failed to recover pre sale buy limit by order id={}, error code:{}",
                        orderR.getResult().getId(), preSaleBuyLimitR.getError());
            }

            result.setResult(oid);
            return result;

        }catch (IllegalStateException e) {
//            log.error("fail to cancel preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
//                    oid, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
//            log.error("fail to cancel preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
//                    oid, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
//            log.error("fail to cancel preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
//                    oid, sessionId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.cancel.fail"));
            return result;
        }
    }

    /**
     * 预售订单退货款
     * @return 操作子订单id
     */
    @RequestMapping(value = "/preSale/orderItem/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> preSaleOrderItemCancel(@RequestParam("orderItemId") Long orderItemId,
                                                      @RequestParam("session") String sessionId,
                                                      @RequestParam("channel") String channel,
                                                      @RequestParam("sign") String sign,
                                                      HttpServletRequest request) {
        NbResponse<Long> result = new NbResponse<Long>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();

            Response<User> userR = accountService.findUserById(uid);
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", uid, userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }

            Response<Boolean> updateR = orderWriteService.cancelOrderItem(orderItemId);
            if(!updateR.isSuccess()) {
                log.error("fail to cancel preSale orderItem id={}, error code:{}",orderItemId, updateR.getError());
                result.setError(messageSources.get(updateR.getError()));
                return result;
            }

            result.setResult(orderItemId);
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to cancel preSale orderItem id={}, session={}, channel={}, sign={}, cause:{}",
                    orderItemId, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to cancel preSale orderItem id={}, session={}, channel={}, sign={}, cause:{}",
                    orderItemId, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to cancel preSale orderItem id={}, session={}, channel={}, sign={}, cause:{}",
                    orderItemId, sessionId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("preSale.orderItem.refund.or.returnGood.fail"));
            return result;
        }
    }

    @RequestMapping(value = "/code/activities", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<List<ActivityDefinition>> getActivityByCode(@RequestParam("code") String code,
                                                                     @RequestParam("skuIds") String skuString,
                                                                     @RequestParam("session") String sessionId,
//                                                      @RequestParam("channel") String channel,
//                                                      @RequestParam("sign") String sign,
                                                                     HttpServletRequest request) {
        NbResponse<List<ActivityDefinition>> result = new NbResponse<List<ActivityDefinition>>();

        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
//            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
//            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
//
//            // 校验签名, 先注释方便调试
//            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<List<ActivityDefinition>> activityDefR = activityDefinitionService.findValidActivityDefinitionsByCode(code);
            if(!activityDefR.isSuccess() || activityDefR.getResult() == null) {
                log.error("fail to find activity def by code={}, error code={}",code, activityDefR.getError());
                result.setError(messageSources.get(activityDefR.getError()));
                return result;
            }
            List<ActivityDefinition> activityDefinitions = activityDefR.getResult();
            List<Long> skuIds = new ArrayList<Long>();
            for(String a :skuString.split(",")){
                skuIds.add(Long.valueOf(a));
            };
//            List<Long> skuIds = getSkuIds(skuString);

            Response<List<Sku>> skusR = itemService.findSkuByIds(skuIds);
            if(!skusR.isSuccess() || skusR.getResult() == null) {
                log.error("fail to find skus by ids={}, error code:{}", skuIds, skusR.getError());
                result.setError(messageSources.get(skusR.getError()));
                return result;
            }
            List<Sku> skus = skusR.getResult();
            List<Long> itemIds = Lists.transform(skus, new Function<Sku, Long>() {
                @Override
                public Long apply(Sku input) {
                    return input.getItemId();
                }
            });
            List<ActivityDefinition> filterActivityDef = Lists.newArrayList();
            for(ActivityDefinition activityDef : activityDefinitions) {
                Response<List<Long>> bindIdsR = activityBindService.findBindIdsByActivityId(activityDef.getId(), ActivityBind.TargetType.ITEM.toNumber());
                if(!bindIdsR.isSuccess()) {
                    log.error("fail to find bind ids by activityId={}, targetType=ITEM, error code:{}",
                            activityDef.getId(), bindIdsR.getError());
                    continue;
                }
                List<Long> bindIds = bindIdsR.getResult();
                List<Long> e = new ArrayList<Long>();
                for(Long id : bindIds) {
                    Response<Map<String, Object>> detailGetResult = itemService.findWithDetailsById(id);
                    FullItem c = (FullItem) detailGetResult.getResult().get("fullItem");
                    for(Sku d :c.getSkus()){
                        e.add(d.getId());
                    };
                }
                activityDef.setSkuId(e);
                //有适用商品，库存为空（不限制购买数量）或者库存大于0的才显示出来
                if(inUseRange(bindIds, itemIds) && (activityDef.getStock() == null || activityDef.getStock() > 0)) {
                    filterActivityDef.add(activityDef);
                }
            }

            result.setResult(filterActivityDef);
            return result;

        }catch (IllegalStateException e) {
//            log.error("fail to find activity by code={},skuString={},session={},channel={},sign={}, cause:{}",
//                    code,skuString,sessionId,channel,sign,e.getMessage());
//            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
//            log.error("fail to find activity by code={},skuString={},session={},channel={},sign={}, cause:{}",
//                    code,skuString,sessionId,channel,sign,e.getMessage());
//            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
//            log.error("fail to find activity by code={},skuString={},session={},channel={},sign={}, cause:{}",
//                    code,skuString,sessionId,channel,sign,Throwables.getStackTraceAsString(e));
//            result.setError(messageSources.get("activityDefinition.select.failed"));
            return result;
        }
    }

    @RequestMapping(value = "/sku/discount", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<Long> getSkuDiscount(@RequestParam("skuIds") String skuIds,
                                              @RequestParam("activityId") Long activityId,
                                              @RequestParam("code") String code,
                                              @RequestParam("session") String sessionId,
                                              @RequestParam("channel") String channel,
                                              @RequestParam("sign") String sign,
                                              HttpServletRequest request) {
        NbResponse<Long> result = new NbResponse<Long>();

        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Map<Long, Integer> skuMap = jsonMapper.fromJson(skuIds, jsonMapper.createCollectionType(Map.class, Long.class, Integer.class));
            Response<List<Sku>> skusR = itemService.findSkuByIds(Lists.newArrayList(skuMap.keySet()));
            if(!skusR.isSuccess()) {
                log.error("fail to find skus by ids={},error code:{}", skuIds, skusR.getError());
                result.setError(messageSources.get(skusR.getError()));
                return result;
            }

            Response<ActivityDefinition> activityDefinitionR = activityDefinitionService.findActivityDefinitionById(activityId);
            if(!activityDefinitionR.isSuccess()) {
                log.error("fail to find activity def by id={}, error code:{}", activityId, activityDefinitionR.getError());
                result.setError(messageSources.get(activityDefinitionR.getError()));
                return result;
            }
            ActivityDefinition activityDef = activityDefinitionR.getResult();

            Response<List<Long>> itemIdsR = activityBindService.findBindIdsByActivityId(activityId, ActivityBind.TargetType.ITEM.toNumber());
            if(!itemIdsR.isSuccess()) {
                log.error("fail to find bind ids by activityId={}, TargetType=ITEM, error code:{}",
                        activityId, itemIdsR.getError());
                result.setError(messageSources.get(itemIdsR.getError()));
                return result;
            }
            List<Long> itemIds = itemIdsR.getResult();

            Long totalDiscount = 0l;
            Integer totalToBuy = 0;

            for(Sku sku : skusR.getResult()) {
                int quantity = skuMap.get(sku.getId());

                //sku在活动范围之内,sku价格高于优惠的金额
                if(itemIds.contains(sku.getItemId()) && sku.getPrice() > activityDef.getDiscount()) {
                    totalDiscount += quantity * activityDef.getDiscount();
                    totalToBuy += quantity;
                }
            }
            //如果库存为空，不需要做数量的判断
            if(activityDef.getStock() == null) {
                result.setResult(totalDiscount);
                return result;
            }

            Response<ActivityCode> activityCodeR = activityCodeService.findOneByActivityIdAndCode(activityId, code);
            if(!activityCodeR.isSuccess()) {
                log.error("fail to find usage by activityId={}, code={}, error code:{}",
                        activityId, code, activityCodeR.getError());
                result.setError(messageSources.get(activityCodeR.getError()));
                return result;
            }
            Integer usage = activityCodeR.getResult().getUsage();

            if((usage + totalToBuy) > activityDef.getStock()) {
                log.warn("activityDef id={} has used {}, want to use {}", activityDef.getId(), usage, totalToBuy);
                result.setError(messageSources.get("stock.not.enough"));
                return result;
            }
            result.setResult(totalDiscount);
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to get sku discount by skuIds={}, activityId={}, code={}, session={},channel={},sign={},cause:{}",
                    skuIds,activityId,code,sessionId,channel,sign,e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to get sku discount by skuIds={}, activityId={}, code={}, session={},channel={},sign={},cause:{}",
                    skuIds,activityId,code,sessionId,channel,sign,e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to get sku discount by skuIds={}, activityId={}, code={}, session={},channel={},sign={},cause:{}",
                    skuIds,activityId,code,sessionId,channel,sign,Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("get.sku.discount.fail"));
            return result;
        }
    }

    private List<Long> getSkuIds(String skuString) {
        List<String> ids = splitter.splitToList(skuString);

        List<Long> skuIds = Lists.newArrayListWithCapacity(ids.size());
        for (String id : ids) {
            skuIds.add(Long.valueOf(id));
        }
        return skuIds;
    }

    private boolean inUseRange(List<Long> bindIds, List<Long> itemIds) {
        for(Long bindId : bindIds) {
            if(itemIds.contains(bindId))
                return true;
        }
        return false;
    }
    /**
     * 根据买家 ID 返回分页的评价
     *
     * @param pageNo 页数
     * @param size   每页几个
     * @param currentBuyer 自动注入当前的用户
     * @return 分页的详细订单评价
     */

    @RequestMapping(value = "/selectComment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Paging<RichOrderCommentForBuyer>> selectCommentById(@ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                        @ParamInfo("size") @Nullable Integer size,
                                                                        @ParamInfo("baseUser") @Nullable BaseUser currentBuyer) {

        Response<Paging<RichOrderCommentForBuyer>> result =  orderCommentService.findByBuyerId(pageNo,size,currentBuyer);


        return result;
    }
    /**
     * 根据买家 ID 返回未评价的订单
     *
     *
     * @param currentBuyer 自动注入当前的用户
     * @return 分页的详细订单评价
     */
    @RequestMapping(value = "/selectUnComment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RichOrderBuyerView> selectCommentById(@ParamInfo("baseUser") @Nullable BaseUser currentBuyer) {
        Response<Paging<RichOrderBuyerView>> ordersGet = orderQueryService.findByBuyerId(currentBuyer, 1, 100, null, null, null);
        List<RichOrderBuyerView> result = new ArrayList<RichOrderBuyerView>();
        for(RichOrderBuyerView order :ordersGet.getResult().getData()){
            if(order.getCanComment() == true){
                result.add(order);
            }
        }
        return result;
    }


    @RequestMapping(value = "/selectOrderFrom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String selectOrderFrom100 (@RequestParam("num") String num,
                                      @RequestParam("com") String com,
                                      HttpServletRequest request) {

        String param ="{\"com\":\""+com+"\",\"num\":\""+ num+"\"}";
        String key = express100Key;
        String sign = MD5.encode(param + key + customer);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("param",param);
        params.put("sign",sign);
        params.put("customer",customer);
        String resp = "";
        System.out.println(sign);
        try {
            resp = new HttpRequest().postData("http://poll.kuaidi100.com/poll/query.do", params, "utf-8").toString();
            System.out.println(resp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }



    /**
     * 根据子订单id找自订单
     * @param orderItemId 子订单id
     * @return 子订单
     */
    @RequestMapping(value = "/{orderItemId}/select", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<OrderItem> selectOrderItemId(@PathVariable(value = "orderItemId") Long orderItemId) {
        Response<OrderItem> result = orderQueryService.findOrderItemById(orderItemId);
        return result;
    }

    /**
     * 添加退货款理由和金额
     *
     * @param orderItemId  子订单id
     * @param reason       理由
     * @param refundAmount 金额
     * @return 是否成功
     */
    @RequestMapping(value = "/refund", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> addExtra( @RequestParam(value = "orderItemId") Long orderItemId,
                           @RequestParam(value = "reason") String reason,
                           @RequestParam(value = "refundAmount") Integer refundAmount) {
        NbResponse<String> results = new NbResponse<String>();
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.addReasonAndRefund(orderItemId, user, reason, refundAmount);
        if (!result.isSuccess()) {
            log.error("fail to add reason or refund for orderItemId(id={}) by user({}),cause:{}",
                    orderItemId, user, result.getError());
            results.setError("退款申请失败");
            return results;
        }
        try {
            OrderItem orderItem=orderQueryService.findOrderItemById(orderItemId).getResult();
            int status=orderItem.getStatus();
            int payType=orderItem.getPayType();
           /* long sellerId=orderItem.getSellerId();
            long buyerId=orderItem.getBuyerId();
            long orderId=orderItem.getOrderId();
            Shop shop=shopService.findByUserId(sellerId).getResult();
            User user1=accountService.findUserById(buyerId).getResult();
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");  */
            if(payType==1){
                if(status==4){
//                    smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"4"));
                	/*if(isUserStatus(String.valueOf(sellerId),"2")){
                    	sendSms(shop.getPhone(),"您有一项新的业务要处理，贵商铺订单【"+orderId+"】买家【"+user1.getMobile()+"】"
                    			+ "于【"+format.format(new Date())+"】申请退款，请尽快审核。");
                    }*/
                }else if(status==5){
//                    smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"5"));
                	/*if(isUserStatus(String.valueOf(sellerId),"2")){
                    	sendSms(shop.getPhone(),"您有一项新的业务要处理，贵商铺订单【"+orderId+"】买家【"+user1.getMobile()+"】于"
                    			+ "【"+format.format(new Date())+"】申请退货，请尽快审核。");
                    }*/
                }else{
                    log.error("fail to get sms status"+status);
                }
            }

        } catch (Exception e) {
            log.error("fail to get sms exception"+e.getMessage());
        }
        results.setResult("success");
        return results;
    }


    @RequestMapping(value = "/undo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<String> undo( @RequestParam(value = "orderItemId") Long orderItemId) {
        NbResponse<String> results = new NbResponse<String>();
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.undoRequest(orderItemId, user);
        if (!result.isSuccess()) {
            log.error("fail to undo refund or returnGoods for orderItem(id={}) by user({}),error code:{}",
                    orderItemId, user, result.getError());
            results.setError("false");
            return results;
        }
        results.setResult("success");
        return results;
    }


    /**
     * 查询订单物流信息
     * @param orderId 订单id
     * @return 订单物流信息id
     */
    @RequestMapping(value = "/logistics", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NbResponse<OrderLogisticsInfoDetail> findOrderLogisticsInfo(@RequestParam(value = "orderId") Long orderId){
        Response<OrderLogisticsInfo> resp = orderLogisticsInfoService.findByOrderId(orderId);
        NbResponse<OrderLogisticsInfoDetail> result=new NbResponse<OrderLogisticsInfoDetail>();
        if (!resp.isSuccess()){
            result.setError("订单信息暂未查到！");
            return result;
        }
        OrderLogisticsInfoDetail orderLogisticsInfoDetail = new OrderLogisticsInfoDetail();
        OrderLogisticsInfo orderLogisticsInfo = resp.getResult();
        orderLogisticsInfoDetail.setOrderLogisticsInfo(orderLogisticsInfo);

        if(orderLogisticsInfoDetail.getOrderLogisticsInfo() !=null){
            String com = orderLogisticsInfoDetail.getOrderLogisticsInfo().getExpressCode();
            String num = orderLogisticsInfoDetail.getOrderLogisticsInfo().getExpressNo();
            if(com !=null && !com.equals("") && num !=null && !num.equals("")){
                String param ="{\"com\":\""+com+"\",\"num\":\""+ num+"\"}";
                String key = express100Key;
                String sign = MD5.encode(param + key + customer);
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("param",param);
                params.put("sign",sign);
                params.put("customer",customer);
                String message = "";
                System.out.println(sign);
                try {
                    message = new HttpRequest().postData("http://poll.kuaidi100.com/poll/query.do", params, "utf-8").toString();
                    if(message.indexOf("result")!=-1&&message.indexOf("returnCode")!=-1){
                        result.setError("查询无结果，请隔段时间再查");
                        return result;
                    }
                    System.out.println(message);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                orderLogisticsInfoDetail.setMessage(message);
            }
        }
        result.setResult(orderLogisticsInfoDetail);
        return result;
    }



}
