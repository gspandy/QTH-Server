package com.nowbook.coupons.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class NbCouOrderItem extends NbCou {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private Long orderId;

	@Getter
	@Setter
	private Long userId;

	@Getter
	@Setter
	private Long itemId;

	@Getter
	@Setter
	private Long couponsId;

	@Getter
	@Setter
	private BigDecimal freeAmount;

    @Getter
    @Setter
    private Long skuId;

}
