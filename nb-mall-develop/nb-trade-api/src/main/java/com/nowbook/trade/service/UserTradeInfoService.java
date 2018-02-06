package com.nowbook.trade.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.trade.model.UserTradeInfo;
import com.nowbook.user.base.BaseUser;

import java.util.List;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-14
 */
public interface UserTradeInfoService {
    Response<Long> create(UserTradeInfo userTradeInfo);

    Response<List<UserTradeInfo>> findTradeInfosByUserId(Long userId);

    Response<List<UserTradeInfo>> findTradeInfosByUser(@ParamInfo("baseUser") BaseUser baseUser);

    Response<Boolean> delete(Long id);

    Response<Long> update(UserTradeInfo userTradeInfo, Long userId);

    Response<Boolean> makeDefault(Long userId, Long id);

    Response<Boolean> invalidate(Long id);

    Response<UserTradeInfo> findById(Long id);

    Response<UserTradeInfo> findDefault(Long userId);

    /**
     * 根据用户id和区域id查找当前用户当前区域的所有有效的收货地址
     * @param userId 用户id
     * @param districtId 区id
     * @return 收货地址集合
     */
    Response<List<UserTradeInfo>> findTradeInfoByUserAndDistrict(Long userId,Integer districtId);


}
