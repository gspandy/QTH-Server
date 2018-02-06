package com.nowbook.sdp.service;


import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.AmountDetailDao;
import com.nowbook.sdp.dao.DistributionsDao;
import com.nowbook.sdp.dao.DistributorRedisDao;
import com.nowbook.sdp.model.AmountDetail;
import com.nowbook.sdp.model.DistributionInfo;
import com.nowbook.sdp.model.DistributionInfoForQuery;
import com.nowbook.sdp.model.ItemSettlement;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wangdongchang on 16-04-24
 */
@Service
public class AmountDetailServiceImpl implements AmountDetailService {

    private final static Logger log = LoggerFactory.getLogger(AmountDetailServiceImpl.class);
    @Autowired
    private AmountDetailDao amountDetailDao;

    @Autowired
    private DistributionsDao distributionsDao;

    @Autowired
    private DistributorRedisDao distributorRedisDao;

    @Autowired
    private DistributionsService distributionsService;


    @Override
    public Response<Boolean> deleteByPrimaryKey(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        if (id == null) {
            log.error("id should be specified when deleted");
            result.setError("amountDetail.id.null");
            return result;
        }
        try {
            amountDetailDao.deleteByPrimaryKey(id);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete amountDetail(id={}),cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
            return result;
        }
    }

    @Override
    public Response<Long> insert(AmountDetail record) {
        Response<Long> result = new Response<Long>();
        if (record == null) {
            log.error("AmountDetail can not be null when create AmountDetail");
            result.setError("illegal.param");
            return result;
        }
        if (record.getId() == null) {
            log.error("id can not be null when create AmountDetail");
            result.setError("illegal.param");
            return result;
        }
        amountDetailDao.insert(record);
        result.setResult(record.getId());

        return result;
    }

    @Override
    public Response<Long> insertSelective(AmountDetail record) {
        Response<Long> result = new Response<Long>();
        if (record == null) {
            log.error("AmountDetail can not be null when create AmountDetail");
            result.setError("illegal.param");
            return result;
        }
        if (record.getId() == null) {
            log.error("id can not be null when create AmountDetail");
            result.setError("illegal.param");
            return result;
        }
        amountDetailDao.insertSelective(record);
        result.setResult(record.getId());

        return result;
    }

    @Override
    public Response<Boolean> updateByPrimaryKeySelective(AmountDetail record) {
        Response<Boolean> result = new Response<Boolean>();

        if(record.getId() == null ) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            amountDetailDao.updateByPrimaryKeySelective(record);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update AmountDetail method openStatus by id={}, orderId={},cause:{}",
                    record.getId(), record.getOrderId(), Throwables.getStackTraceAsString(e));
            result.setError("AmountDetail.method.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateByPrimaryKey(AmountDetail record) {
        Response<Boolean> result = new Response<Boolean>();

        if(record.getId() == null ) {
            log.error("AmountDetail params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            amountDetailDao.updateByPrimaryKey(record);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update AmountDetail method openStatus by id={}, orderId={},cause:{}",
                    record.getId(), record.getOrderId(), Throwables.getStackTraceAsString(e));
            result.setError("AmountDetail.method.update.fail");
            return result;
        }
    }



    @Override
    public Response<Paging<AmountDetail>> selectAmountDetail(AmountDetail amountDetail, Integer pageNo, Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<AmountDetail>> result = new Response<Paging<AmountDetail>>();

        Paging<AmountDetail> amountDetailPaging;

        try {
            amountDetailPaging = amountDetailDao.selectAmountDetail(amountDetail, page.getOffset(), page.getLimit());

            result.setResult(amountDetailPaging);
            return result;
        }catch (Exception e) {
            log.error("failed to find all amountDetail, cause:", e);
            result.setError("AmountDetail.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> calcAmount(Long orderId, Long ditrabutorId) {
        Response<Boolean> result = new Response<Boolean>();

        if(orderId == null ||ditrabutorId == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }



        result.setResult(Boolean.TRUE);
        return result;

    }
    @Override
    public void jobUpdateOrder(String result){
        amountDetailDao.jobUpdateOrder(result);
    }

    @Override
    public void deleteSum() {
        amountDetailDao.deleteSum();
    }


    @Override
    public Response<Paging<AmountDetail>> AmountDetailForQuery(@ParamInfo("shopName") @Nullable String shopName, @ParamInfo("pageNo") @Nullable Integer pageNo, @ParamInfo("size") @Nullable Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<AmountDetail>> result = new Response<Paging<AmountDetail>>();
        AmountDetail amountDetail = new AmountDetail();
        DistributionInfo distributionInfo = new DistributionInfo();
        if(shopName!=null&&!shopName.equals(""))
        distributionInfo.setShopName(shopName);
        amountDetail.setDistributionInfo(distributionInfo);

        Paging<AmountDetail> amountDetailPaging;

        try {
            amountDetailPaging = amountDetailDao.selectAmountDetail(amountDetail, page.getOffset(), page.getLimit());
            result.setResult(amountDetailPaging);
            return result;
        }catch (Exception e) {
            log.error("failed to find all amountDetail, cause:", e);
            result.setError("AmountDetail.query.fail");
            return result;
        }
    }
    @Override
    public Response<Long> insertByOrder(String orderId,String orderItemId,Long buyerId) {
        Long nbCommission = 0L;              // 平台佣金
        Response<Long> result = new Response<Long>();
        AmountDetail record = new AmountDetail();
        record.setOrderId(orderId);
        record.setOrderItemId(orderItemId);
        record.setOperTime(new Date());
        DistributionInfo distributionInfo = new DistributionInfo();
        String parentIds = "";
        Response<DistributionInfoForQuery> distributionInfoForQueryResult = distributionsService.selectDistributionByUserId(buyerId);
        if (distributionInfoForQueryResult.getResult() != null && distributionInfoForQueryResult.getResult().getId() != null) {
            record.setFromDistributorsId(distributionInfoForQueryResult.getResult().getId().toString());
            parentIds = distributionsDao.getParentIdsByuserId(buyerId);
        }else{
            parentIds = distributionsDao.selectDistributorId(orderId);
            if(!distributionsDao.getParentsByOrderId(Long.valueOf(orderId)).equals("")){
                parentIds = distributionsDao.getParentsByOrderId(Long.valueOf(orderId))+","+parentIds;
            }
        }


        //将父分销商放入数组
        String [] stringArr= parentIds.split(",");
        int length = 0;
        if(!"".equals(parentIds)){
            length = stringArr.length;
        }
        //获取订单的结算信息 计算平台佣金
        ItemSettlement itemSettlement =  amountDetailDao.findByOrderItemId(Long.valueOf(orderItemId));
        nbCommission =  Math.round(itemSettlement.getFee() * itemSettlement.getCommissionRate());
        //将平台佣金分配给本订单的分销商的上级分销商
        for(int i = 0;i<length;i++){
            distributionInfo.setId(Long.parseLong(stringArr[i]));
            record.setDistributionInfo(distributionInfo);
            record.setDistributorsId(Long.parseLong(stringArr[i]));
            String key = "level_"+(length - i)+"_commission_ratio";
            String ratioStr = distributorRedisDao.getDistributionConfKey(key);
            Double ratio =  Double.valueOf(ratioStr);
            Double amount = nbCommission*ratio/Long.valueOf(100);
            record.setGetAmount(Integer.valueOf(amount.intValue())*10);
            record.setIsComplete("0");
            if(record.getGetAmount()!=0){
                amountDetailDao.insert(record);
//                //重新调用存储过程计算佣金
//                amountDetailDao.updateSum(stringArr[i]);
            }

        }


        if (record == null) {
            log.error("AmountDetail can not be null when create AmountDetail");
            result.setError("illegal.param");
            return result;
        }
        if (record.getId() == null) {
            log.error("id can not be null when create AmountDetail");
            result.setError("illegal.param");
            return result;
        }
        result.setResult(record.getId());

        return result;
    }
    @Override
    public Response<Boolean> updateByOrder(String orderId,String orderItemId,String status) {
        Response<Boolean> result = new Response<Boolean>();
        AmountDetail record = new AmountDetail();
        record.setOrderId(orderId);
        record.setOrderItemId(orderItemId);
        record.setIsComplete(status);


        try {

            amountDetailDao.updateByOrder(record);
            /*//重新调用存储过程计算佣金
            for(int i = 0;i<length;i++){
                amountDetailDao.updateSum(stringArr[i]);

            }*/
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update AmountDetail method openStatus by id={}, orderId={},cause:{}",
                    record.getId(), record.getOrderId(), Throwables.getStackTraceAsString(e));
            result.setError("AmountDetail.method.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateSum(String orderId,String orderItemId,Long buyerId) {
        Response<Boolean> result = new Response<Boolean>();
        String parentIds = "";
        Response<DistributionInfoForQuery> distributionInfoForQueryResult = distributionsService.selectDistributionByUserId(buyerId);
        if (distributionInfoForQueryResult.getResult() != null && distributionInfoForQueryResult.getResult().getId() != null) {
            parentIds = distributionsDao.getParentIdsByuserId(buyerId);
        }else{
            parentIds = distributionsDao.selectDistributorId(orderId);
            if(!distributionsDao.getParentsByOrderId(Long.valueOf(orderId)).equals("")){
                parentIds = distributionsDao.getParentsByOrderId(Long.valueOf(orderId))+","+parentIds;
            }
        }


        //将父分销商放入数组
        String [] stringArr= parentIds.split(",");
        int length = 0;
        if(!"".equals(parentIds)){
            length = stringArr.length;
        }

        try {
            //重新调用存储过程计算汇总信息
            for(int i = 0;i<length;i++){
                amountDetailDao.updateSum(stringArr[i]);

            }
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update AmountDetail method openStatus by id={}, orderId={},cause:{}",
                    orderId, orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("AmountDetail.method.update.fail");
            return result;
        }
    }

}