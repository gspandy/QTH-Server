package com.nowbook.rlt.settle.manager;

import com.nowbook.common.model.Response;
import com.nowbook.rlt.settle.model.AbnormalTrans;
import com.nowbook.rlt.settle.model.ItemSettlement;
import com.nowbook.rlt.settle.model.SellerSettlement;
import com.nowbook.rlt.settle.model.Settlement;
import com.nowbook.rlt.settle.dao.*;
import com.nowbook.sdp.dao.AmountDetailDao;
import com.nowbook.sdp.dao.DistributionsDao;
import com.nowbook.sdp.dao.DistributorRedisDao;
import com.nowbook.sdp.dao.LevelDao;
import com.nowbook.sdp.model.AmountDetail;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.model.UserWalletSummary;
import com.nowbook.sdp.service.UserWalletService;
import com.nowbook.shop.model.Shop;
import com.nowbook.shop.service.ShopService;
import com.nowbook.trade.model.OrderItem;
import com.nowbook.trade.service.OrderQueryService;
import com.nowbook.user.model.User;
import com.nowbook.user.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.nowbook.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-01-20 4:29 PM  <br>
 * Author:cheng
 */
@Slf4j
@Component
public class SettlementManager {

    @Autowired
    private DepositFeeDao depositFeeDao;

    @Autowired
    private DepositAccountDao depositAccountDao;

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private SellerSettlementDao sellerSettlementDao;

    @Autowired
    private DepositFeeCashDao depositFeeCashDao;

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    @Autowired
    private ItemSettlementDao itemSettlementDao;

    @Autowired
    private AbnormalTransDao abnormalTransDao;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private DistributionsDao distributionsDao;

    @Autowired
    private DistributorRedisDao distributorRedisDao;

    @Autowired
    private UserWalletService userWalletService;

    @Autowired
    private ShopService shopService;
    @Autowired
    private LevelDao levelDao;

    @Autowired
    private AmountDetailDao amountDetailDao;


    public void settled(Long id) {
        Settlement settlement = settlementDao.get(id);
        checkState(notNull(settlement), "settle.entity.not.found");

        settlementDao.settled(id);
    }

    /**
     * 创建订单及其子订单的结算信息
     * @param settlement        订单结算信息
     */
    @Transactional
    public Long create(Settlement settlement, Double rate) {
        settlementDao.create(settlement);
        createItemSettlements(settlement, rate);
        return settlement.getId();
    }

    /**
     * 创建订单及其子订单的结算信息 for 预售订金
     * @param settlement        订单结算信息
     */
    @Transactional
    public Long createForPresale(Settlement settlement, Double rate) {
        settlementDao.create(settlement);
        createItemSettlements(settlement, rate);
        return settlement.getId();
    }

    /**
     * 根据订单创建子订单结算记录
     *
     * @param settlement 订单
     * @param rate       费率
     */
    private void createItemSettlements(Settlement settlement, Double rate) {
        checkState(notNull(settlement.getOrderId()));

        Long orderId = settlement.getOrderId();
        List<ItemSettlement> itemSettlements = itemSettlementDao.findByOrderId(orderId);  // 判断是否已经创建子订单结算信息
        if (itemSettlements.size() > 0) {  // 若已经存在则不创建
            log.debug("item settlements of {} existed skipped", settlement);
            return;
        }

        Response<List<OrderItem>> orderItemsQueryResult = orderQueryService.findOrderItemByOrderId(orderId);
        checkState(orderItemsQueryResult.isSuccess(), "order.item.query.fail");
        List<OrderItem> items = orderItemsQueryResult.getResult();

        for (OrderItem item : items) {
            ItemSettlement is = new ItemSettlement();
            is.setOrderId(settlement.getOrderId());
            is.setSellerId(settlement.getSellerId());
            is.setSellerName(settlement.getSellerName());
            is.setBuyerId(settlement.getBuyerId());
            is.setPaidAt(item.getPaidAt());//子订单的支付时间
            is.setBusiness(settlement.getBusiness());

            is.setSettleStatus(Settlement.SettleStatus.FINISH.value());
            is.setOrderItemId(item.getId());
            is.setTradeStatus(item.getStatus());
            is.setPayType(item.getPayType());
            is.setFee((long)item.getFee());
            is.setType(item.getType());
            is.setItemName(item.getItemName());
            is.setItemQuantity(item.getQuantity());
            is.setReason(item.getReason());
            is.setPaymentCode(item.getPaymentCode());
            is.setRefundAmount( item.getRefundAmount() == null? 0L: item.getRefundAmount().longValue());

            is.setCommissionRate(rate);

            Response<User> getUser = accountService.findUserById(item.getBuyerId());
            if (getUser.isSuccess()) {
                User buyer = getUser.getResult();
                is.setBuyerName(buyer.getName());
            }

            itemSettlementDao.create(is);
            if(!item.getStatus().equals(3)){
                break;
            }
            if(item.getPaymentPlatform().equals("1")){
                break;
            }
            Long money = Long.valueOf(item.getFee()-item.getDeliverFee());
            if(money<=0){
                break;
            }
            JSONObject jsonObject = JSONObject.fromObject(item.getJson());
            jsonObject.put("num",item.getQuantity());
            UserWalletSummary userWalletSummary = new UserWalletSummary();
            userWalletSummary.setUserId(item.getBuyerId());
            userWalletSummary.setMoney(money);
            if(jsonObject.get("priceType").toString().equals("1")){
                Level level = new Level();
                level.setLevel(item.getLevel());
                List<Level> levelList = levelDao.selectByLevel(level);
                userWalletSummary.setRealMoney(money*100/levelList.get(0).getDiscount());
                userWalletSummary.setMoneyType(1);
            }else if(jsonObject.get("priceType").toString().equals("2")){
                //随便给个，进去再算
                userWalletSummary.setRealMoney(10L);
                userWalletSummary.setMoneyType(2);
            }else if(jsonObject.get("priceType").toString().equals("3")){
                //随便给个，进去再算
                userWalletSummary.setRealMoney(10L);
                userWalletSummary.setMoneyType(3);
            }
            userWalletSummary.setOrderItemId(item.getId());
            userWalletSummary.setJson(jsonObject);
            userWalletService.profit(userWalletSummary);
        }

    }

    /**
     * 更新订单及其子订单的结算状态（除支付宝手续费以外的各项金额）
     *
     * @param settlement         订单结算信息
     * @param itemSettlements    子订单结算信息
     */
    @Transactional
    public void update(Settlement settlement, List<ItemSettlement> itemSettlements) {

//        String parentIds =  distributionsDao.getParentsByOrderId(settlement.getOrderId());
//        String [] stringArr= parentIds.split(",");
//        int length = 0;
//        if(!"".equals(parentIds)){
//            length = stringArr.length;
//        }

//        AmountDetail amountDetail = new AmountDetail();
        //去掉主订单的佣金计算
        /*for(int i = 0;i<length;i++){
            amountDetail.setOrderId(settlement.getOrderId()+"");
            amountDetail.setDistributorsId(Long.getLong(stringArr[i]));
            String key = "level_"+(length - i)+"_commission_ratio";
            String ratioStr = distributorRedisDao.getDistributionConfKey(key);
            Long ratio =  Long.valueOf(ratioStr);
            Long amount = settlement.getNbCommission()*ratio/Long.valueOf(100);
            amountDetail.setIsComplete("0");
            amountDetail.setGetAmount(Double.valueOf(amount));
            amountDetailDao.insert(amountDetail);
        }*/

         // add by wangdongchang 计算本订单的各级分销商的佣金
        settlementDao.update(settlement);
        for (ItemSettlement sub : itemSettlements) {
            /*parentIds =  distributionsDao.getParentsByOrderId(sub.getOrderId());
            stringArr= parentIds.split(",");
             length = 0;
            if(!"".equals(parentIds)){
                length = stringArr.length;
            }

            amountDetail = new AmountDetail();
            for(int i = 0;i<length;i++){
                amountDetail.setOrderId(sub.getOrderId()+"");
                amountDetail.setDistributorsId(Long.parseLong(stringArr[i]));
                String key = "level_"+(length - i)+"_commission_ratio";
                String ratioStr = distributorRedisDao.getDistributionConfKey(key);
                Double ratio =  Double.valueOf(ratioStr);
                Double amount = sub.getNbCommission()*ratio/Long.valueOf(100);
                amountDetail.setGetAmount(amount);
                amountDetail.setIsComplete("1");
                if(amountDetail.getGetAmount()!=0){
                    amountDetailDao.insert(amountDetail);
                }

            }*/

//            amountDetail = new AmountDetail();
//            amountDetail.setOrderId(sub.getOrderId()+"");
//            amountDetail.setOrderItemId(sub.getOrderItemId()+"");
//            amountDetail.setIsComplete("0");
//            amountDetailDao.updateByOrder(amountDetail);


            itemSettlementDao.update(sub);
        }
    }


    /**
     * 关闭订单及关闭子订单 （更新交易状态及结束标记)
     *
     * @param settlement        订单结算信息
     */
    @Transactional
    public void finished(Settlement settlement) {

        settlementDao.finished(settlement);

        List<ItemSettlement> itemSettlements = itemSettlementDao.findByOrderId(settlement.getOrderId());
        for (ItemSettlement itemSettlement : itemSettlements) {
            Long orderItemId = itemSettlement.getOrderItemId();
            Response<OrderItem> orderItemResponse = orderQueryService.findOrderItemById(orderItemId);
            if (orderItemResponse.isSuccess()) {
                OrderItem orderItem = orderItemResponse.getResult();
                itemSettlement.setTradeStatus(orderItem.getStatus());
                itemSettlement.setRefundAmount(orderItem.getRefundAmount() == null ? 0L : orderItem.getRefundAmount().longValue());
                itemSettlement.setReason(orderItem.getReason());
                itemSettlementDao.update(itemSettlement);
            }
        }
    }

    /**
     * 更新指定商户日汇总的同步状态为 “已同步“ 同时更新对应的结算信息为已结算 <br/>
     * 需要同步更新对应的订单结算信息
     *
     * @param id  商户结算信息标识
     */
    @Transactional
    public void synced(Long id) {
        SellerSettlement sellerSettlement = sellerSettlementDao.get(id);
        checkState(notNull(sellerSettlement), "seller.settlement.not.exist");

        Date confirmedAt = sellerSettlement.getConfirmedAt();
        Date startAt = new DateTime(confirmedAt).withTimeAtStartOfDay().toDate();
        Date endAt = new DateTime(startAt).plusDays(1).toDate();

        boolean success = sellerSettlementDao.synced(id);

        checkState(success, "seller.settlement.synced.fail");
        settlementDao.batchSynced(startAt, endAt);
        itemSettlementDao.batchSynced(startAt, endAt);
    }

    /**
     * 记录不正确的的订单信息
     * @param settlements 订单列表
     */
    public void recordIncorrectSettlements(Collection<Settlement> settlements, String reason) {
        for (Settlement settlement : settlements) {
            AbnormalTrans abnormalTrans = new AbnormalTrans();
            abnormalTrans.setReason(reason);
            abnormalTrans.setSettlementId(settlement.getId());
            abnormalTrans.setOrderId(settlement.getOrderId());
            abnormalTransDao.create(abnormalTrans);
        }
    }

    @Transactional
    public void batchUpdate(Collection<Settlement> multiPaidSettlements) {
        for (Settlement settlement : multiPaidSettlements) {
            settlementDao.update(settlement);
        }
    }

    @Transactional
    public void batchUpdateOuterCodeOfSeller(String outerCode, Long sellerId) {
        Integer count = depositAccountDao.updateOuterCode(outerCode, sellerId);
        log.info("handled {} depositAccount to {} of seller(id:{})", count, outerCode, sellerId);

        count = depositFeeDao.batchUpdateOuterCode(outerCode, sellerId);
        log.info("handled {} depositFee to {} of seller(id:{})", count, outerCode, sellerId);

        sellerSettlementDao.batchUpdateOuterCode(outerCode, sellerId);
        log.info("handled {} sellerSettlement to {} of seller(id:{})", count, outerCode, sellerId);

        sellerAlipayCashDao.batchUpdateOuterCode(outerCode, sellerId);
        log.info("handled {} sellerAlipayCash to {} of seller(id:{})", count, outerCode, sellerId);

        depositFeeCashDao.batchUpdateOuterCode(outerCode, sellerId);
        log.info("handled {} depositFeeCash to {} of seller(id:{})", count, outerCode, sellerId);
    }

    /**
     * 标记订单及子订单的帐务记录为补帐
     * @param settlement  帐务
     */
    @Transactional
    public void fixed(Settlement settlement) {
        settlementDao.fixed(settlement);
        List<ItemSettlement> itemSettlements = itemSettlementDao.list(settlement.getOrderId());
        for (ItemSettlement itemSettlement : itemSettlements) {
            itemSettlementDao.fixed(itemSettlement.getId());
        }
    }
}
