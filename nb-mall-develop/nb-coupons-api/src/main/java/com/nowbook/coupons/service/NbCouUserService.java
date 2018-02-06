package com.nowbook.coupons.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.coupons.model.NbCouUserView;

import java.util.List;

/**
 * Created by yea01 on 2014/8/22.
 */
public interface NbCouUserService {
    /**
     * 获取当前用户的优惠劵信息
     * **/
    Response<List<NbCouUserView>> queryCouponsAllByUser(@ParamInfo("baseUser") BaseUser baseUse,@ParamInfo("skus") String skus,@ParamInfo("status") Long status);
}
