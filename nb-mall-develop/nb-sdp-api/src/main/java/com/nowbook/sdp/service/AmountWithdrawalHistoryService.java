package com.nowbook.sdp.service;


import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.AmountWithdrawalHistory;

import javax.annotation.Nullable;

public interface AmountWithdrawalHistoryService {
    Response<Boolean>  deleteByPrimaryKey(Long id);

    Response<Long>  insert(AmountWithdrawalHistory record);

    Response<Long>  insertSelective(AmountWithdrawalHistory record);

    Response<AmountWithdrawalHistory> selectByPrimaryKey(Long id);

    Response<Boolean>  updateByPrimaryKeySelective(AmountWithdrawalHistory record);

    Response<Boolean> updateByPrimaryKey(AmountWithdrawalHistory record);

    /**
     * 检索
     *
     * @param shopName 店铺名
     * @param pageNo   页数
     * @param size     条数
     * @return
     */
    Response<Paging<AmountWithdrawalHistory>> AmountWithdrawalHistoryForQuery(@ParamInfo("shopName") @Nullable String shopName,
                                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                @ParamInfo("size") @Nullable Integer size);


    Response<Paging<AmountWithdrawalHistory>> selectWithdrawal(AmountWithdrawalHistory withdrawal, Integer pageNo,Integer size);





}