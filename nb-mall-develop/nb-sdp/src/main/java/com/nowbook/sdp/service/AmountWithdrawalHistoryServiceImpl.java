package com.nowbook.sdp.service;


import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.AmountWithdrawalHistoryDao;
import com.nowbook.sdp.model.AmountWithdrawalHistory;
import com.nowbook.sdp.model.DistributionInfo;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

/**
 * Created by wangdongchang on 16-04-24
 */
@Service
public class AmountWithdrawalHistoryServiceImpl implements  AmountWithdrawalHistoryService{
    private final static Logger log = LoggerFactory.getLogger(AmountWithdrawalHistoryServiceImpl.class);
    @Autowired
    private AmountWithdrawalHistoryDao amountWithdrawalHistoryDao;
    @Override
    public  Response<Boolean> deleteByPrimaryKey(Long id){

        Response<Boolean> result = new Response<Boolean>();
        if (id == null) {
            log.error("AmountWithdrawalHistory id should be specified when deleted");
            result.setError("AmountWithdrawalHistory.id.null");
            return result;
        }
        try {
            amountWithdrawalHistoryDao.deleteByPrimaryKey(id);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete AmountWithdrawalHistory(id={}),cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
            return result;
        }
    }
    @Override
    public  Response<Long> insert(AmountWithdrawalHistory record){
        Response<Long> result = new Response<Long>();
        if (record == null) {
            log.error("AmountWithdrawalHistory can not be null when create AmountWithdrawalHistory");
            result.setError("illegal.param");
            return result;
        }
        if (record.getDistributorsId() == null) {
            log.error("AmountWithdrawalHistory distributorsId can not be null when create AmountWithdrawalHistory");
            result.setError("illegal.param");
            return result;
        }
        if (record.getMoney() == null) {
            log.error("AmountWithdrawalHistory money can not be null when create AmountWithdrawalHistory");
            result.setError("illegal.param");
            return result;
        }
        amountWithdrawalHistoryDao.insert(record);
        result.setResult(record.getId());

        return result;
    }
    @Override
    public Response<Long> insertSelective(AmountWithdrawalHistory record){
        Response<Long> result = new Response<Long>();
        if (record == null) {
            log.error("AmountWithdrawalHistory can not be null when create AmountWithdrawalHistory");
            result.setError("illegal.param");
            return result;
        }
        if (record.getId() == null) {
            log.error("AmountWithdrawalHistory id can not be null when create AmountWithdrawalHistory");
            result.setError("illegal.param");
            return result;
        }
        amountWithdrawalHistoryDao.insertSelective(record);
        result.setResult(record.getId());

        return result;
    }
    @Override
    public Response<AmountWithdrawalHistory> selectByPrimaryKey(Long id){
        Response<AmountWithdrawalHistory> result = new Response<AmountWithdrawalHistory>();
        AmountWithdrawalHistory amountWithdrawalHistory = amountWithdrawalHistoryDao.selectByPrimaryKey(id);
        result.setResult(amountWithdrawalHistory);
        return result;
    }
    @Override
    public  Response<Boolean> updateByPrimaryKeySelective(AmountWithdrawalHistory record){
        Response<Boolean> result = new Response<Boolean>();

        if(record.getId() == null ) {
            log.error("AmountWithdrawalHistory params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            amountWithdrawalHistoryDao.updateByPrimaryKeySelective(record);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update AmountWithdrawalHistory method openStatus by id={}, money={},cause:{}",
                    record.getId(), record.getMoney(), Throwables.getStackTraceAsString(e));
            result.setError("AmountWithdrawalHistory.method.update.fail");
            return result;
        }
    }
    @Override
    public  Response<Boolean> updateByPrimaryKey(AmountWithdrawalHistory record){
        Response<Boolean> result = new Response<Boolean>();

        if(record.getId() == null ) {
            log.error("AmountWithdrawalHistory params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            amountWithdrawalHistoryDao.updateByPrimaryKey(record);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update AmountWithdrawalHistory method openStatus by id={}, money={},cause:{}",
                    record.getId(), record.getMoney(), Throwables.getStackTraceAsString(e));
            result.setError("AmountWithdrawalHistory.method.update.fail");
            return result;
        }
    }
    @Override
    public Response<Paging<AmountWithdrawalHistory>> AmountWithdrawalHistoryForQuery(@ParamInfo("shopName") @Nullable String shopName, @ParamInfo("pageNo") @Nullable Integer pageNo, @ParamInfo("size") @Nullable Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<AmountWithdrawalHistory>> result = new Response<Paging<AmountWithdrawalHistory>>();

        AmountWithdrawalHistory withdrawal = new AmountWithdrawalHistory();
        DistributionInfo distributionInfo= new DistributionInfo();
        distributionInfo.setShopName(shopName);
        withdrawal.setDistributionInfo(distributionInfo);
        Paging<AmountWithdrawalHistory> withdrawalPaging;
        try {
            withdrawalPaging = amountWithdrawalHistoryDao.selectWithdrawal(withdrawal, page.getOffset(), page.getLimit());

            result.setResult(withdrawalPaging);
            return result;
        }catch (Exception e) {
            log.error("failed to find all amountWithdrawalHistory, cause:", e);
            result.setError("AmountWithdrawalHistory.query.fail");
            return result;
        }
    }

    @Override
    public Response<Paging<AmountWithdrawalHistory>> selectWithdrawal(AmountWithdrawalHistory withdrawal, Integer pageNo, Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<AmountWithdrawalHistory>> result = new Response<Paging<AmountWithdrawalHistory>>();

        Paging<AmountWithdrawalHistory> withdrawalPaging;

        try {
            withdrawalPaging = amountWithdrawalHistoryDao.selectWithdrawal(withdrawal, page.getOffset(), page.getLimit());

            result.setResult(withdrawalPaging);
            return result;
        }catch (Exception e) {
            log.error("failed to find all withdrawal, cause:", e);
            result.setError("AmountWithdrawalHistory.query.fail");
            return result;
        }
    }
}