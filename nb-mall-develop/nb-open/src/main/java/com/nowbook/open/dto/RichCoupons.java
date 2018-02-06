package com.nowbook.open.dto;

import com.nowbook.coupons.model.NbShowCouponView;
import lombok.Data;

/**
 * Created by neusoft on 14-9-9.
 */
@Data
public class RichCoupons extends NbShowCouponView{

    private  Long userId;

    private  Long couponId;

    private  String cpName;

    private  String categoryId;

    private int term;
}
