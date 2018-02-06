package com.nowbook.trade.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.nowbook.agreements.dao.PreAuthorizationDao;
import com.nowbook.agreements.model.PreAuthorizationDepositOrder;
import com.nowbook.sdp.dao.LevelDao;
import com.nowbook.sdp.dao.SystemConfRedisDao;
import com.nowbook.sdp.dao.UserLevelDao;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.model.UserLevel;
import com.nowbook.sdp.model.UserWalletSummary;
import com.nowbook.sdp.service.UserWalletService;
import com.nowbook.sdp.service.UserWalletServiceImpl;
import com.nowbook.trade.dto.*;
import com.nowbook.alipay.dto.AlipayRefundData;
import com.nowbook.alipay.event.AlipayEventBus;
import com.nowbook.alipay.event.TradeCloseEvent;
import com.nowbook.alipay.request.CallBack;
import com.nowbook.alipay.request.RefundRequest;
import com.nowbook.alipay.request.Token;
import com.nowbook.alipay.request.UnFreezeRequest;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.JsonMapper;
import com.nowbook.constant.ExpireTimes;
import com.nowbook.exception.ServiceException;
import com.nowbook.item.model.Item;
import com.nowbook.item.model.Sku;
import com.nowbook.item.service.ItemBundleService;
import com.nowbook.item.service.ItemService;
import com.nowbook.rlt.predeposit.service.PreDepositService;
import com.nowbook.rlt.presale.service.PreSaleService;
import com.nowbook.sdp.service.AmountDetailService;
import com.nowbook.shop.service.ShopService;
import com.nowbook.trade.dao.OrderDao;
import com.nowbook.trade.dao.OrderExtraDao;
import com.nowbook.trade.dao.OrderItemDao;
import com.nowbook.trade.manager.OrderManager;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderExtra;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.unionpay.acp.sdk.AcpService;
import com.nowbook.unionpay.acp.sdk.SDKConfig;
import com.nowbook.user.base.BaseUser;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nowbook.weixin.weixin4j.Configuration;
import com.nowbook.weixin.weixin4j.Weixin;
import com.nowbook.weixin.weixin4j.message.Articles;
import com.nowbook.weixin.weixin4j.pay.SignUtil;
import com.nowbook.weixin.weixin4j.pay.WCRefund;
import com.nowbook.weixin.weixin4j.pay.WCRefundResult;
import com.nowbook.weixin.weixin4j.token.model.AccessToken;
import com.nowbook.weixin.weixin4j.token.service.RefurbishAccessTokenService;
import org.aspectj.weaver.ast.Or;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.nowbook.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-12-02
 */
@Service
public class OrderWriteServiceImpl implements OrderWriteService {
    private final static Logger log = LoggerFactory.getLogger(OrderWriteServiceImpl.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final int PAGE_SIZE = 200;

    private static final JsonMapper mapper = JsonMapper.nonDefaultMapper();
    private final static JavaType invoiceType = mapper.createCollectionType(HashMap.class, String.class, String.class);

    @Autowired
    private PreAuthorizationDao preAuthorizationDao;


    @Autowired
    private OrderManager orderManager;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemBundleService itemBundleService;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderExtraDao orderExtraDao;

    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private PreDepositService preDepositService;

    @Autowired
    private AmountDetailService amountDetailService;
    @Autowired
    private AccountService<User> accountService;
    @Autowired
    private RefurbishAccessTokenService refurbishAccessTokenService;

    @Autowired
    private Token token;

    @Autowired
    private AlipayEventBus alipayEventBus;

    @Autowired
    private UserLevelDao userLevelDao;

    @Autowired
    private UserWalletService userWalletService;

    @Autowired
    private LevelDao levelDao;

    @Autowired
    private SystemConfRedisDao systemConfRedisDao;

    @Value("#{app.alipayRefundSuffix}")
    private String notifyUrl;

    @Value("#{app.alipayAppId}")
    private String alipayAppId;

    @Value("#{app.alipayAppPrivateKey}")
    private String alipayAppPrivateKey;

    @Value("#{app.alipayAppPublicKey}")
    private String alipayAppPublicKey;

    @Value("#{app.alipayBody}")
    private String alipayBody;

    @Value("#{app.alipaySubject}")
    private String alipaySubject;

    @Value("#{app.alipayNotifyUrl}")
    private String alipayNotifyUrl;

    @Value("#{app.alipayCharset}")
    private String alipayCharset;

    @Value("#{app.wxKey}")
    private String wxKey;

    @Value("#{app.wxMchId}")
    private String wxMchId;

    @Value("#{app.wxMchKey}")
    private String wxMchKey;

    @Value("#{app.wxAppID}")
    private String wxAppID;

    @Value("#{app.wxAppSecret}")
    private String wxAppSecret;

    @Value("#{app.wxAppID}")
    private String wxAppId;

    @Value("#{app.wxNotifyUrl}")
    private String wxNotifyUrl;

    @Value("#{app.wxBody}")
    private String wxBody;

    @Value("#{app.wxCertFile}")
    private String wxCertFile;

    @Value("#{app.acpsdkMerId}")
    private String acpsdkMerId;
    /**
     * 创建订单
     *
     * @param buyerId     买家id
     * @param tradeInfoId 收货信息id
     * @param fatOrders   买家提交的订单
     * @return sellerId 和 新创建的订单id列表 key-value对
     */
    @Override
    public Response<Map<Long,Long>> create(Long buyerId, Long tradeInfoId,
                                           List<FatOrder> fatOrders,Map<Long,Integer> skuIdAndDiscount,String bank) {
        Response<Map<Long,Long>> result = new Response<Map<Long,Long>>();

        try {

            checkArgument(notNull(buyerId), "buyer.id.can.not.be.empty");
            checkArgument(notNull(tradeInfoId), "trade.info.id.can.not.be.empty");
            checkArgument(notNull(fatOrders) && !fatOrders.isEmpty(), "fat.orders.can.not.be.empty");

            Map<Long, SkuAndItem> skuAndItems = prepareOrder(fatOrders);

            OrderResult orderResult = orderManager.create(buyerId, tradeInfoId, fatOrders, skuAndItems, skuIdAndDiscount, bank);
            //减库存及增加销量
            for (StockChange stockChange : orderResult.getStockChanges()) {
                Response<Boolean> dr = itemService.changeSoldQuantityAndStock(stockChange.getSkuId(), stockChange.getItemId(), -stockChange.getQuantity());
                if (!dr.isSuccess()) {
                    log.error("failed to decrement stock of sku(id={}) and item(id={}),error code:{}",
                            stockChange.getSkuId(), stockChange.getItemId(), dr.getError());
                }
            }
            result.setResult(orderResult.getSellerIdAndOrderId());
            return result;
        } catch (IllegalArgumentException e) {
            log.error("failed to create orders from {} for buyer(id={}): error:{}",
                    fatOrders, buyerId, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("failed to create orders from {} for buyer(id={}): cause:{}",
                    fatOrders, buyerId, Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
            return result;
        }
    }

    @Override
    public Response<Long> createItemBundle(Long buyerId, Long tradeInfoId, ItemBundleFatOrder itemBundleFatOrder,String bank) {
        Response<Long> result = new Response<Long>();
        try {
            checkArgument(notNull(buyerId), "buyer.id.can.not.be.empty");
            checkArgument(notNull(tradeInfoId), "trade.info.id.can.not.be.empty");
            checkArgument(notNull(itemBundleFatOrder), "order.can.not.be.empty");


            Map<Long, SkuAndItem> skuAndItems = Maps.newHashMap();
            boolean hasIllegalSku = getSkuAndItem(itemBundleFatOrder, skuAndItems);
            if (hasIllegalSku) {
                log.error("has illegal sku when create item bundle order from{}", itemBundleFatOrder);
                result.setError("item.bundle.illegal.sku");
                return result;
            }

            //创建订单
            ItemBundleOrderResult ibor = orderManager.itemBundleOrderCreate(buyerId, tradeInfoId, itemBundleFatOrder, skuAndItems);

            //减库存及增加销量
            for (StockChange stockChange : ibor.getStockChanges()) {
                Response<Boolean> dr = itemService.changeSoldQuantityAndStock(stockChange.getSkuId(), stockChange.getItemId(), -stockChange.getQuantity());
                if (!dr.isSuccess()) {
                    log.error("failed to decrement stock of sku(id={}) and item(id={}),error code:{}", stockChange.getSkuId(), stockChange.getItemId());
                }
            }

            result.setResult(ibor.getOrderId());
            return result;
        } catch (IllegalArgumentException e) {
            log.error("failed to create item bundle orders from{}. for buyer id={}, error:{}",
                    itemBundleFatOrder, buyerId, e.getMessage());
            result.setError("order.create.fail");
            return result;
        } catch (Exception e) {
            log.error("failed to create item bundle orders from{}. for buyer id={}, cause:{}",
                    itemBundleFatOrder, buyerId, Throwables.getStackTraceAsString(e));
            result.setError("order.create.fail");
            return result;
        }
    }

    /**
     * 根据用户提交的订单信息查询对应商品的sku及item信息,对于不能完成购买的sku,则直接忽略
     *
     * @param fatOrders 用户提交的订单信息
     * @return 以skuId为key的对应商品的sku及item信息
     */
    private Map<Long, SkuAndItem> prepareOrder(List<FatOrder> fatOrders) {
        Map<Long, SkuAndItem> skuAndItems = Maps.newHashMap();
        for (FatOrder fatOrder : fatOrders) {
            getSkuAndItem(fatOrder, skuAndItems);
        }
        return skuAndItems;
    }

    /**
     * 当组合模板商品调用改方法验证sku合法性的时候，如果skuIdAndQuantity数量和skuAndItems数量不一致，
     * 说明有非法sku，返回true，直接返回error
     */
    private boolean getSkuAndItem(FatOrder fatOrder, Map<Long, SkuAndItem> skuAndItems) {
        Map<Long, Integer> skuIdAndQuantity = fatOrder.getSkuIdAndQuantity();
        for (Long skuId : skuIdAndQuantity.keySet()) {
            Integer quantity = skuIdAndQuantity.get(skuId);
            if (quantity <= 0) {
                log.error("sku(id={}) quantity can not litter than 1, but is {}", skuId, quantity);
                throw new ServiceException("sku.quantity.incorrect");
            }
            Response<Sku> skuR = itemService.findSkuById(skuId);
            if (!skuR.isSuccess()) {
                log.error("fail to find sku by id={}, error code:{}", skuId, skuR.getError());
                throw new ServiceException("sku.not.found");
            }
            Sku sku = skuR.getResult();
            if (sku == null) {
                log.error("can not find sku where id = {}", skuId);
                throw new ServiceException("sku.not.found");
            }
            if (sku.getStock() < quantity) {
                log.warn("no enough stock for sku where id={} (required:{},stock:{})", skuId, quantity, sku.getStock());
                throw new ServiceException("sku.stock.not.enough");
            }
            Response<Item> ir = itemService.findById(sku.getItemId());
            if (!ir.isSuccess()) {
                log.error("no item(id={}) found, error code: {}", sku.getItemId(), ir.getError());
                throw new ServiceException("item.not.found");
            }
            Item item = ir.getResult();
            if (!Objects.equal(item.getStatus(), Item.Status.ON_SHELF.toNumber())) {
                log.warn("item(id={}) is not onShelf,so skip this {}", item.getId(), sku);
                throw new ServiceException("item.status.incorrect");
            }
            SkuAndItem skuAndItem = new SkuAndItem();
            skuAndItem.setItem(item);
            skuAndItem.setSku(sku);
            skuAndItems.put(skuId, skuAndItem);
        }
        return skuAndItems.size() < skuIdAndQuantity.size();
    }

    /**
     * 为订单付款
     *
     */
    @Override
    public Response<Boolean> normalOrderPaid(Long orderId, String paymentCode,String paymentPlatform, Date paidAt) {
        Response<Boolean> result = new Response<Boolean>();
        if (orderId == null) {
            log.error("orderId can not be null");
            result.setError("order.id.can.not.be.empty");
            return result;
        }
        try {
            orderManager.pay(paymentCode, paymentPlatform , paidAt, orderId);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to pay for order(id={}),cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("order.pay.fail");
            return result;
        }
    }

    /**
     * 多个订单同时付款
     * @return 是否付款成功
     */
    @Override
    public Response<Boolean> batchNormalOrderPaid(List<Long> orderIds, String paymentCode,String paymentPlatform, Date paidAt) {
        Response<Boolean> result = new Response<Boolean>();
        if (orderIds == null || orderIds.isEmpty()) {
            log.error("orderIds can not be null");
            result.setError("order.ids.can.not.be.empty");
            return result;
        }
        try {
            orderManager.pay(paymentCode, paymentPlatform,paidAt, orderIds.toArray(new Long[orderIds.size()]));
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to batch pay for orderIds={},cause:{}", orderIds, Throwables.getStackTraceAsString(e));
            result.setError("order.batch.pay.fail");
            return result;
        }
    }

    /**
     * 卖家发货
     *
     * @param order 订单
     */
    @Override
    public Response<Boolean> deliver(Order order, OrderLogisticsInfoDto orderLogisticsInfoDto, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        Long sellerId = user.getId();

        try {
            checkArgument(notNull(order), "order.can.not.be.empty");
            checkArgument(notNull(sellerId), "seller.id.can.not.be.empty");
            checkArgument(equalWith(order.getSellerId(), sellerId)
                    || equalWith(user.getType(), User.TYPE.ADMIN.toNumber()), "authorize.fail");

            Weixin weixin = new Weixin();
            String appId = Configuration.getOAuthAppId();
            String secret = Configuration.getOAuthSecret();
            Response<AccessToken> atResult= refurbishAccessTokenService.selectAccessToken();
            if(atResult.getResult() == null){
                weixin.login(appId, secret);
                AccessToken accessToken = new AccessToken();
                accessToken.setAccess_token(weixin.getOAuthToken().getAccess_token());
                accessToken.setUpdated_at(new Date());
                refurbishAccessTokenService.creatAccessToken(accessToken);
            }else{
                AccessToken accessTokenResult = atResult.getResult();
                String access_token = accessTokenResult.getAccess_token();
                Long updated_at = accessTokenResult.getUpdated_at().getTime();
                weixin.init(access_token,appId,secret,7200,updated_at);
                weixin.login(appId, secret);
                if(!atResult.getResult().getAccess_token().equals(weixin.getOAuthToken().getAccess_token())){
                    refurbishAccessTokenService.updateAccessToken(weixin.getOAuthToken().getAccess_token());
                }
            }
            Articles articles = new Articles();
            articles.setTitle("卖家已发货！");
            String txt = "您的订单 " + order.getId() + " 卖家已发货，点击查看订单状态。";
            articles.setDescription(txt);
            String url = "http://shop.nowbook.com/html/mall/order_list.html";
            articles.setUrl(url);
            List<Articles> listArticles = new ArrayList<Articles>();
            listArticles.add(articles);
//            weixin.customSendNews(accountService.findUserById(order.getBuyerId()).getResult().getThirdPartId(), listArticles);
            orderManager.deliver(order,orderLogisticsInfoDto);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("failed to deliver for order ({}),error:{}", order, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("failed to deliver for order ({}),cause:{}", order, Throwables.getStackTraceAsString(e));
            result.setError("order.deliver.fail");
            return result;
        }

    }

    /**
     * 买家确认收货
     *
     * @param order   订单
     * @param buyerId 确认者id
     */
    @Override
    public Response<Boolean> confirm(Order order, Long buyerId) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            checkArgument(notNull(order), "order.can.not.be.empty");
            checkArgument(notNull(order.getBuyerId()), "buyer.id.can.not.be.empty");


            Long orderId = order.getId();

            orderManager.confirm(orderId, buyerId);
            //add shop sales count
            shopService.incrShopSalesBySellerId(order.getSellerId(), Long.valueOf(order.getFee()));
            List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());
            long soldQuantity = 0l;
            for (OrderItem oi : orderItems) {
                soldQuantity += oi.getQuantity();
            }
            //add shop sold quantity count
            shopService.incrShopSoldQuantityBySellerId(order.getSellerId(), soldQuantity);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("failed to confirm order:{},error:{}", order, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("failed to confirm order:{},cause:{}", order, Throwables.getStackTraceAsString(e));
            result.setError("order.confirm.fail");
            return result;
        }
    }

    /**
     * 买家取消订单
     *
     * @param order 订单
     */
    private Response<Boolean> cancelByBuyer(Order order) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());
            //预售订单，买家取消订单时定金已支付，预售总，子订单的状态为CANCELED_PRESALE_DEPOSIT_BY_BUYER
            //如果定金未支付，预售总，子订单的状态为CANCELED_BY_BUYER
            if (Objects.equal(order.getType(), Order.Type.PRE_SELL.value())) {
                //找到定金订单
                OrderItem earnest;
                if(Objects.equal(orderItems.get(0).getType(), OrderItem.Type.PRESELL_DEPOSIT.value()))
                    earnest = orderItems.get(0);
                else
                    earnest = orderItems.get(1);
                //判断定金是否已付款
                List<Long> orderItemIds = Lists.transform(orderItems, new Function<OrderItem, Long>() {
                    @Override
                    public Long apply(OrderItem input) {
                        return input.getId();
                    }
                });
                if(Objects.equal(earnest.getStatus(), OrderItem.Status.PAYED.value())) {
                    orderManager.preSalePaidCancelByBuyer(order, orderItemIds);
                }
                if(Objects.equal(earnest.getStatus(), OrderItem.Status.WAIT_FOR_PAY.value())) {
                    orderManager.preSaleCancelByBuyer(order, orderItemIds);
                }
                //恢复库存
                itemService.changeSoldQuantityAndStock(earnest.getSkuId(), earnest.getItemId(), earnest.getQuantity());
            } else {
                if (Objects.equal(order.getPaymentType(), Order.PayType.COD.value())) {
                    orderManager.cancelCodByBuyer(order);
                } else if (Objects.equal(order.getPaymentType(), Order.PayType.ONLINE.value())||Objects.equal(order.getPaymentType(), Order.PayType.STORE_PAY.value())) {
                    orderManager.cancelByBuyer(order);
                }
                //恢复库存 抢购订单不回滚库存
                if(isNull(order.getIsBuying())||!order.getIsBuying()){

                    for (OrderItem sku : orderItems) {
                        itemService.changeSoldQuantityAndStock(sku.getSkuId(), sku.getItemId(), sku.getQuantity());
                    }
                }
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to cancel {},cause:{}", order, Throwables.getStackTraceAsString(e));
            result.setError("order.cancel.fail");
            return result;
        }
    }

    /**
     * 当订单未付款时,卖家取消订单,预售订单卖家不能取消订单
     *
     * @param order 订单
     */
    private Response<Boolean> cancelBySeller(Order order) {
        Response<Boolean> result = new Response<Boolean>();
        try {

            List<OrderItem> skus = orderItemDao.findByOrderId(order.getId());

            if (Objects.equal(order.getPaymentType(), Order.PayType.COD.value())) {
                //货到付款 预售和普通订单状态不同，所以要分开处理
                if(Objects.equal(order.getType(), Order.Type.PLAIN.value())) {
                    orderManager.cancelCodBySeller(order);
                }
                if(Objects.equal(order.getType(), Order.Type.PRE_SELL.value())) {
                    List<Long> orderItemIds = Lists.transform(skus, new Function<OrderItem, Long>() {
                        @Override
                        public Long apply(OrderItem input) {
                            return input.getId();
                        }
                    });
                    orderManager.cancelPreSaleCodBySeller(order, orderItemIds);
                }

            } else if (Objects.equal(order.getPaymentType(), Order.PayType.ONLINE.value())) {
                orderManager.cancelBySeller(order);
            }
            //恢复库存，如果是预售商品，只需要恢复其中任意一个子订单就好了
            if(Objects.equal(order.getType(), Order.Type.PRE_SELL.value())) {
                OrderItem earnestOrRemain = skus.get(0);
                itemService.changeSoldQuantityAndStock(earnestOrRemain.getSkuId(), earnestOrRemain.getItemId(),
                        earnestOrRemain.getQuantity());
            }else {
                for (OrderItem sku : skus) {
                    itemService.changeSoldQuantityAndStock(sku.getSkuId(), sku.getItemId(), sku.getQuantity());
                }
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to cancel {},cause:{}", order, Throwables.getStackTraceAsString(e));
            result.setError("order.cancel.fail");
            return result;
        }
    }


    private Response<Boolean> cancelByRefund(OrderItem orderItem) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            boolean isUpdated = orderManager.cancelByRefund(orderItem);
            //恢复库存

            itemService.changeSoldQuantityAndStock(orderItem.getSkuId(), orderItem.getItemId(), orderItem.getQuantity());
            result.setResult(isUpdated);
            return result;
        } catch (Exception e) {
            log.error("failed to refund and cancel {},cause:{}", orderItem, Throwables.getStackTraceAsString(e));
            result.setError("order.refund.fail");
            return result;
        }
    }

    /**
     * 取消子订单
     *
     * @param orderItemId 订单id
     */
    @Override
    public Response<Boolean> cancelOrderItem(Long orderItemId) {
        Response<Boolean> result = new Response<Boolean>();
        if (orderItemId == null) {
            log.error("orderItemId can not be null");
            result.setError("order.item.id.can.not.be.empty");
            return result;
        }
        OrderItem orderItem = orderItemDao.findById(orderItemId);
        Order order = orderDao.findById(orderItem.getOrderId());
        if (order == null) {
            log.error("no order(id={}) found", orderItem.getOrderId());
            result.setError("order.not.found");
            return result;
        }

        //普通订单退货款入口
        if (Objects.equal(order.getType(), Order.Type.PLAIN.value())) {
            if (OrderItem.Status.WAIT_FOR_REFUND == OrderItem.Status.from(orderItem.getStatus())) {
                return cancelByRefund(orderItem);
            } else if (OrderItem.Status.AGREE_RETURNGOODS == OrderItem.Status.from(orderItem.getStatus())) {
                return cancelByReturnGoods(orderItem);
            }
            result.setError("orderItem.status.incorrect");
            return result;
        } else { //预售订单退货款入口
            if (OrderItem.Status.WAIT_FOR_REFUND == OrderItem.Status.from(orderItem.getStatus())) {
                orderManager.cancelPreSaleOrderItem(order,
                        OrderItem.Status.CANCELED_BY_REFUND.value());
                result.setResult(Boolean.TRUE);
                return result;
            } else if (OrderItem.Status.AGREE_RETURNGOODS == OrderItem.Status.from(orderItem.getStatus())) {
                orderManager.cancelPreSaleOrderItem(order,
                        OrderItem.Status.CANCELED_BY_RETURNGOODS.value());
                result.setResult(Boolean.TRUE);
                return result;
            }
            result.setError("orderItem.status.incorrect");
            return result;
        }
    }

    @Override
    public Response<Boolean> cancelOrder(Order order, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        Long userId = user.getId();
        if (order == null || userId == null) {
            log.error("both order and userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        if (Objects.equal(userId, order.getBuyerId())) {
            return cancelByBuyer(order);
        } else if (Objects.equal(userId, order.getSellerId())
                || Objects.equal(User.TYPE.ADMIN.toNumber(), user.getType())) {
            return cancelBySeller(order);
        }
        result.setError("authorize.fail");
        return result;
    }

    /**
     * 拒绝退款或者拒绝退货
     *
     * @param orderItemId 订单id
     * @param user        当前登录用户id,该接口只有卖家能调用
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> refuse(Long orderItemId, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        Long sellerId = user.getId();
        if (orderItemId == null || sellerId == null) {
            log.error("both orderId and userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        OrderItem orderItem = orderItemDao.findById(orderItemId);
        Order order = orderDao.findById(orderItem.getOrderId());
        if (order == null) {
            log.error("no order(orderItemId={}) found", orderItemId);
            result.setError("order.not.found");
            return result;
        }
        if (!Objects.equal(order.getSellerId(), sellerId)
                && !Objects.equal(User.TYPE.ADMIN.toNumber(), user.getType())) {
            log.error("current user({}) don't have the right to refuse order({})", user, order);
            result.setError("authorize.fail");
            return result;
        }
        try {
            if (OrderItem.Status.WAIT_FOR_REFUND == OrderItem.Status.from(orderItem.getStatus())) {
                orderManager.refuseRefund(orderItem);
            } else if (OrderItem.Status.APPLY_FOR_RETURNGOODS == OrderItem.Status.from(orderItem.getStatus())) {
                orderManager.refuseReturnGoods(orderItem, order);
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("refuse refund or returnGoods fail, orderItemId={}, cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("order.status.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> agreeReturnGoods(Long orderItemId, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        Long sellerId = user.getId();
        if (orderItemId == null || sellerId == null) {
            log.error("both orderId and userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        OrderItem orderItem = orderItemDao.findById(orderItemId);
        Order order = orderDao.findById(orderItem.getOrderId());
        if (order == null) {
            log.error("no order(orderItemId={}) found", orderItemId);
            result.setError("order.not.found");
            return result;
        }
        if ((!Objects.equal(order.getSellerId(), sellerId))
                && (!Objects.equal(User.TYPE.ADMIN.toNumber(), user.getType()))) {
            log.error("current user don't have the right");
            result.setError("authorize.fail");
            return result;
        }
        try {
            orderManager.agreeReturnGoods(orderItem);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("seller agree return goods fail, orderItemId={}, cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("order.status.update.fail");
            return result;
        }
    }

    /**
     * 卖家同意退货-押金预授权订单
     *
     * @param orderId 订单id
     * @param user        当前登录用户id，该接口只有卖家能调用
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> agreeReturnDeposit(Long orderId, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        Long sellerId = user.getId();
        if (orderId == null || sellerId == null) {
            log.error("both orderId and userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        Order order = orderDao.findById(orderId);
        OrderItem depositOrder = orderItemDao.findByMap(orderId,2);//押金子订单
        if (order == null) {
            log.error("no order(orderId={}) found", orderId);
            result.setError("order.not.found");
            return result;
        }
        if ((!Objects.equal(order.getSellerId(), sellerId))
                && (!Objects.equal(User.TYPE.ADMIN.toNumber(), user.getType()))) {
            log.error("current user don't have the right");
            result.setError("authorize.fail");
            return result;
        }
        try {
            orderManager.agreeReturnDeposit(depositOrder);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("seller agree return goods fail, orderItemId={}, cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("order.status.update.fail");
            return result;
        }
    }


    @Override
    public Response<Boolean> undoRequest(Long orderItemId, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        Long buyerId = user.getId();
        if (orderItemId == null || buyerId == null) {
            log.error("both orderId and userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        OrderItem orderItem = orderItemDao.findById(orderItemId);
        Order order = orderDao.findById(orderItem.getOrderId());
        if (order == null) {
            log.error("no order(orderItemId={}) found", orderItemId);
            result.setError("order.not.found");
            return result;
        }
        if (!Objects.equal(order.getBuyerId(), buyerId)
                && !Objects.equal(User.TYPE.ADMIN.toNumber(), user.getType())) {
            log.error("current user don't have the right");
            result.setError("authorize.fail");
            return result;
        }
        try {
            if (OrderItem.Status.WAIT_FOR_REFUND == OrderItem.Status.from(orderItem.getStatus())) {
                orderManager.undoRequestRefund(orderItem);
            } else if (OrderItem.Status.APPLY_FOR_RETURNGOODS == OrderItem.Status.from(orderItem.getStatus())) {
                orderManager.undoRequestReturnGoods(orderItem,order);
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("undo refund or returnGoods fail, orderItemId={}, cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("order.status.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> addReasonAndRefund(Long orderItemId, BaseUser user, String reason, Integer refundAmount) {
        Response<Boolean> result = new Response<Boolean>();
        Long userId = user.getId();
        if (orderItemId == null || userId == null) {
            log.error("both orderItemId and userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        OrderItem orderItem = orderItemDao.findById(orderItemId);
        if (orderItem == null) {
            log.error("no orderItem(id={}) found", orderItemId);
            result.setError("order.not.found");
            return result;
        }
        if (!Objects.equal(orderItem.getBuyerId(), userId)) {
            log.error("current user don't have the right");
            result.setError("authorize.fail");
            return result;
        }
        //添加一个退款金额的最大数值验证
        if(orderItem.getFee() < refundAmount){
            log.error("refund amount fee({}) must be less than orderItem total fee({}).", orderItem.getFee(), refundAmount);
            result.setError("order.item.refund.mustLess");
            return result;
        }

        Order order = orderDao.findById(orderItem.getOrderId());
        if (order == null) {
            log.error("no order(id={}) found", orderItem.getOrderId());
            result.setError("order.not.found");
            return result;
        }

        String return_day_limit = systemConfRedisDao.getDate("return_day_limit");
        if(order.getDoneAt() !=null && new Date().getTime() - order.getDoneAt().getTime()>Long.valueOf(return_day_limit)*24*60*60*1000){
            orderItem.setStatus(8);
            result.setError("申请售后已超时");
            return result;
        }

        try {
            orderItem.setReason(reason);
            orderItem.setRefundAmount(refundAmount);
            if (OrderItem.Status.PAYED == OrderItem.Status.from(orderItem.getStatus())) {
                //货到付款的子订单不能退款
                if (Objects.equal(orderItem.getPayType(), OrderItem.PayType.COD.value())) {
                    result.setError("orderItem.type.incorrect");
                    return result;
                }
                orderManager.requestRefund(orderItem);
            } else if (OrderItem.Status.DELIVERED == OrderItem.Status.from(orderItem.getStatus())) {
                //货到付款退货默认退款金额为0
                if (Objects.equal(orderItem.getPayType(), OrderItem.PayType.COD.value())) {
                    orderItem.setRefundAmount(0);
                }
                orderManager.requestReturnGoods(orderItem);
            } else if (OrderItem.Status.DONE == OrderItem.Status.from(orderItem.getStatus())) {
                //货到付款退货默认退款金额为0
                if (Objects.equal(orderItem.getPayType(), OrderItem.PayType.COD.value())) {
                    orderItem.setRefundAmount(0);
                }
                orderManager.requestReturnGoods(orderItem);
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to create or update orderExtra orderItemId={}, cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("extra.update.fail");
            return result;
        }
    }

    @Override
    public Response<Long> createOrder(Order order) {
        Response<Long> result = new Response<Long>();

        try {
            orderDao.create(order);

            result.setResult(order.getId());

            return result;
        } catch (Exception e) {
            log.error("failed to create {}, cause:{}", order, Throwables.getStackTraceAsString(e));
            result.setError("order.create.fail");
            return result;
        }
    }

    @Override
    public Response<Long> createOrderItem(OrderItem orderItem) {
        Response<Long> result = new Response<Long>();
        try {
            orderItemDao.create(orderItem);
            result.setResult(orderItem.getId());
            return result;
        } catch (Exception e) {
            log.error("failed to create {}, cause:{}", orderItem, Throwables.getStackTraceAsString(e));
            result.setError("orderItem.create.fail");
            return result;
        }
    }

    @Override
    public void createOrderExtra(OrderExtra orderExtra) {
        orderExtraDao.create(orderExtra);
    }

    private Response<Boolean> cancelByReturnGoods(OrderItem orderItem) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            orderManager.cancelByReturnGoods(orderItem);
            //恢复库存
            Response<Boolean> sR= itemService.changeSoldQuantityAndStock(orderItem.getSkuId(),
                    orderItem.getItemId(), orderItem.getQuantity());
            if(!sR.isSuccess()){
                log.error("failed to change stock and sold quantity of sku(id={}), error code:{}",orderItem.getSkuId(),sR.getError());
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to cancel order by return goods, orderId={}, cause:{}", orderItem.getOrderId(), Throwables.getStackTraceAsString(e));
            result.setError("order.cancel.fail");
            return result;
        }
    }

    /**
     * 订单超时处理，分3种情况 1.超时未付款：取消订单 2.超时未确认收货：确认收货 3.超时未同意退款：确认退款
     */
    @Override
    public Response<Boolean> verifyOrderExpire(Date date) {
        Response<Boolean> result = new Response<Boolean>();
        log.info("start verify order expire job");

//        int maxDays = Math.max(ExpireTimes.NOT_PAY_EXPIRE_DAY, ExpireTimes.NOT_CONFIRM_EXPIRE_DAY);
        int maxDays = Math.max(Integer.valueOf(systemConfRedisDao.getDate("pay_day_limit")), Integer.valueOf(systemConfRedisDao.getDate("take_deliver_limit")));

        String compared = DATE_TIME_FORMAT.print(new DateTime(date).withTimeAtStartOfDay().minusDays(maxDays));

        Stopwatch stopwatch = Stopwatch.createStarted();
        Long lastId = orderDao.maxId() + 1;  //scan from maxId+1

        log.info("begin to verify order create after {}", compared);
        int returnSize = PAGE_SIZE;
        int handled = 0;
        while (returnSize == PAGE_SIZE) {
            List<Order> orders = orderDao.findNotFinished(lastId, compared, PAGE_SIZE);
            returnSize = orders.size();
            if (orders.isEmpty()) {
                log.info("no more order, lastId={}", lastId);
                break;
            } else {
                for (Order order : orders) {
                    if(Objects.equal(order.getType(), Order.Type.PRE_SELL.value())){ //不处理预售订单
                        continue;
                    }
                    try {
                        boolean success =processNotFinishedOrder(order);
                        if(success){
                            handled++;
                        }
                    } catch (Exception e) {
                        log.error("failed to expire order(id={}),cause:{},skip",order.getId(), Throwables.getStackTraceAsString(e));
                    }
                }
                lastId = Iterables.getLast(orders).getId();
            }
        }
        stopwatch.stop();
        log.info("end verify order expire job, succeed handle {} not finished orders ", handled);
        result.setResult(Boolean.TRUE);
        return result;
    }

    @Override
    public Response<Boolean> verifyOrderNotPaidExpire(Date date) {
        Response<Boolean> result = new Response<Boolean>();
        log.info("verify order not paid expire job start");

        String endAt = DATE_TIME_FORMAT.print(new DateTime(date).withTimeAtStartOfDay().minusDays(ExpireTimes.NOT_PAY_EXPIRE_DAY));
        String startAt = DATE_TIME_FORMAT.print(new DateTime(date).withTimeAtStartOfDay().minusDays(ExpireTimes.NOT_PAY_EXPIRE_DAY+ExpireTimes.MONTH_DAY));

        Long lastId = orderDao.maxId() + 1;
        log.info("begin to verify order create after {}, before {}", startAt, endAt);
        int returnSize = PAGE_SIZE;
        int handled = 0;

        while(returnSize == PAGE_SIZE) {
            try {
                List<Order> orders = orderDao.findNotPaid(lastId, startAt, endAt, PAGE_SIZE);

                if(orders.isEmpty()) {
                    log.info("no more order, lastId={}", lastId);
                    break;
                }else {
                    handled += notPaidExpire(orders);
                }

                returnSize = orders.size();
                handled += orders.size();
                lastId = orders.get(orders.size()-1).getId();
            }catch (Exception e) {
                log.error("fail to verify orders by lastId={}, startAt={}, endAt={}, cause:{}, skip this page",
                        lastId, startAt, endAt, Throwables.getStackTraceAsString(e));
            }
        }
        log.info("verify order not paid expire job end, handle {} orders", handled);
        result.setResult(Boolean.TRUE);
        return result;
    }

    @Override
    public Response<Boolean> notPaidExpire(List<Long> orderIds) {
        Response<Boolean> result = new Response<Boolean>();

        List<Order> orders = orderDao.findByIds(orderIds);
        List<Order> unpaid = new ArrayList<Order>();
        for (Order order : orders) {
            if (!order.getIsBuying())  // Skip normal order.
                continue;
            if (Order.Status.WAIT_FOR_PAY.value()==order.getStatus().intValue())
                unpaid.add(order);
        }
        if (!unpaid.isEmpty())
            notPaidExpire(unpaid);
        result.setResult(Boolean.TRUE);
        return result;
    }

    private int notPaidExpire(List<Order> orders) {
        int handled = 0;
        for(Order order : orders) {
            if(Objects.equal(order.getType(), Order.Type.PRE_SELL.value())){ //不处理预售订单
                continue;
            }
            try {
                Date now = new Date();

                //更新总订单和子订单状态
                Order updated = new Order();
                updated.setId(order.getId());
                updated.setStatus(Order.Status.CANCELED_BY_BUYER.value());
                updated.setFinishedAt(now);
                updated.setCanceledAt(now);
                orderDao.update(updated);
                orderManager.bathUpdateOrderItemStatusByOrderId(OrderItem.Status.WAIT_FOR_PAY.value(),
                        OrderItem.Status.CANCELED_BY_BUYER.value(), order.getId(), null);

                // 通知支付宝关闭交易
                if (equalWith(order.getType(), Order.Type.PLAIN.value())) {     // 如果是普通订单
                    alipayEventBus.post(new TradeCloseEvent(token, order.getId().toString()));
                } else {
                    List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());
                    OrderItem deposit = getDepositOrder(orderItems);
                    if (notNull(deposit)) {
                        alipayEventBus.post(new TradeCloseEvent(token, deposit.getOrderId() + "," + deposit.getId()));
                    }
                }


                handled ++;
            }catch (Exception e) {
                log.error("fail to expire order id={}, cause:{}, skip it",
                        order.getId(), Throwables.getStackTraceAsString(e));
            }
        }
        return handled;
    }

    private OrderItem getDepositOrder(List<OrderItem> orderItemes) {
        for (OrderItem orderItem : orderItemes) {
            if (equalWith(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                return orderItem;
            }
        }

        return null;
    }



    private boolean processNotFinishedOrder(Order order) {
        try {
            final DateTime startAtNow = DateTime.now();
            final Date now = new Date();
            switch (Order.Status.from(order.getStatus())) {
                case WAIT_FOR_PAY:
                    DateTime createTime = new DateTime(order.getCreatedAt()).withTimeAtStartOfDay();
                    //订单超时未付款，订单关闭
                    if (Days.daysBetween(createTime, startAtNow).getDays() >= Integer.valueOf(systemConfRedisDao.getDate("pay_day_limit"))) {
                        //更新总订单和子订单状态
                        Order updated = new Order();
                        updated.setId(order.getId());
                        updated.setStatus(Order.Status.CANCELED_BY_BUYER.value());
                        updated.setFinishedAt(now);
                        updated.setCanceledAt(now);
                        orderDao.update(updated);
                        orderManager.bathUpdateOrderItemStatusByOrderId(OrderItem.Status.WAIT_FOR_PAY.value(),
                                OrderItem.Status.CANCELED_BY_BUYER.value(), order.getId(), null);
                        return true;
                    }
                    break;
                case PAID:
                    DateTime paidTime = new DateTime(order.getCreatedAt()).withTimeAtStartOfDay();
                    //订单超时未发货
                    if (Days.daysBetween(paidTime, startAtNow).getDays() >= Integer.valueOf(systemConfRedisDao.getDate("deliver_day_limit"))) {
                        //更新总订单和子订单状态
                        Order updated = new Order();
                        updated.setId(order.getId());
                        updated.setStatus(Order.Status.CANCELED_BY_SELLER.value());
                        updated.setFinishedAt(now);
                        updated.setCanceledAt(now);
                        orderDao.update(updated);
                        orderManager.bathUpdateOrderItemStatusByOrderId(OrderItem.Status.PAYED.value(),
                                OrderItem.Status.CANCELED_BY_SELLER.value(), order.getId(), null);
                        return true;
                    }
                    break;
                case DELIVERED:
                    DateTime deliverTime = new DateTime(order.getDeliveredAt()).withTimeAtStartOfDay();
                    //订单超时未确认收货,统一10天超时
                    if (Days.daysBetween(deliverTime, startAtNow).getDays() >= Integer.valueOf(systemConfRedisDao.getDate("take_deliver_limit"))) {
                        //更新总订单和子订单状态
                        Order updated = new Order();
                        updated.setId(order.getId());
                        updated.setStatus(Order.Status.DONE.value());
                        updated.setFinishedAt(now);
                        updated.setDoneAt(now);
                        orderDao.update(updated);
                        orderManager.bathUpdateOrderItemStatusByOrderId(Order.Status.DELIVERED.value(),
                                Order.Status.DONE.value(), order.getId(), null);
                        return true;
                    }
                    break;
                default:
                    List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());
                    for (OrderItem oi : orderItems) {
                        if (!Objects.equal(oi.getStatus(), OrderItem.Status.WAIT_FOR_REFUND.value()))
                            continue;
                        DateTime requestRefundTime = new DateTime(oi.getRequestRefundAt()).withTimeAtStartOfDay();
                        //退款申请超时，自动同意退款
                        if (Days.daysBetween(requestRefundTime, startAtNow).getDays() >= Integer.valueOf(systemConfRedisDao.getDate("return_approve_limit"))) {
                            refundPlainOrderItem(new Date(), oi);
                            return true;
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            log.error("fail to verify order expire job, with orderId:{}, cause:{}, skip", order.getId(),
                    Throwables.getStackTraceAsString(e));
        }
        return false;
    }


    /**
     * 更新订单状态，内部调用
     *
     * @param id     订单id
     * @param status 订单状态
     * @return 操作是否成功
     */
    public Response<Boolean> updateStatus(Long id, Integer status) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            Order order = orderDao.findById(id);
            checkState(notNull(order), "order.not.found");


            if (Objects.equal(order.getStatus(), Order.Status.PAID.value()) && Objects.equal(status, Order.Status.DELIVERED.value())) {  // 已付款 ->  已发货
                orderManager.deliver(order);
            }

            if (Objects.equal(order.getStatus(), Order.Status.DELIVERED.value()) && Objects.equal(status, Order.Status.DONE.value())) {  // 已发货 -> 交易成功
                Order updated = new Order();
                updated.setId(order.getId());
                updated.setStatus(Order.Status.DONE.value());
                updated.setFinishedAt(new Date());
                updated.setDoneAt(new Date());
                orderDao.update(updated);
                orderManager.bathUpdateOrderItemStatusByOrderId(Order.Status.DELIVERED.value(),
                        Order.Status.DONE.value(), order.getId(), null);
            }


            result.setResult(Boolean.TRUE);
            return result;
        } catch (ServiceException e) {
            log.error("fail to invoke updateStatus with id={}, status={}, error:{}", id, status, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (IllegalStateException e) {
            log.error("fail to invoke updateStatus with id={}, status={}, error:{}", id, status, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("fail to invoke updateStatus with id={}, status={}", id, status, e);
            result.setError("order.update.status.fail");
            return result;
        }
    }

    /**
     * 预售付款
     *
     * @param orderItemId 子订单id
     * @param paymentCode 支付宝交易号
     * @param paidAt 付款时间
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> preSalePay(Long orderItemId, String paymentCode, Date paidAt) {
        Response<Boolean> result = new Response<Boolean>();
        if (orderItemId == null) {
            log.error("orderItem can not be null when pay earnest");
            result.setError("illegal.param");
            return result;
        }
        try {
            OrderItem orderItem = orderItemDao.findById(orderItemId);
            if (orderItem == null) {
                log.error("no orderItem(id={}) found", orderItemId);
                result.setError("orderItem.not.found");
                return result;
            }
            //如果预售定金没有付款直接支付尾款，报错
            if(Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_REST.value())) {
                List<OrderItem> earnestAndRemain = orderItemDao.findByOrderId(orderItem.getOrderId());
                for (OrderItem oi : earnestAndRemain) {
                    if (Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                        if(Objects.equal(oi.getStatus(), OrderItem.Status.WAIT_FOR_PAY.value())) {
                            log.error("can't pay remain orderItem when earnest orderItem not paid, orderId={}",
                                    orderItem.getOrderId());
                            result.setError("pay.remain.fail.earnest.not.paid");
                            return result;
                        }
                    }
                }
            }
            //只有等待付款的订单才能有付款这个操作
            if(!Objects.equal(orderItem.getStatus(), OrderItem.Status.WAIT_FOR_PAY.value())) {
                log.error("preSale orderItem(id={}) pay error, status not wait_for_pay", orderItem.getId());
                result.setError("orderItem.status.incorrect");
                return result;
            }

            orderManager.preSalePay(orderItem, paymentCode,paidAt);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to pay earnest for orderItem(id={}), cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("earnest.pay.fail");
            return result;
        }
    }

    /**
     * 预售申请退款退货，需要把退款金额分到2个子订单中去
     *
     * @param orderItemId  子订单id
     * @param user         当前登录用户id， 只有买家能调用
     * @param reason       退款理由
     * @param refundAmount 退款金额
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> preSaleAddReasonAndRefund(Long orderItemId, BaseUser user, String reason, Integer refundAmount) {
        Response<Boolean> result = new Response<Boolean>();
        Long userId = user.getId();
        if (orderItemId == null || userId == null) {
            log.error("both orderItemId and userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        OrderItem orderItem = orderItemDao.findById(orderItemId);
        if (orderItem == null) {
            log.error("no orderItem(id={}) found", orderItemId);
            result.setError("order.not.found");
            return result;
        }
        if (!Objects.equal(orderItem.getBuyerId(), userId)) {
            log.error("current user don't have the right");
            result.setError("authorize.fail");
            return result;
        }


        Order order = orderDao.findById(orderItem.getOrderId());
        if (order == null) {
            log.error("no order(id={}) found", orderItem.getOrderId());
            result.setError("order.not.found");
            return result;
        }

        //添加一个退款金额的最大数值验证
        if(order.getFee() < refundAmount){ //预售应该判断整个订单的fee, 而不是orderItem.fee
            log.error("refund amount fee({}) must be less than order total fee({}).", order.getFee(), refundAmount);
            result.setError("order.item.refund.mustLess");
            return result;
        }

        try {
            List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());
            OrderItem earnest = new OrderItem();
            OrderItem remain = new OrderItem();
            Date now = DateTime.now().toDate();
            if (Objects.equal(orderItem.getPayType(), OrderItem.PayType.ONLINE.value())) {
                for (OrderItem oi : orderItems) {
                    if (Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                        earnest.setId(oi.getId());
                        earnest.setRefundAmount(oi.getFee());
                        break;
                    }
                }
                remain.setId(orderItemId);
                remain.setRefundAmount(refundAmount - earnest.getRefundAmount());
            } else { //货到付款
                for (OrderItem oi : orderItems) {
                    if (Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                        earnest.setId(oi.getId());
                        earnest.setRefundAmount(refundAmount);
                        break;
                    }
                }
                remain.setId(orderItemId);
                remain.setRefundAmount(0);
            }
            earnest.setReason(reason);
            remain.setReason(reason);
            earnest.setRequestRefundAt(now);
            remain.setRequestRefundAt(now);
            if (OrderItem.Status.PAYED == OrderItem.Status.from(orderItem.getStatus())) {
                //货到付款的子订单不能退款
                if (Objects.equal(orderItem.getPayType(), OrderItem.PayType.COD.value())) {
                    result.setError("orderItem.type.incorrect");
                    return result;
                }
                orderManager.preSaleAddReasonAndRefund(order, earnest, remain,
                        OrderItem.Status.WAIT_FOR_REFUND.value());
            } else if (OrderItem.Status.DELIVERED == OrderItem.Status.from(orderItem.getStatus())) {
                orderManager.preSaleAddReasonAndRefund(order, earnest, remain,
                        OrderItem.Status.APPLY_FOR_RETURNGOODS.value());
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to create or update orderExtra orderItemId={}, cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("extra.update.fail");
            return result;
        }
    }

    /**
     * 押金订单申请退款退货，需要把退款金额分到2个子订单中去
     *
     * @param orderItemId  子订单id
     * @param user         当前登录用户id， 只有买家能调用
     * @param reason       退款理由
     * @param refundAmount 退款金额
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> depositAddReasonAndRefund(Long orderItemId, BaseUser user, String reason, Integer refundAmount) {
        Response<Boolean> result = new Response<Boolean>();
        Long userId = user.getId();

        if (orderItemId == null || userId == null) {
            log.error("both orderItemId and userId can not be null");
            result.setError("illegal.param");
            return result;
        }
        OrderItem orderItem = orderItemDao.findById(orderItemId);
        if (orderItem == null) {
            log.error("no orderItem(id={}) found", orderItemId);
            result.setError("order.not.found");
            return result;
        }
        if (!Objects.equal(orderItem.getBuyerId(), userId)) {
            log.error("current user don't have the right");
            result.setError("authorize.fail");
            return result;
        }

        Order order = orderDao.findById(orderItem.getOrderId());
        if (order == null) {
            log.error("no order(id={}) found", orderItem.getOrderId());
            result.setError("order.not.found");
            return result;
        }

        OrderItem earnest = orderItemDao.findByMap(order.getId(),2);//押金订单
        OrderItem remain = orderItemDao.findByMap(order.getId(),3);//尾款订单

        PreAuthorizationDepositOrder preAuthorizationDepositOrder =
                preAuthorizationDao.findOneByOrderId(order.getId());//预授权信息

        //添加一个退款金额的最大数值验证
        if(earnest.getFee() < refundAmount){ //预授权押金应该判断押金子订单的fee
            log.error("refund amount fee({}) must be less than depositOrder fee({}).", order.getFee(), refundAmount);
            result.setError("order.item.refund.mustLess");
            return result;
        }

        try {
            List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());
            Date now = DateTime.now().toDate();
            earnest.setRefundAmount(earnest.getFee());
            remain.setRefundAmount(refundAmount - earnest.getRefundAmount());
            earnest.setReason(reason);
            remain.setReason(reason);
            earnest.setRequestRefundAt(now);
            remain.setRequestRefundAt(now);

            //去掉发货后才能退货限制
            if (preAuthorizationDepositOrder.getDeliverStatus() == 1) {
                orderManager.depositAddReasonAndRefund(order, earnest, remain,preAuthorizationDepositOrder,-1);//已发货，买家申请退货
            }else{
                orderManager.depositAddReasonAndRefund(order, earnest, remain,preAuthorizationDepositOrder,-2);//未发货，买家申请退货
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to create or update orderExtra orderItemId={}, cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("extra.update.fail");
            return result;
        }
    }

    /**
     * 退普通订单货款
     *
     * @param refundAt  退款时间
     * @param orderItem 子订单
     */
    @Override
    public Response<Boolean> refundPlainOrderItem(Date refundAt, OrderItem orderItem) {
        Response<Boolean> result = new Response<Boolean>();
        if (!Objects.equal(orderItem.getType(), OrderItem.Type.PLAIN.value())) {
            log.error("this orderItem(id={}) is not a plain order item(type=1), it's type is:{}", orderItem.getId(), orderItem.getType());
            result.setError("order.item.type.illegal");
            return result;
        }

        if (Objects.equal(orderItem.getPayType(), OrderItem.PayType.COD.value())) {
            //更新货到付款退货状态
            Response<Boolean> cancelResult = cancelOrderItem(orderItem.getId());
            if (!cancelResult.isSuccess()) {
                log.error("failed to refund {}, error code:{}", orderItem, cancelResult.getError());
                result.setError(cancelResult.getError());
                return result;
            }

            result.setResult(Boolean.TRUE);
            return result;
        }

        OrderItem item = orderItemDao.findById(orderItem.getId());

        Response<Boolean> refundRes = payRefund(item);
        if (!refundRes.isSuccess()) {
            log.error(refundRes.getError());
            result.setError(refundRes.getError());
            return result;
        }

        result.setResult(Boolean.TRUE);
        return result;
    }

    /**
     * 退款 1:余额退款 2：预存款退款 3：支付宝退款 4：微信退款 5：银联退款
     * @param orderItem
     * @return
     */
    @Override
    @Transactional
    public Response<Boolean> payRefund(OrderItem orderItem) {
        Response<Boolean> result = new Response<Boolean>();

        if (orderItem.getRefundAmount() == null) {
            orderItem.setRefundAmount(orderItem.getFee());
        }
        // 金额大于0时才执行退款请求
        if (orderItem.getRefundAmount() > 0) {
            Response<Boolean> response = new Response<Boolean>();
            if(orderItem.getPaymentPlatform().equals("1")){
                response = balanceRefund(orderItem);
            }else if(orderItem.getPaymentPlatform().equals("2")){
                response = advanceRefund(orderItem);
            }else if(orderItem.getPaymentPlatform().equals("3")){
                response = alipayRefund(orderItem);
            }else if(orderItem.getPaymentPlatform().equals("4")){
                response = wxRefund(orderItem);
            }else if(orderItem.getPaymentPlatform().equals("5")){
                response = unionpayRefund(orderItem);
            }
            if (!response.isSuccess()) {
                log.error("wx pay fail to refund to {}, error code:{}", orderItem, response.getError());
                result.setError(response.getError());
                return result;
            }
        }

        Response<Boolean> updResult = updateOrderStatus(orderItem);
        if (!updResult.isSuccess()) {
            log.error(updResult.getError());
            result.setError(updResult.getError());
            return result;
        }

        result.setResult(Boolean.TRUE);
        return result;
    }


    /**
     * 贷款余额退款接口
     * @return
     */
    private Response<Boolean> balanceRefund(OrderItem orderItem) {
        Response<Boolean> response = new Response<Boolean>();
        UserLevel userLevel = new UserLevel();
        userLevel.setUserId(orderItem.getBuyerId());
        List<UserLevel> userLevelList = userLevelDao.selectByUserId(userLevel);
        if(userLevelList ==null || userLevelList.size() ==0){
            response.setError("此用户不存在。");
            return response;
        }
        UserWalletSummary userWalletSummary = new UserWalletSummary();
        userWalletSummary.setUserId(orderItem.getBuyerId());
        if(userLevelList.get(0).getLevel().equals(orderItem.getLevel())){
            userWalletSummary.setMoney(Long.valueOf(orderItem.getRefundAmount()));
            userWalletSummary.setType(3);
        }else{
            Level oldLevel = new Level();
            oldLevel.setLevel(orderItem.getLevel());
            List<Level> oldLevelList = levelDao.selectByLevel(oldLevel);
            if(oldLevelList == null || oldLevelList.size() == 0){
                response.setError("等级没有设置。");
                return response;
            }
            Level newLevel = new Level();
            newLevel.setLevel(userLevelList.get(0).getLevel());
            List<Level> newLevelList = levelDao.selectByLevel(newLevel);
            if(newLevelList == null || newLevelList.size() == 0){
                response.setError("等级没有设置。");
                return response;
            }
            Long money = Long.valueOf(orderItem.getRefundAmount()*newLevelList.get(0).getDiscount()/oldLevelList.get(0).getDiscount());
            userWalletSummary.setMoney(money);
            userWalletSummary.setLevel(newLevelList.get(0).getLevel());
            userWalletSummary.setType(5);
        }
        userWalletService.updateUserWallet(userWalletSummary);

        response.setResult(true);
        return response;
    }

    /**
     * 预存款退款接口
     * @return
     */
    private Response<Boolean> advanceRefund(OrderItem orderItem) {
        Response<Boolean> response = new Response<Boolean>();
        UserWalletSummary userWalletSummary = new UserWalletSummary();
        userWalletSummary.setUserId(orderItem.getBuyerId());
        userWalletSummary.setMoney(Long.valueOf(orderItem.getRefundAmount()));
        userWalletSummary.setType(25);
        userWalletService.updateUserWallet(userWalletSummary);
        response.setResult(true);
        return response;
    }

    /**
     * 支付宝退款接口
     * @return
     */
    private Response<Boolean> alipayRefund(OrderItem orderItem) {
        Response<Boolean> result = new Response<Boolean>();
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",alipayAppId,alipayAppPrivateKey,"json",alipayCharset,alipayAppPublicKey,"RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
                "\"trade_no\":\""+orderItem.getPaymentCode()+"\"," +
                "\"refund_amount\":"+String.valueOf(Double.valueOf(orderItem.getRefundAmount())/100)+"," +
                "\"refund_reason\":\"正常退款\"," +
                "\"out_request_no\":\""+orderItem.getId()+"\"" +
                " }");
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
            if(response.isSuccess()){
                System.out.println("alipay调用成功");
                result.setResult(true);
            } else {
                log.error("fail to refund to {}, error code:{}", orderItem);
                result.setError("");
                return result;
            }
        } catch (AlipayApiException e) {
            if(e.getMessage().equals("com.alipay.api.AlipayApiException: sign check fail: check Sign and Data Fail!")){
                System.out.println("alipay调用成功");
                result.setResult(true);
            }else{
                result.setError(e.getMessage());
            }
        }
        return result;
    }


    /**
     * 微信退款接口
     * @return
     */
    private Response<Boolean> wxRefund(OrderItem orderItem) {
        Response<Boolean> response = new Response<Boolean>();
        WCRefund wcRefund = new WCRefund();
        String appId = wxAppID;
        String secret = wxAppSecret;
        String key = wxMchKey;
        String Mch_id = wxMchId;
        String transaction_id = orderItem.getPaymentCode();
        String out_refund_no = orderItem.getId().toString();
        String total_fee = orderItem.getFee().toString();
        String refund_fee = orderItem.getRefundAmount().toString();
        String op_user_id = wxMchId;
        String error = "";
        try {
            String caFile = wxCertFile;
            FileInputStream ca =null;
            ca = new FileInputStream(new File(caFile));
            log.info("load file success,"+caFile);

            wcRefund.setAppid(appId);
            wcRefund.setMch_id(Mch_id);
            wcRefund.setNonce_str(java.util.UUID.randomUUID().toString().substring(0, 15));
            wcRefund.setOut_refund_no(out_refund_no);
            wcRefund.setRefund_fee(refund_fee);
            wcRefund.setTotal_fee(total_fee);
            wcRefund.setTransaction_id(transaction_id);

            String sign = SignUtil.getSign(wcRefund.toMap(), key);
            wcRefund.setSign(sign);
            Weixin weixin = new Weixin();
            WCRefundResult wcRefundResult = weixin.wcRefundResult(ca,wcRefund,Mch_id);
            if(wcRefundResult.isSuccess()){
                response.setResult(wcRefundResult.isSuccess());
            }else{
                error = wcRefundResult.getErr_code();
            }
            return response;
        } catch (Exception e) {
            log.error("wxRefund fail,"+error);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 银联退款接口
     * @return
     */
    private Response<Boolean> unionpayRefund(OrderItem orderItem) {
        Response<Boolean> response = new Response<Boolean>();
        SDKConfig.getConfig().loadPropertiesFromSrc();
        Map<String, String> data = new HashMap<String, String>();
        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put("version", SDKConfig.getConfig().getVersion());               //版本号
        data.put("encoding", "UTF-8");             //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
        data.put("txnType", "04");                           //交易类型 04-退货
        data.put("txnSubType", "00");                        //交易子类型  默认00
        data.put("bizType", "000201");                       //业务类型
        data.put("channelType", "08");                       //渠道类型，07-PC，08-手机

        /***商户接入参数***/
        data.put("merId", acpsdkMerId);                //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("accessType", "0");                         //接入类型，商户接入固定填0，不需修改
        data.put("orderId", getCurrentTime()+orderItem.getId());          //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
        data.put("txnTime", getCurrentTime());      //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put("currencyCode", "156");                     //交易币种（境内商户一般是156 人民币）
        data.put("txnAmt", orderItem.getRefundAmount().toString());                          //****退货金额，单位分，不要带小数点。退货金额小于等于原消费金额，当小于的时候可以多次退货至退货累计金额等于原消费金额
        data.put("backUrl", SDKConfig.getConfig().getFrontUrl());               //后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 退货交易 商户通知,其他说明同消费交易的后台通知

        /***要调通交易以下字段必须修改***/
        data.put("origQryId", orderItem.getPaymentCode());      //****原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取

        // 请求方保留域，
        // 透传字段，查询、通知、对账文件中均会原样出现，如有需要请启用并修改自己希望透传的数据。
        // 出现部分特殊字符时可能影响解析，请按下面建议的方式填写：
        // 1. 如果能确定内容不会出现&={}[]"'等符号时，可以直接填写数据，建议的方法如下。
//		data.put("reqReserved", "透传信息1|透传信息2|透传信息3");
        // 2. 内容可能出现&={}[]"'符号时：
        // 1) 如果需要对账文件里能显示，可将字符替换成全角＆＝｛｝【】“‘字符（自己写代码，此处不演示）；
        // 2) 如果对账文件没有显示要求，可做一下base64（如下）。
        //    注意控制数据长度，实际传输的数据长度不能超过1024位。
        //    查询、通知等接口解析时使用new String(Base64.decodeBase64(reqReserved), DemoBase.encoding);解base64后再对数据做后续解析。
//		data.put("reqReserved", Base64.encodeBase64String("任意格式的信息都可以".toString().getBytes(DemoBase.encoding)));

        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文------------->**/
        Map<String, String> reqData  = AcpService.sign(data,"UTF-8");		//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        String url = SDKConfig.getConfig().getBackRequestUrl();									//交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl
        Map<String, String> rspData = AcpService.post(reqData, url,"UTF-8");//这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        if(rspData.get("respCode").equals("00")){
            response.setResult(true);
        }else{
            response.setError("unionpayRefund fail");
        }
        return response;
    }
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    /**
     * 退预售订单货款
     *
     * @param refundAt 退款时间
     * @param deposit  定金
     * @param rest     尾款
     */
    @Override
    public Response<Boolean> refundPresellOrderItem(Date refundAt, OrderItem deposit, OrderItem rest) {
        Response<Boolean> result = new Response<Boolean>();
        if (!Objects.equal(deposit.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
            log.error("this order item (id={}) is not a deposit order item", deposit.getId());
            result.setError("order.item.type.illegal");
            return result;
        }
        if (!Objects.equal(rest.getType(), OrderItem.Type.PRESELL_REST.value())) {
            log.error("this order item(id={}) is not a rest order item", rest.getId());
            result.setError("order.item.type.illegal");
            return result;
        }

        // 对于协商退款，尾款金额在数据库中为负数的处理
        this.makeRefundData(deposit, rest);

        List<AlipayRefundData> refunds = Lists.newArrayListWithCapacity(2);
        String batchNo = RefundRequest.toBatchNo(refundAt, rest.getId());
        // 如果不是微信支付定金，则加入 refunds
        if (!Objects.equal(deposit.getPaymentPlatform(), "2")) {
            if (deposit.getRefundAmount() != null && deposit.getRefundAmount() > 0) {
                AlipayRefundData depositRefund = new AlipayRefundData(deposit.getPaymentCode(),
                        deposit.getRefundAmount(), deposit.getReason());
                refunds.add(depositRefund);
            }
        }

        if (Objects.equal(rest.getPayType(), OrderItem.PayType.ONLINE.value())) {   //
            if (!Objects.equal(rest.getPaymentPlatform(), "2")) {
                if (rest.getRefundAmount() != null && rest.getRefundAmount() > 0) {
                    AlipayRefundData restRefund = new AlipayRefundData(rest.getPaymentCode(),
                            rest.getRefundAmount(), rest.getReason());
                    refunds.add(restRefund);
                }
            }
        }

        // 微信支付退款处理逻辑
        Response<Boolean> wxPerSaleRes = this.wxPayPerSaleRefund(deposit, rest);
        if (!wxPerSaleRes.isSuccess()) {
            result.setError(wxPerSaleRes.getError());
            return result;
        }

        if (refunds.size() == 0) {
            result.setResult(Boolean.TRUE);
            return result;
        }

        CallBack notify = new CallBack(notifyUrl);
        Response<Boolean> refundByAlipay = RefundRequest.build(token).batch(batchNo)
                .detail(refunds).notify(notify).refund();
        if (!refundByAlipay.isSuccess()) {
            log.error("fail to refund to buyer(id={}) for order(id={}), error code:{}",
                    deposit.getBuyerId(), deposit.getOrderId(), refundByAlipay.getError());
            result.setError("alipay.refund.fail");
            return result;
        }
        result.setResult(Boolean.TRUE);
        return result;
    }

    @Override
    public Response<Boolean> updateOrderAndOrderItems(Long orderId, Integer status) {
        Response<Boolean> result = new Response<Boolean>();
        if (orderId == null || status == null) {
            log.error("both orderId and status can not be null");
            result.setError("illegal.params");
            return result;
        }
        Order exist = orderDao.findById(orderId);
        if (exist == null) {
            log.error("order{id={}} not found", orderId);
            result.setError("order.not.found");
            return result;
        }
        try {
            orderManager.updateOrderAndOrderItems(orderId, status);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to update order and orderItems, cause:{}", e);
            result.setError("order.update.status.fail");
            return result;
        }
    }


    @Override
    public Response<Boolean> expireOrder(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(notNull(id), "order.id.null");
            Order order = orderDao.findById(id);
            checkState(notNull(order), "order.not.found");
            checkState(notNull(order.getCreatedAt()), "order.created.at.null");

            if (order.getCreatedAt() != null &&
                    Days.daysBetween(new DateTime(order.getCreatedAt()), DateTime.now()).getDays() < ExpireTimes.NOT_CONFIRM_EXPIRE_DAY) {
                Date newCreatedAt = DateTime.now().minusDays(8).toDate();
                order.setCreatedAt(newCreatedAt);
            }

            if (order.getDeliveredAt() != null &&
                    Days.daysBetween(new DateTime(order.getDeliveredAt()), DateTime.now()).getDays() < ExpireTimes.NOT_CONFIRM_EXPIRE_DAY) {
                Date newDeliveredAt = DateTime.now().minusDays(8).toDate();
                order.setDeliveredAt(newDeliveredAt);
            }


            boolean success = orderDao.expired(order);
            checkState(success, "order.update.fail");


        } catch (IllegalArgumentException e) {
            log.error("fail to expire order, cause:{}", e);
            result.setError(e.getMessage());
            return result;
        } catch (IllegalStateException e) {
            log.error("fail to expire order, cause:{}", e);
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("fail to expire order, cause:{}", e);
            result.setError("order.update.fail");
            return result;
        }

        return result;
    }

    @Override
    public Response<Boolean> updateDeliverFee(Long sellerId, Long orderItemId, Integer newFee) {
        Response<Boolean> result = new Response<Boolean>();

        if(orderItemId == null) {
            log.error("change order item deliver fee need orderItemId");
            result.setError("order.item.id.null");
            return result;
        }

        if(newFee == null) {
            log.error("change order item deliver fee need newFee");
            result.setError("order.item.newFee.null");
            return result;
        }

        if(newFee < 0){
            log.error("change order item deliver fee can't be less than 0");
            result.setError("order.item.newFee.less");
            return result;
        }

        if(sellerId == null) {
            log.error("change order item deliver fee need sellerId");
            result.setError("order.item.sellerId.null");
            return result;
        }

        try {
            //验证是否有更改权限
            OrderItem orderItem = orderItemDao.findById(orderItemId);
            if (!Objects.equal(orderItem.getSellerId(), sellerId)) {
                log.error("seller don't have power to change order item deliver fee.");
                result.setError("order.item.not.power");
                return  result;
            }

            orderManager.updateDeliverFee(orderItemId , newFee);
            result.setResult(true);
        }catch(Exception e) {
            log.error("change order item deliver fee failed , sellerId={}, orderItemId={}, newFee={}, error code={}",
                    sellerId, orderItemId, newFee, Throwables.getStackTraceAsString(e));
            result.setError("order.item.change.failed");
        }

        return result;
    }


    @Override
    public Response<Boolean> updateElectInvoice(Long orderId, String invoiceNo, String url) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            OrderExtra extra = orderExtraDao.findByOrderId(orderId);
            checkState(notNull(extra), "order.extra.not.found");

            String invoice = extra.getInvoice();
            checkState(notEmpty(invoice), "invoice.not.found");

            // 校验发票类型
            Map<String,String> mappedInvoice = mapper.fromJson(extra.getInvoice(), invoiceType);
            String type = mappedInvoice.get("type");
            checkState(notEmpty(type), "invoice.type.can.not.be.empty");
            checkState(equalWith(type, OrderExtra.Type.ELECT.value()), "not.elect.invoice");

            mappedInvoice.put("invoiceNo", invoiceNo);
            mappedInvoice.put("invoiceUrl", url);

            OrderExtra updating = new OrderExtra();
            updating.setOrderId(extra.getOrderId());
            updating.setInvoice(mapper.toJson(mappedInvoice));

            boolean success = orderExtraDao.updateByOrderId(updating);
            checkState(success, "order.extra.persist.fail");
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to update invoice of Order(id:{}), invoice:{}, url:{}, error:{}",
                    orderId, invoiceNo, url, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to update invoice of Order(id:{}), invoice:{}, url:{}, error:{}",
                    orderId, invoiceNo, url, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to update invoice of Order(id:{}), invoice:{}, url:{}, cause:{}",
                    orderId, invoiceNo, url, Throwables.getStackTraceAsString(e));
            result.setError("elect.invoice.update.fail");
        }

        return result;
    }


    @Override
    public Response<Boolean> update4Fix(Order updating, List<OrderItem> orderItems) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            orderManager.updateOrderAndOrderItems4Fix(updating, orderItems);
            result.setResult(Boolean.TRUE);

        } catch (IllegalStateException e) {
            log.error("fail to update order:{}, orderItems:{}, error:{}", updating, orderItems, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to update order:{}, orderItems:{}, cause:{}", updating, orderItems, Throwables.getStackTraceAsString(e));
            result.setError("order.update.fail");
        }

        return result;
    }

    @Override
    public Response<Boolean> updateOrder(Order order) {
        Response<Boolean> result = new Response<Boolean>();

        if(order == null) {
            log.error("params null when update order");
            result.setError("illegal.param");
            return result;
        }

        try {
            orderDao.update(order);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update order {}, cause:{}", order, Throwables.getStackTraceAsString(e));
            result.setError("order.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> bathUpdateOrderItemStatusByOrderId(Integer fromStatus, Integer toStatus, Long orderId, String paymentCode) {
        Response<Boolean> result = new Response<Boolean>();

        if(fromStatus == null || toStatus == null || orderId == null) {
            log.error("params null when batch update orderItem status by orderId");
            result.setError("illegal.param");
            return result;
        }

        try {
            orderManager.bathUpdateOrderItemStatusByOrderId(fromStatus, toStatus, orderId, paymentCode);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to batch update orderItem status by orderId, fromStatus={}, toStatus={}, orderId={},error code={}",
                    fromStatus,toStatus,orderId,Throwables.getStackTraceAsString(e));
            result.setError("batch.update.orderItem.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateOrderItem(OrderItem orderItem) {
        Response<Boolean> result = new Response<Boolean>();

        if(orderItem == null) {
            log.error("illegal param when update orderItem");
            result.setError("illegal.param");
            return result;
        }

        try {
            orderItemDao.update(orderItem);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update orderItem {}, cause:{}", orderItem, Throwables.getStackTraceAsString(e));
            result.setError("update.orderItem.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> cancelOrder(Long orderId, Integer toStatus) {
        Response<Boolean> result = new Response<Boolean>();

        if(orderId == null || toStatus == null) {
            log.error("params null when cancel order");
            result.setError("illegal.param");
            return result;
        }

        try {
            orderManager.cancelOrder(orderId, toStatus);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to cancel order by id={}, toStatus={}, cause:{}", orderId, toStatus, Throwables.getStackTraceAsString(e));
            result.setError("cancel.order.fail");
            return result;
        }
    }

    /**
     * 重置一个子订单的id
     *
     * @param oi        需要重置的子订单
     * @param channel   渠道
     * @return 重置后的子订单
     */
    @Override
    public Response<OrderItem> resetOrderItem(OrderItem oi, String channel) {
        Response<OrderItem> result = new Response<OrderItem>();

        try {
            OrderItem newOrderItem = orderManager.resetOrderItem(oi, channel);
            result.setResult(newOrderItem);

        } catch (IllegalStateException e) {
            log.error("fail to reset orderItem with oid:{}, oi:{}, error:{}", oi.getOrderId(), oi, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to reset orderItem with oid:{}, oi:{}, cause:{}", oi.getOrderId(), oi, Throwables.getStackTraceAsString(e));
            result.setError("order.item.reset.fail");
        }

        return result;
    }

    @Override
    public Response<Order> resetOrder(Order order, String channel) {

        Response<Order> result = new Response<Order>();

        try {
            Order newOrder = orderManager.resetOrder(order, channel);
            result.setResult(newOrder);

        } catch (IllegalStateException e) {
            log.error("fail to reset order with order:{}, error:{}", order, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to reset order with order:{}, cause:{}", order, Throwables.getStackTraceAsString(e));
            result.setError("order.reset.fail");
        }

        return result;
    }


    @Override
    public void updateNbOrderItem(OrderItem orderItems) {
        orderItemDao.update(orderItems);
    }

    @Override
    public Response<OrderIdAndEarnestId> preSaleOrderCreate(Order order, OrderItem earnest, OrderItem remain, OrderExtra orderExtra) {
        Response<OrderIdAndEarnestId> result = new Response<OrderIdAndEarnestId>();

        try {
            OrderIdAndEarnestId orderIdAndEarnestId = orderManager.preSaleOrderCreate(order, earnest, remain, orderExtra);
            result.setResult(orderIdAndEarnestId);
            return result;
        }catch (Exception e) {
            log.error("fail to create preSale order by order={}, earnest={}, remain={}, orderExtra={},cause:{}",
                    order,earnest,remain,orderExtra,Throwables.getStackTraceAsString(e));
            result.setError("preSale.order.create.fail");
            return result;
        }
    }

    /**
     * 创建抢购订单，保证事务
     *
     * @param order      总订单
     * @param orderItem  子订单
     * @param orderExtra 订单其他信息
     * @return 创建成功的订单id
     */
    @Override
    public Response<Long> buyingOrderCreate(Order order, OrderItem orderItem, OrderExtra orderExtra) {

        Response<Long> result = new Response<Long>();

        try {

            Long orderId= orderManager.buyingOrderCreate(order, orderItem, orderExtra);

            result.setResult(orderId);
            return result;
        }catch (Exception e) {
            log.error("fail to buying create order by order {}, orderItem {}, orderExtra {}, cause:{}",
                    order, orderItem, orderExtra, Throwables.getStackTraceAsString(e));
            result.setError("order.create.fail");
            return result;
        }
    }


    /**
     * 创建试金行动订单
     *
     * @param buyerId     买家id
     * @param tradeInfoId 收货信息id
     * @param fatOrders   买家提交的订单
     * @return sellerId 和 新创建的订单id列表 key-value对
     */
    @Override
    public Response<Map<Long,Long>> createForSku(Long buyerId, Long tradeInfoId,
                                                 List<FatOrder> fatOrders,Map<Long,Integer> skuIdAndDiscount,String bank) {
        Response<Map<Long,Long>> result = new Response<Map<Long,Long>>();
        if (buyerId == null || tradeInfoId == null || fatOrders == null || fatOrders.isEmpty()) {
            log.error("buyerId and tradeInfoId and fatOrders all required");
            result.setError("illegal.param");
            return result;
        }
        try {
            Map<Long, SkuAndItem> skuAndItems = prepareOrder(fatOrders);

            OrderResult orderResult = orderManager.createForSku(buyerId, tradeInfoId, fatOrders, skuAndItems, skuIdAndDiscount, bank);
            //减库存及增加销量
            for (StockChange stockChange : orderResult.getStockChanges()) {
                Response<Boolean> dr = itemService.changeSoldQuantityAndStock(stockChange.getSkuId(), stockChange.getItemId(), -stockChange.getQuantity());
                if (!dr.isSuccess()) {
                    log.error("failed to decrement stock of sku(id={}) and item(id={}),error code:{}",
                            stockChange.getSkuId(), stockChange.getItemId(), dr.getError());
                }
            }
            result.setResult(orderResult.getSellerIdAndOrderId());
            return result;
        } catch (Exception e) {
            log.error("failed to create orders from {} for buyer(id={}): cause:{}",
                    fatOrders, buyerId, Throwables.getStackTraceAsString(e));
            result.setError("order.create.fail");
            return result;
        }
    }

    /**
     * 更新退款订单状态，外部调用
     *
     * @param tempReturnId     临时逆向订单id
     * @param status 订单状态
     * @return 操作是否成功
     */
    public Response<Boolean> updateStatusForTempReturn(String tempReturnId, Integer status) {
        Response<Boolean> result = new Response<Boolean>();
        try {

            checkState(tempReturnId.startsWith("R"), "order.update.status.fail");

            Long id = Long.valueOf(tempReturnId.substring(tempReturnId.indexOf("R")));

            Order order = orderDao.findById(id);

            checkState(notNull(order), "order.not.found");
            checkState(Objects.equal(status, OrderItem.Status.CANCELED_BY_RETURNGOODS.value())
                            || Objects.equal(status, OrderItem.Status.WAIT_FOR_REFUND.value()),
                    "order.update.status.fail");

            OrderItem updated = new OrderItem();
            updated.setId(id);
            updated.setStatus(OrderItem.Status.CANCELED_BY_REFUND.value());
            orderItemDao.update(updated);
            orderManager.cancelOrder(id, OrderItem.Status.CANCELED_BY_REFUND.value());

            result.setResult(Boolean.TRUE);
            return result;
        } catch (ServiceException e) {
            log.error("fail to invoke updateStatusForTempReturn with tempReturnId={}, status={}, error:{}", tempReturnId, status, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (IllegalStateException e) {
            log.error("fail to invoke updateStatusForTempReturn with tempReturnId={}, status={}, error:{}", tempReturnId, status, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("fail to invoke updateStatusForTempReturn with tempReturnId={}, status={}", tempReturnId, status, e);
            result.setError("order.update.status.fail");
            return result;
        }
    }

    /**
     * 退预授权普通订单货款
     *
     * @param refundAt  退款时间
     * @param orderItem 子订单
     */
    @Override
    public Response<Boolean> refundPlainOrderItem2(Date refundAt, OrderItem orderItem) {
        Response<Boolean> result = new Response<Boolean>();
        if (!Objects.equal(orderItem.getType(), OrderItem.Type.PLAIN.value())) {
            log.error("this orderItem(id={}) is not a plain order item(type=1), it's type is:{}", orderItem.getId(), orderItem.getType());
            result.setError("order.item.type.illegal");
            return result;
        }

        if (Objects.equal(orderItem.getPayType(), OrderItem.PayType.COD.value())) {
            //更新货到付款退货状态
            Response<Boolean> cancelResult = cancelOrderItem(orderItem.getId());
            if (!cancelResult.isSuccess()) {
                log.error("failed to refund {}, error code:{}", orderItem, cancelResult.getError());
                result.setError(cancelResult.getError());
                return result;
            }

            result.setResult(Boolean.TRUE);
            return result;
        }

//        String batchNo = RefundRequest.toBatchNo(refundAt, orderItem.getId());
//        AlipayRefundData refund = new AlipayRefundData(orderItem.getPaymentCode(),
//                orderItem.getRefundAmount(), orderItem.getReason());
//
//        CallBack notify = new CallBack(notifyUrl);
//        Response<Boolean> refundByAlipay = RefundRequest.build(token).batch(batchNo)
//                .detail(Lists.newArrayList(refund)).notify(notify).refund();

        CallBack notify1 = new CallBack(notifyUrl);
        Response<Boolean> refundByAlipay = UnFreezeRequest.build(token).authNo("2014121500002001790000000016").outRequestNo("2014121500002001790000000016").remark("ceshi").amount(7).notify(notify1).refund();

        if (!refundByAlipay.isSuccess()) {
            log.error("fail to refund to {}, error code:{}", orderItem, refundByAlipay.getError());
            result.setError(refundByAlipay.getError());
            return result;
        }

        result.setResult(Boolean.TRUE);
        return result;
    }

    /**
     * 卖家发货
     *
     * @param order 订单
     */
    @Override
    public Response<Boolean> deliver2(Order order, OrderLogisticsInfoDto orderLogisticsInfoDto, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        Long sellerId = user.getId();
        if (order == null || sellerId == null) {
            log.error("both order and sellerId can not be null");
            result.setError("illegal.param");
            return result;
        }
        if (!Objects.equal(order.getSellerId(), sellerId) &&
                !Objects.equal(user.getType(), User.TYPE.ADMIN.toNumber())) {
            log.error("current user don't have the right");
            result.setError("authorize.fail");
            return result;
        }
        try {
            orderManager.deliver2(order, orderLogisticsInfoDto);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to deliver for order ({}),cause:{}", order, Throwables.getStackTraceAsString(e));
            result.setError("order.deliver.fail");
            return result;
        }

    }

    /**
     * 试金购买，退订
     *
     * @param orderId 订单
     */
    @Override
    public Response<Boolean> updateOrderCallBack(Long orderId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            result = orderManager.updateOrderCallBack(orderId);

        } catch (Exception e) {
            log.error("failed to deliver for order ({}),cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("order.deliver.fail");
            result.setSuccess(Boolean.FALSE);
        }
        return result;
    }

    @Override
    public Response<Boolean> updateOrderItemType(OrderItem orderItem) {
        Response<Boolean> result = new Response<Boolean>();

        if(orderItem == null) {
            log.error("illegal param when update orderItem");
            result.setError("illegal.param");
            return result;
        }

        try {
            orderItemDao.updateOrderIdType(orderItem);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update orderItem {}, cause:{}", orderItem, Throwables.getStackTraceAsString(e));
            result.setError("update.orderItem.fail");
            return result;
        }
    }


    /**
     * 是否试金订单
     *
     * @param orderId 订单号
     */
    @Override
    public boolean checkPreDeposit(Long orderId){
        PreAuthorizationDepositOrder preAuthorizationDepositOrder =
                preAuthorizationDao.findOneByOrderId(orderId);//预授权信息

        if(isNull(preAuthorizationDepositOrder)){
            return false;
        }
        return true;
    }

    /**
     * 是否试金订单
     *
     * @param orderId 订单号
     */
    @Override
    public Boolean checkPreDepositPayOrBack(Long orderId){
        PreAuthorizationDepositOrder preAuthorizationDepositOrder =
                preAuthorizationDao.findOneByOrderId(orderId);//预授权信息

        if(preAuthorizationDepositOrder.getStatus() == PreAuthorizationDepositOrder.DepositPayType.PAYED.value() ||
                preAuthorizationDepositOrder.getStatus() == PreAuthorizationDepositOrder.DepositPayType.PAYFINNSH.value()){
            return Boolean.TRUE ;

        }else if (preAuthorizationDepositOrder.getStatus() ==  PreAuthorizationDepositOrder.DepositPayType.DELIVERED.value()){
            return Boolean.FALSE;
        }else{
            log.error("preOrder statueP{} !=-2 and statue !=1 , return directly");
            return null;
        }
    }

    /**
     * 退款接口调用成功后订单处理逻辑
     * @param orderItem
     * @return
     */
    @Transactional
    private Response<Boolean> updateOrderStatus (OrderItem orderItem) {
        Response<Boolean> response = new Response<Boolean>();
        // 订单已退款或退货成功, 则不进行后续步骤
        if(Objects.equal(orderItem.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())
                || Objects.equal(orderItem.getStatus(), OrderItem.Status.CANCELED_BY_RETURNGOODS.value())) {
            log.error("refund has finished");
            response.setError("refund has finished");
            return response;
        }

        // 判断是否试金行动订单
        Boolean checkPreDeposit = this.checkPreDeposit(orderItem.getOrderId());
        if (checkPreDeposit) {
            //判断购买回调库存
            Boolean bool = this.checkPreDepositPayOrBack(orderItem.getOrderId());
            Response<Boolean> result = new Response<Boolean>();
            if(bool==null){
                log.error("fail to get preOrder statue");
                response.setError("fail to get preOrder statue");
                return response;
            } else {
                if(bool){
                    //对于分仓的预售还要恢复库存
                    result  = preDepositService.recoverPreDepositStorageIfNecessary(orderItem.getOrderId());
                    if(!result.isSuccess()){
                        log.error("fail to update Storage data{}, error code={}", orderItem.getOrderId(), result.getError());
                        response.setError(result.getError());
                        return response;
                    }
                }
                result = this.updateOrderCallBack(orderItem.getOrderId());
                if (!result.isSuccess()) {
                    log.error("fail to update order data{}, error code={}", orderItem.getOrderId(), result.getError());
                    response.setError(result.getError());
                    return response;
                }
            }
        } else {
            //对于分仓的预售还要恢复库存
            Response<Boolean> storageR = preSaleService.recoverPreSaleStorageIfNecessary(orderItem.getOrderId());
            if(!storageR.isSuccess()){
                log.error("failed to recover storage for order(id={}), error code:{}", orderItem.getOrderId(), storageR.getError());
                response.setError(storageR.getError());
                return response;
            }
            // 更新订单退款状态
            Response<Boolean> cancel = this.cancelOrderItem(orderItem.getId());   // 标记退款或退货成功
            if (!cancel.isSuccess()) {
                log.error("fail to update order status cause {}", cancel.getError());
                response.setError(cancel.getError());
                return response;
            }
        }
        response.setResult(true);
        return response;
    }

    /**
     * 标记orders / orderItems 表中的支付平台和支付渠道
     * @param orders
     * @param orderItems
     * @param isPreSale 0 普通订单 1 预售/试用订单
     * @return
     */
    @Override
    @Transactional
    public Response<Boolean> setPaymentPlatform(List<Order> orders, List<OrderItem> orderItems, int isPreSale) {
        Response<Boolean> response = new Response<Boolean>();
        try {
            if (Objects.equal(isPreSale, 0)) {
                for (Order order : orders) {
                    orderDao.update(order);
                    List<OrderItem> orderItemList = orderItemDao.findByOrderId(order.getId());
                    for (OrderItem orderItem : orderItemList) {
                        orderItem.setPayType(order.getPaymentType());
                        orderItem.setPaymentPlatform(order.getPaymentPlatform());
                        orderItem.setChannel(order.getChannel());
                        orderItemDao.update(orderItem);
                    }
                }
            } else if (Objects.equal(isPreSale, 1)) {
                for (Order order : orders) {
                    orderDao.update(order);
                }
                for (OrderItem orderItem : orderItems) {
                    orderItemDao.update(orderItem);
                }
            }
        } catch (Exception e) {
            log.error("fail to update payment platform");
            response.setError("fail to update payment platform");
            return response;
        }
        response.setResult(true);
        return response;
    }





    /**
     * 微信支付 预售订单 退款
     * @param deposit
     * @param rest
     * @return
     */
    private Response<Boolean> wxPayPerSaleRefund (OrderItem deposit, OrderItem rest) {
        Response<Boolean> result = new Response<Boolean>();
        // 如果 定金 和 尾款 都是微信支付
        if (Objects.equal(deposit.getPaymentPlatform(), "2") && Objects.equal(rest.getPaymentPlatform(), "2")) {
            // 因为预售订单申请退款，只有尾款子订单的状态是 4 ，此处若要通过微信退定金，则必须先把定金子订单的状态改成 4
            OrderItem upd = new OrderItem();
            upd.setId(deposit.getId());
            upd.setStatus(OrderItem.Status.WAIT_FOR_REFUND.value());
            orderItemDao.update(upd);
            // 退定金
            Response<Boolean> wxRefundRes = payRefund(deposit);
            if (!wxRefundRes.isSuccess()) {
                log.error(wxRefundRes.getError());
                result.setError(wxRefundRes.getError());
                return result;
            }
            // 退尾款  因为退定金的时候已经更改状态了，这里只发送退款请求
            if (rest.getRefundAmount() == null) {
                rest.setRefundAmount(rest.getFee());
            }
            if (rest.getRefundAmount() > 0) {
                Response<Boolean> responseR = wxRefund(rest);
                if (!responseR.isSuccess()) {
                    log.error("wx pay fail to refund to {}, error code:{}", rest, responseR.getError());
                    result.setError(responseR.getError());
                    return result;
                }
            }
        }
        // 定金用微信支付 尾款用支付宝支付
        if (Objects.equal(deposit.getPaymentPlatform(), "2") && !Objects.equal(rest.getPaymentPlatform(), "2")) {
            // 因为预售订单申请退款，只有尾款子订单的状态是 4 ，此处若要通过微信退定金，则必须先把定金子订单的状态改成 4
            OrderItem upd = new OrderItem();
            upd.setId(deposit.getId());
            upd.setStatus(OrderItem.Status.WAIT_FOR_REFUND.value());
            orderItemDao.update(upd);
            // 退定金
            Response<Boolean> wxRefundRes = this.payRefund(deposit);
            if (!wxRefundRes.isSuccess()) {
                log.error(wxRefundRes.getError());
                result.setError(wxRefundRes.getError());
                return result;
            }
        }
        // 定金用支付宝支付 尾款用微信支付
        if (!Objects.equal(deposit.getPaymentPlatform(), "2") && Objects.equal(rest.getPaymentPlatform(), "2")) {
            // 退尾款
            Response<Boolean> wxRefundRes = payRefund(rest);
            if (!wxRefundRes.isSuccess()) {
                log.error(wxRefundRes.getError());
                result.setError(wxRefundRes.getError());
                return result;
            }
        }
        result.setResult(Boolean.TRUE);
        return result;
    }

    /**
     * 处理协商退款时，若退款金额小于定金金额，则造成尾款退款金额为负数的情况
     * 这种情况，优先从定金中退
     * @param deposit
     * @param rest
     */
    private void makeRefundData (OrderItem deposit, OrderItem rest) {
        if (rest.getRefundAmount() < 0) {
            if (deposit.getRefundAmount() == null) {
                deposit.setRefundAmount(0);
            }
            if (rest.getRefundAmount() == null) {
                rest.setRefundAmount(0);
            }
            Integer refundAmount = deposit.getRefundAmount() + rest.getRefundAmount();
            rest.setRefundAmount(0);
            deposit.setRefundAmount(refundAmount);
        }
    }
}
