package com.nowbook.sdp.service;


import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.AmountDetail;

import javax.annotation.Nullable;

public interface AmountDetailService {
    Response<Boolean> deleteByPrimaryKey(Long id);

    Response<Long> insert(AmountDetail record);

    Response<Long> insertSelective(AmountDetail record);

    Response<Boolean> updateByPrimaryKeySelective(AmountDetail record);

    Response<Boolean> updateByPrimaryKey(AmountDetail record);

    Response<Paging<AmountDetail>> selectAmountDetail(AmountDetail amountDetail, Integer pageNo, Integer size);

    /**
     * 根据订单号以及分销商ID 分配佣金
     *
     *
     */
    Response<Boolean> calcAmount(Long orderId,Long ditrabutorId);
    void jobUpdateOrder(String result);
    void deleteSum();


    /**
     * 检索
     *
     * @param shopName 店铺名
     * @param pageNo   页数
     * @param size     条数
     * @return
     */
    Response<Paging<AmountDetail>> AmountDetailForQuery(@ParamInfo("shopName") @Nullable String shopName,
                                                        @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                        @ParamInfo("size") @Nullable Integer size);

    /**
     * 插入佣金明细
     *
     * @param orderId 订单号
     * @param orderItemId   子订单号
     * @return
     */
    Response<Long> insertByOrder(String orderId,String orderItemId,Long buyerId);
    /**
     * 检索
     *
     * @param orderId 订单号
     * @param orderItemId   子订单号
     * @param status     状态
     * @return
     */
    Response<Boolean> updateByOrder(String orderId,String orderItemId,String status);

    Response<Boolean> updateSum(String orderId,String orderItemId,Long buyerId);
}