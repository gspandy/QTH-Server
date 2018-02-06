package com.nowbook.rlt.presale.manager;

import com.nowbook.category.model.Spu;
import com.nowbook.common.model.Response;
import com.nowbook.common.utils.BeanMapper;
import com.nowbook.common.utils.JsonMapper;
import com.nowbook.exception.ServiceException;
import com.nowbook.item.model.*;
import com.nowbook.item.service.ItemService;
import com.nowbook.rlt.code.dto.DiscountAndUsage;
import com.nowbook.rlt.code.model.CodeUsage;
import com.nowbook.rlt.code.service.ActivityCodeService;
import com.nowbook.rlt.code.service.CodeUsageService;
import com.nowbook.rlt.presale.dao.*;
import com.nowbook.rlt.presale.dto.FatOrderPreSale;
import com.nowbook.rlt.presale.model.AddressStorage;
import com.nowbook.rlt.presale.model.PreSale;
import com.nowbook.shop.model.Shop;
import com.nowbook.shop.service.ShopService;
import com.nowbook.trade.dto.OrderIdAndEarnestId;
import com.nowbook.trade.model.*;
import com.nowbook.trade.service.DeliveryMethodService;
import com.nowbook.trade.service.OrderWriteService;
import com.nowbook.trade.service.UserVatInvoiceService;
import com.nowbook.user.enums.Business;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nowbook.common.utils.Arguments.*;
import static com.nowbook.common.utils.NumberValidator.gt0;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yangzefeng on 14-2-12
 */
@Component
public class PreSaleManager {

    private final static Long ADMIN_SHOP_ID = 0l;

    private final static Integer DEFAULT_ITEM_QUANTITY = 10000000;

    private final static String COUNTRY_REGION = "0";

    private static final JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    private static final JavaType javaType = jsonMapper.createCollectionType(ArrayList.class, BaseSku.class);

    private static final Logger log = LoggerFactory.getLogger(PreSaleManager.class);

    private static final Splitter splitter = Splitter.on(" ").trimResults().omitEmptyStrings();

    @Autowired
    private ItemService itemService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private PreSaleDao preSaleDao;

    @Autowired
    private PreSaleRedisDao preSaleRedisDao;

    @Autowired
    private CodeUsageService codeUsageService;

    @Autowired
    private ActivityCodeService activityCodeService;

    @Autowired
    private UserVatInvoiceService userVatInvoiceService;

    @Autowired
    private StorageStockDao storageStockDao;

    @Autowired
    private AddressStorageDao addressStorageDao;

    @Autowired
    private PreSaleBuyLimitDao preSaleBuyLimitDao;

    @Autowired
    private DeliveryMethodService deliveryMethodService;

    private static final JsonMapper mapper = JsonMapper.nonDefaultMapper();

    private List<BaseSku> getValidBaseSkus(List<BaseSku> origin) {
        List<BaseSku> baseSkus = Lists.newArrayListWithCapacity(origin.size());
        for (BaseSku bs : origin) {
            //如果三个字段都没有填写, 则不产生sku
            if (bs.getPrice() == null &&
                    Strings.isNullOrEmpty(bs.getOuterId()) &&
                    Strings.isNullOrEmpty(bs.getModel())) {
                continue;
            }
            baseSkus.add(bs);
        }

        return baseSkus;
    }


    @Transactional
    public void create(PreSale preSale, Spu spu, DefaultItem defaultItem) {
        Long spuId = preSale.getSpuId();
        checkArgument(gt0(spuId), "spu.id.invalid");

        List<BaseSku> origin = jsonMapper.fromJson(defaultItem.getJsonSkus(), javaType);
        checkState(notNull(origin) && not(origin.isEmpty()), "default.item.skus.null");

        List<BaseSku> baseSkus = getValidBaseSkus(origin);
        checkState(not(baseSkus.isEmpty()), "default.Item.sku.info.incomplete");


        // 创建商品
        Item item = getItem(preSale, spu, defaultItem, baseSkus);

        // 创建商品详情
        ItemDetail itemDetail = getItemDetail(defaultItem);

        // 创建skus
        List<Sku> skus = getSkus(baseSkus, item);

        Response<Long> itemIdR = itemService.create(item, itemDetail, skus, Boolean.FALSE);
        checkState(itemIdR.isSuccess(), itemIdR.getError());
        Long itemId = itemIdR.getResult();

        // create preSale
        preSale.setItemId(itemId);
        preSaleDao.create(preSale);
    }

    private List<Sku> getSkus(List<BaseSku> baseSkus, Item item) {
        List<Sku> skus = Lists.newArrayListWithCapacity(baseSkus.size());

        for (BaseSku baseSku : baseSkus) {
            Sku sku = new Sku();
            BeanMapper.copy(baseSku, sku);
            sku.setPrice(item.getPrice());
            sku.setStock(DEFAULT_ITEM_QUANTITY);
            skus.add(sku);
        }
        return skus;
    }

    private ItemDetail getItemDetail(DefaultItem defaultItem) {
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setImage1(defaultItem.getImage1());
        itemDetail.setImage2(defaultItem.getImage2());
        itemDetail.setImage3(defaultItem.getImage3());
        itemDetail.setImage4(defaultItem.getImage4());
        return itemDetail;
    }

    private Item getItem(PreSale preSale, Spu spu, DefaultItem defaultItem, List<BaseSku> baseSkus) {
        Item item = new Item();
        item.setSpuId(spu.getId());


        item.setUserId(getUserIdFromShopIds(preSale.getShopIds()));
        item.setShopId(ADMIN_SHOP_ID);
        item.setName(defaultItem.getName());
        item.setMainImage(defaultItem.getMainImage());
        item.setTradeType(Item.TradeType.BUY_OUT.toNumber());
        item.setStatus(Item.Status.ON_SHELF.toNumber());
        item.setQuantity(baseSkus.size() * DEFAULT_ITEM_QUANTITY);
        item.setSoldQuantity(0);

        int price = preSale.getEarnest() + preSale.getRemainMoney();
        item.setPrice(price);
        item.setOriginPrice(preSale.getPrice());
        item.setRegion(COUNTRY_REGION);
        item.setBrandId(Long.valueOf(spu.getBrandId()));
        item.setOnShelfAt(new Date());
        return item;
    }

    private Long getUserIdFromShopIds(String originShopIds) {
        List<Long> shopIds = Lists.transform(splitter.splitToList(originShopIds), new Function<String, Long>() {
            @Override
            public Long apply(String input) {
                return Long.valueOf(input);
            }
        });
        //按约定，如果有多个店铺id取第一个
        Long shopId = shopIds.get(0);
        Response<Shop> shopR = shopService.findById(shopId);
        if(!shopR.isSuccess()) {
            log.error("fail to find shop by id = {} when create presale, error code:{}", shopId, shopR.getError());
            throw new ServiceException("shop.query.fail");
        }
        return shopR.getResult().getUserId();
    }

    @Transactional
    public void release(PreSale preSale) {
        preSaleDao.update(preSale);
        //add index to all release preSale list
        preSaleRedisDao.addPreSaleToAllReleasePreSaleList(preSale.getId());
    }


    private String getPersonalInvoice() {
        Map<String, String> mapped = Maps.newTreeMap();
        mapped.put("title", "个人");
        mapped.put("type", OrderExtra.Type.PLAIN.value());
        return mapper.toJson(mapped);
    }

    @Transactional
    public Long createPreSaleOrders(Long buyerId, Long tradeInfoId, Integer regionId,
                                    FatOrderPreSale fatOrderPreSale, Long businessId, PreSale preSale,
                                    DiscountAndUsage discountAndUsage,String bank) {

        Map<Long, Integer> skuIdAndDiscount = discountAndUsage.getSkuIdAndDiscount();
        Map<Long, CodeUsage> sellerIdAndCodeUsage = discountAndUsage.getSellerIdAndUsage();
        //使用数量
        Map<Long, Integer> activityCodeIdAndUsage = discountAndUsage.getActivityCodeIdAndUsage();

        Order order = new Order();
        //如果卖家和买家一样则不创建订单
        if (Objects.equal(fatOrderPreSale.getSellerId(), buyerId)) {
            log.warn("buyerId can not same as sellerId={}, this order will be ignored", buyerId);
            throw new ServiceException("buyer.same.as.seller");
        }
        if (new DateTime(preSale.getPreSaleFinishAt()).isBeforeNow()) {
            log.warn("preSale(id={}) has finished", fatOrderPreSale.getPreSale().getId());
            throw new ServiceException("presale.has.expired");
        }
        order.setChannel(bank); //添加支付渠道
        order.setSellerId(fatOrderPreSale.getSellerId());
        order.setBuyerId(buyerId);
        order.setTradeInfoId(tradeInfoId);
        order.setType(Order.Type.PRE_SELL.value());
        order.setPaymentType(fatOrderPreSale.getPaymentType());
        order.setStatus(Order.Status.WAIT_FOR_PAY.value());
        order.setBusiness(businessId);
        // 运费暂时都是0
        order.setDeliverFee(0);
        Long skuId = null;
        Sku sku = null;
        Integer quantity = 0;
        //预售优惠价格只对尾款有效
        Integer discount = 0;
        for (Long id : fatOrderPreSale.getSkuIdAndQuantity().keySet()) {
            skuId = id;
            quantity = fatOrderPreSale.getSkuIdAndQuantity().get(skuId);
            Response<Sku> skuR = itemService.findSkuById(skuId);
            if (!skuR.isSuccess()) {
                log.error("fail to find sku by id={} when create preSale order, skip it", skuId);
                continue;
            }
            sku = skuR.getResult();

            //优惠码优惠价格
            if (skuIdAndDiscount != null) {
                discount = skuIdAndDiscount.get(skuId);
                discount = discount != null ? discount : 0;
            }
        }
        order.setFee((fatOrderPreSale.getPreSale().getEarnest()+fatOrderPreSale.getPreSale().getRemainMoney() - discount) * quantity);

        //验证plainBuyLimit
        if (!checkPlainBuyLimit(skuIdAndDiscount, preSale.getPlainBuyLimit(),
                quantity,buyerId,preSale.getId(), preSale.getPreSaleFinishAt())) {
            log.error("fail to buy preSale id={}, buyLimit={}, but now buy quantity is {}",
                    preSale.getId(), preSale.getPlainBuyLimit(), quantity);
            throw new ServiceException("overflow.buy.limit");
        }

        //预售尾款做价格校验
        if(preSale.getRemainMoney() <= discount) {
            log.warn("preSale {} discount {} illegal, sku id={}",preSale, discount, skuId);
            throw new ServiceException("discount.great.than.remain.money");
        }

        if (sku == null) {
            log.error("skuIdAndQuantity can not be null when create preSale order");
            throw new ServiceException("sku.not.found");
        }
        Response<Item> itemR = itemService.findById(sku.getItemId());
        if (!itemR.isSuccess()) {
            log.error("fail to find item by item id={} when create preSale order", sku.getItemId());
            throw new ServiceException("item.not.found");
        }
        Item item = itemR.getResult();
        if(!Objects.equal(item.getStatus(), Item.Status.ON_SHELF.toNumber())) {
            log.error("item id={}, status not on_shelf", item.getId());
            throw new ServiceException("item.status.incorrect");
        }

        OrderExtra orderExtra = new OrderExtra();
        if (notEmpty(fatOrderPreSale.getBuyerNotes()) || fatOrderPreSale.getInvoiceType() != null
                || notEmpty(fatOrderPreSale.getDeliverTime())) {
            orderExtra.setInvoice(fatOrderPreSale.getInvoice());
            orderExtra.setBuyerNotes(fatOrderPreSale.getBuyerNotes());
            orderExtra.setDeliverTime(fatOrderPreSale.getDeliverTime());

            //没有发票信息，如果家电频道默认创建
            if (fatOrderPreSale.getInvoiceType() == null) {
                if (equalWith(order.getBusiness(), Business.GZMH.value())) {
                    orderExtra.setInvoice(getPersonalInvoice());
                }
            }
            if (equalWith(fatOrderPreSale.getInvoiceType(), Integer.valueOf(OrderExtra.Type.VAT.value()))) {
                //增值税发票根据已经填写的信息自动生成
                String vatInvoice = getExistVATInvoice(buyerId);
                if (Strings.isNullOrEmpty(vatInvoice)) {
                    log.error("fail to create order when invoice type is VAT but userVatInvoice is null");
                    throw new ServiceException("vat.invoice.not.found");
                }
                orderExtra.setInvoice(vatInvoice);
            }

        } else if (equalWith(order.getBusiness(), Business.GZMH.value())) {  // 家电频道默认创建个人发票
            orderExtra.setInvoice(getPersonalInvoice());
        }

        //配送承诺
        Response<DeliveryMethod> deliveryMethodR = deliveryMethodService.findById(item.getDeliveryMethodId());

        //创建定金子订单
        OrderItem earnest = new OrderItem();
        earnest.setChannel(bank);//添加支付渠道
        earnest.setBuyerId(buyerId);
        earnest.setSellerId(fatOrderPreSale.getSellerId());
        earnest.setItemId(item.getId());
        earnest.setItemName(item.getName());
        earnest.setBrandId(item.getBrandId());
        earnest.setBusinessId(businessId);
        earnest.setSkuId(skuId);
        earnest.setQuantity(quantity);
        earnest.setFee(preSale.getEarnest() * quantity); //子订单的定金的费用需要乘以quantity
        earnest.setStatus(OrderItem.Status.WAIT_FOR_PAY.value());
        earnest.setType(OrderItem.Type.PRESELL_DEPOSIT.value());
        earnest.setPayType(order.getPaymentType());
        if(!deliveryMethodR.isSuccess() || deliveryMethodR.getResult() == null) {
            log.error("fail to find delivery by id={}, when create pre sale order",item.getDeliveryMethodId());
        }else {
            earnest.setDeliveryPromise(deliveryMethodR.getResult().getName());
        }

        //创建尾款子订单
        OrderItem remainMoney = new OrderItem();
        remainMoney.setChannel(bank);//添加支付渠道
        remainMoney.setBuyerId(buyerId);
        remainMoney.setSellerId(fatOrderPreSale.getSellerId());
        remainMoney.setItemId(item.getId());
        remainMoney.setItemName(item.getName());
        remainMoney.setBrandId(item.getBrandId());
        remainMoney.setBusinessId(businessId);
        remainMoney.setSkuId(skuId);
        remainMoney.setQuantity(quantity);
        remainMoney.setDiscount(discount * quantity);
        remainMoney.setFee((preSale.getRemainMoney() - discount) * quantity); //子订单的尾款的费用需要乘以quantity,优惠码优惠金额算入尾款中
        //货到付款
        if (Objects.equal(Order.PayType.COD.value(), order.getPaymentType())) {
            remainMoney.setStatus(Order.Status.PAID.value());
        } else {
            remainMoney.setStatus(Order.Status.WAIT_FOR_PAY.value());
        }
        remainMoney.setType(OrderItem.Type.PRESELL_REST.value());
        remainMoney.setPayType(order.getPaymentType());

        if(!deliveryMethodR.isSuccess() || deliveryMethodR.getResult() == null) {
            log.error("fail to find delivery by id={}, when create pre sale order",item.getDeliveryMethodId());
        }else {
            remainMoney.setDeliveryPromise(deliveryMethodR.getResult().getName());
        }

        Response<OrderIdAndEarnestId> createR =
                orderWriteService.preSaleOrderCreate(order,earnest,remainMoney,orderExtra);
        if(!createR.isSuccess()) {
            log.error("fail to create preSale order by order={},earnest={},remain={},orderExtra={},error code={}",
                    order,earnest,remainMoney,orderExtra,createR.getError());
            throw new ServiceException(createR.getError());
        }
        OrderIdAndEarnestId orderIdAndEarnestId = createR.getResult();

        Long orderId = orderIdAndEarnestId.getOrderId();

        //增加销量，减少库存(预售商品库存默认无限大)
        itemService.changeSoldQuantityAndStock(skuId, item.getId(), -quantity);

        //如果是分仓的预售, 则要修改分仓的销量
        if (Objects.equal(preSale.getByStorage(), Boolean.TRUE)) {
            //首先根据商品id和地区id查找仓库码
            AddressStorage addressStorage = addressStorageDao.findByItemIdAndAddressId(item.getId(), regionId);
            if (addressStorage == null) {
                log.warn("no addressStorage found by itemId={} and addressId={}", item.getId(), regionId);
                throw new ServiceException("storage.not.found");
            }

            //修改分仓的销量
            Long storageId = addressStorage.getStorageId();
            storageStockDao.changeSoldCount(quantity, item.getId(), storageId);

            //将分仓信息和order信息关联起来, 取消订单恢复库存要用到这个信息
            preSaleRedisDao.addStorageId2PreSaleOrder(orderId, storageId, item.getId(),quantity);
        }


        //订单id存入订单列表
        preSaleRedisDao.addOrderIdToAllPreSaleItemOrdersList(orderId);

        //记录优惠码使用情况
        CodeUsage cu = sellerIdAndCodeUsage.get(fatOrderPreSale.getSellerId());
        if (cu != null) {
            cu.setOrderId(orderId);
            Response<CodeUsage> codeUsageC = codeUsageService.create(cu);
            if (!codeUsageC.isSuccess()) {
                log.error("fail to create code usage={}, error code:{}", cu, createR.getError());
            }
            //调用批量更新优惠码使用数量的接口

            Response<Boolean> codeUsageU = activityCodeService.batchUpdateByIds(activityCodeIdAndUsage);
            if(!codeUsageU.isSuccess()) {
                log.error("fail to batch update activityCode usage by map={}, error code:{}",
                        activityCodeIdAndUsage, codeUsageU.getError());
            }
        }

        return orderIdAndEarnestId.getEarnestId();
    }

    //增值税发票
    private String getExistVATInvoice(Long userId) {
        Response<UserVatInvoice> userVatInvoiceR = userVatInvoiceService.getByUserId(userId);
        if (!userVatInvoiceR.isSuccess() || userVatInvoiceR.getResult() == null) {
            log.error("fail to get vat invoice by userId={}, error code={}", userId, userVatInvoiceR.getError());
            throw new ServiceException("get.vat.invoice.fail");
        }
        Map<String, Object> mapped = Maps.newHashMap();
        mapped.put("type", OrderExtra.Type.VAT.value());
        mapped.put("vat", userVatInvoiceR.getResult());
        return mapper.toJson(mapped);
    }

    private boolean checkPlainBuyLimit(Map<Long, Integer> skuIdAndDiscount, Integer plainBuyLimit,
                                       Integer buyQuantity, Long buyerId, Long preSaleId, Date preSaleFinishAt) {
        //如果没有用码并且plainBuyLimit不为空，需要验证购买数量
        if (skuIdAndDiscount == null || skuIdAndDiscount.isEmpty()) {
            if (plainBuyLimit != null) {
                //获取已经购买的数量,如果为null表示从未购买过
                Integer buyCount = preSaleBuyLimitDao.getPreSaleBuyCount(buyerId, preSaleId);
                if(buyCount == null) {
                    //购买数量在限制范围之内则可以购买
                    if(buyQuantity <= plainBuyLimit) {
                        //设置购买数量
                        preSaleBuyLimitDao.setPreSaleBuyCount(buyerId,preSaleId,buyQuantity,preSaleFinishAt.getTime()/1000);
                        return true;
                    }
                    return false;
                }
                if((buyCount+buyQuantity) <= plainBuyLimit) {
                    preSaleBuyLimitDao.setPreSaleBuyCount(buyerId,preSaleId,(buyQuantity+buyCount),preSaleFinishAt.getTime()/1000);
                    return true;
                }
                return false;
            }
        }
        return true;
    }


    @Transactional
    public void updateItemAndSku(Item updateItem,List<Sku> skus){
        try {
            Response<Boolean> itemUpdateR = itemService.updateItem(updateItem);
            checkState(itemUpdateR.isSuccess(), itemUpdateR.getError());
            Response<Boolean> skuUpdateR = itemService.updateSkus(skus);
            checkState(skuUpdateR.isSuccess(), skuUpdateR.getError());
        }catch (IllegalStateException e){
            log.error("update item or sku fail error:{}",e.getMessage());
        }catch (Exception e){
            log.error("update item or sku fail cause:{}", Throwables.getStackTraceAsString(e));
        }
    }
}
