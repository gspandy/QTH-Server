package com.nowbook.coupons.service;

import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.coupons.model.LqCouponView;
import com.nowbook.coupons.model.LqMessage;

import java.util.List;

/**
 * Created by zhua02 on 2014/8/21.
 */
public interface LqCouponService {
    Response<List<LqCouponView>> findCouponAll();
    LqMessage LqCoupon(BaseUser baseUser,int couponId);
}
