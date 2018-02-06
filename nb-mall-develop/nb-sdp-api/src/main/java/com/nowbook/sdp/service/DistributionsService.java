package com.nowbook.sdp.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.category.model.FrontCategory;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.DistributionInfo;
import com.nowbook.sdp.model.DistributionInfoForQuery;
import com.nowbook.sdp.model.Distributions;
import com.nowbook.common.model.Paging;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-14
 */
public interface DistributionsService {

    /**
     * 检索
     * @param openStatus 店铺状态（1开启，0关闭）
     * @param auditStatus 审核状态（1通过，0未通过）
     * @param shopName 店铺名
     * @param pageNo 页数
     * @param size 条数
     * @return
     */
    Response<Paging<Distributions>> distributionsAll(@ParamInfo("openStatus") @Nullable String openStatus,
                                                     @ParamInfo("auditStatus") @Nullable String auditStatus,
                                                     @ParamInfo("shopName") @Nullable String shopName,
                                                     @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                     @ParamInfo("size") @Nullable Integer size);

    /**
     * 更新店铺状态
     * @param id 配送方式id
     * @param openStatus 店铺更新状态（1开启，0关闭）
     * @return 是否成功
     */
    public Response<Boolean> updateOpenStatus(Long id, String openStatus);

    /**
     * 更新分销商信息
     */
    Response<Boolean> distributionsUpdate(Distributions distributions);

    /**
     * 创建分销商
     */
    Response<Long> distributorsCreateInteger(Distributions distributions);

    Response<Boolean> distributorsCreate(Distributions distributions);


    Response<DistributionInfo> selectByPrimaryKey(Long id);

    Response<DistributionInfoForQuery> selectSummaryByKey(Long id);


    /**
     * 绑定订单号以及分销商ID
     *
     *
     */
    Response<Boolean> bindSdp(Long orderId,Long ditrabutorId);

    Response<String> getParentsByOrderId(Long orderId);


    Response<Paging<DistributionInfo>> getDistributionByLevel(Long id, String level, Integer pageNo, Integer size);

    Response<List<FrontCategory>> findAllSecondLevel();
    /**
     * 微信提现
     *
     *
     */
    Response<HashMap<String,String>> withdrawalsPreconditions(Long distributorId,Double money);


    Response<String> withdrawalsTimeIntval();
    Response<DistributionInfoForQuery> selectDistributionByUserId(Long userId);
    Response<String> getId(Long userId);
    Response<DistributionInfoForQuery> selectDistributionByDistributionId(Long id);
    String getAvatar(Long userId);
    Response<Boolean> updateQr(Distributions distributions);
    String selectDistributorId(String orderId);
    public Response<Boolean> updateDistributor(Distributions distribution);
    Response<String> getDistributionConfKey(String key);


}
