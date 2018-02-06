package com.nowbook.rlt.buying.manager;

import com.nowbook.common.model.Response;
import com.nowbook.exception.ServiceException;
import com.nowbook.item.model.Item;
import com.nowbook.item.service.ItemService;
import com.nowbook.rlt.buying.dao.BuyingActivityDefinitionDao;
import com.nowbook.rlt.buying.dao.BuyingItemDao;
import com.nowbook.rlt.buying.dao.BuyingOrderRecordDao;
import com.nowbook.rlt.buying.dao.BuyingTempOrderDao;
import com.nowbook.rlt.buying.dto.BuyingActivityDto;
import com.nowbook.rlt.buying.model.BuyingActivityDefinition;
import com.nowbook.rlt.buying.model.BuyingItem;
import com.nowbook.rlt.buying.model.BuyingOrderRecord;
import com.nowbook.rlt.buying.model.BuyingTempOrder;
import com.nowbook.rlt.code.dao.CodeUsageDao;
import com.nowbook.rlt.code.model.CodeUsage;
import com.nowbook.shop.model.Shop;
import com.nowbook.shop.service.ShopService;
import com.nowbook.trade.model.Order;
import com.nowbook.trade.model.OrderExtra;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.trade.service.OrderWriteService;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.nowbook.common.utils.Arguments.isNull;

/**
 * Created by songrenfei on 14-9-23
 */
@Component
@Slf4j
public class BuyingActivityManger {

    @Autowired
    private BuyingActivityDefinitionDao buyingActivityDefinitionDao;

    @Autowired
    private BuyingItemDao buyingItemDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private BuyingOrderRecordDao buyingOrderRecordDao;

    @Autowired
    private BuyingTempOrderDao buyingTempOrderDao;

    @Autowired
    private CodeUsageDao codeUsageDao;

    @Value("#{app.eNbSellerId}")
    private String eNbSellerId;

    @Transactional
    public Long create(BuyingActivityDto buyingActivityDto,Long userId){


        BuyingActivityDefinition buyingActivityDefinition =buyingActivityDto.getBuyingActivityDefinition();

        Response<Shop> shopRes = shopService.findByUserId(userId);
        if(!shopRes.isSuccess()){
            throw new ServiceException(shopRes.getError());
        }
        Shop shop = shopRes.getResult();
        buyingActivityDefinition.setShopId(shop.getId());
        buyingActivityDefinition.setShopName(shop.getName());
        buyingActivityDefinition.setSellerId(userId);
        buyingActivityDefinition.setSellerName(shop.getUserName());
        buyingActivityDefinition.setBusinessId(shop.getBusinessId());

        List<BuyingItem> buyingItemList = buyingActivityDto.getItemList();

        if(isNull(buyingItemList)||buyingItemList.size()>15){
            log.error("buying item list length={} out of", buyingItemList.size());
            throw new ServiceException("buying.item.list.length.out.of");
        }


        if(buyingItemList.size()>1){

            final List<Long> itemIds= Lists.transform(buyingItemList, new Function<BuyingItem, Long>() {
                @Override
                public Long apply(BuyingItem buyingItem) {
                    return buyingItem.getItemId();
                }
            });

            for(BuyingItem buyingItem : buyingItemList){
                    if(itemIds.indexOf(buyingItem.getItemId())!=itemIds.lastIndexOf(buyingItem.getItemId())){
                        log.error("create buying item for item(id={}) multiple", buyingItem.getItemId());
                        throw new ServiceException("buying.item.fot.item.id.multiple");
                    }
            }
        }

        Long id = buyingActivityDefinitionDao.create(buyingActivityDefinition);

        for (BuyingItem buyingItem : buyingItemList){


            Response<Item> itemRes = itemService.findById(buyingItem.getItemId());
            if (itemRes.getResult() == null) {
                log.error("item(id={}) mot found", id);
                throw new ServiceException("item.not.found");
            }
            //这里没有判断是否上架
            //商品是否属于卖家
            if (!Objects.equal(userId, itemRes.getResult().getUserId())) {
                log.error("item(id{}) not owner");
                throw new ServiceException("item.not.owner");
            }
            if(buyingItem.getIsStorage()!=null&&buyingItem.getIsStorage()){
                if(!Objects.equal(eNbSellerId,userId.toString())){
                    log.error("current  user not enb");
                    throw new ServiceException("current.user.not.enb");
                }
            }
            //当不支持分仓时要验证商品库存
            if(buyingItem.getIsStorage()==null||!buyingItem.getIsStorage()){
                buyingItem.setIsStorage(Boolean.FALSE);
                if(itemRes.getResult().getQuantity()<=0){
                    log.error("item(id={}) stock not enough",id);
                    throw new ServiceException("item.stock.not.enough");
                }
            }

            buyingItem.setBuyingActivityId(id);
            buyingItemDao.create(buyingItem);
        }

        return id;
    }


    @Transactional
    public Boolean update(BuyingActivityDto buyingActivityDto,Long userId){

        BuyingActivityDefinition buyingActivityDefinition =buyingActivityDto.getBuyingActivityDefinition();


        Response<Shop> shopRes = shopService.findByUserId(userId);
        if(!shopRes.isSuccess()){
            throw new ServiceException(shopRes.getError());
        }
        Shop shop = shopRes.getResult();
        buyingActivityDefinition.setShopId(shop.getId());
        buyingActivityDefinition.setShopName(shop.getName());
        buyingActivityDefinition.setSellerId(userId);
        buyingActivityDefinition.setSellerName(shop.getUserName());

        List<BuyingItem> buyingItemList = buyingActivityDto.getItemList();

        Boolean isUpdate  = buyingActivityDefinitionDao.update(buyingActivityDefinition);

        Long id =buyingActivityDto.getBuyingActivityDefinition().getId();

        buyingItemDao.deleteByActivityId(id);   //删除之前的关联

        for (BuyingItem buyingItem : buyingItemList){

            Response<Item> itemRes = itemService.findById(buyingItem.getItemId());
            if (itemRes.getResult() == null) {
                log.error("item(id={}) mot found", id);
                throw new ServiceException("item.not.found");
            }
            if (!Objects.equal(userId, itemRes.getResult().getUserId())) {
                log.error("item(id{}) not owner");
                throw new ServiceException("item.not.owner");
            }

            if(buyingItem.getIsStorage()!=null&&buyingItem.getIsStorage()){
                if(!Objects.equal(eNbSellerId,userId.toString())){
                    log.error("current  user not enb");
                    throw new ServiceException("current.user.not.enb");
                }
            }

            //当不支持分仓时要验证商品库存
            if(buyingItem.getIsStorage()==null||(!buyingItem.getIsStorage()&&!Objects.equal(eNbSellerId,userId))){
                if(itemRes.getResult().getQuantity()<=0){
                    log.error("item(id={}) stock not enough",id);
                    throw new ServiceException("item.stock.not.enough");
                }
            }

                buyingItem.setBuyingActivityId(id);
                buyingItemDao.create(buyingItem);
        }

        return isUpdate;
    }


    @Transactional
    public Boolean updateFakeSoldQuantity(List<BuyingItem> buyingItemList){

        for(BuyingItem buyingItem : buyingItemList){
            buyingItemDao.update(buyingItem);
        }

        return Boolean.TRUE;
    }

    @Transactional
    public Long createBuyingOrder(Order order, OrderItem orderItem,
                                  OrderExtra orderExtra, BuyingOrderRecord buyingOrderRecord,
                                  BuyingTempOrder buyingTempOrderToUpdate, CodeUsage codeUsage) {

        Response<Long> orderIdR = orderWriteService.buyingOrderCreate(order, orderItem, orderExtra);
        if(!orderIdR.isSuccess() || orderIdR.getResult() == null) {
            log.error("fail to create buying order, error code:{}", orderIdR.getError());
            throw new ServiceException(orderIdR.getError());
        }

        Long orderId = orderIdR.getResult();

        buyingOrderRecord.setOrderId(orderId);
        buyingOrderRecordDao.create(buyingOrderRecord);

        buyingTempOrderToUpdate.setOrderId(orderId);
        buyingTempOrderDao.update(buyingTempOrderToUpdate);

        if(codeUsage.getDiscount() != null) {
            codeUsage.setOrderId(orderId);
            codeUsageDao.create(codeUsage);
        }

        return orderId;
    }
}
