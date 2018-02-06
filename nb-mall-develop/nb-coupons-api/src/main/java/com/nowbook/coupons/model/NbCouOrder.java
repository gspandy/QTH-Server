package com.nowbook.coupons.model;

import lombok.Getter;
import lombok.Setter;

public class NbCouOrder extends NbCou {

	private static final long serialVersionUID = 1L;

    @Getter
    @Setter
	private Long orderId;

    @Getter
    @Setter
	private Long userId;
}
