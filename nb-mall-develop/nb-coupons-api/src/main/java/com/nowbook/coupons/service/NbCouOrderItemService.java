package com.nowbook.coupons.service;

import com.nowbook.common.model.Response;
import com.nowbook.coupons.model.NbCouOrderItem;

/**
 * Created by yea01 on 2014/8/25.
 */
public interface NbCouOrderItemService {

    public Response<Boolean> saveCouOrderItem(NbCouOrderItem nbCouOrderItem);
}
