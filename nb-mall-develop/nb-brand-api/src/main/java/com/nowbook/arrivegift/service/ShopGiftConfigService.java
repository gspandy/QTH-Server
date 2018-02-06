package com.nowbook.arrivegift.service;

import com.nowbook.common.model.Response;
import com.nowbook.arrivegift.model.ShopGiftConfig;

/**
 * Created by zf on 2014/10/15.
 */
public interface ShopGiftConfigService {
	
	public Response<Integer> insertShopGift(ShopGiftConfig shopGift);
	public Response<Boolean> updateShopGift(ShopGiftConfig shopGift);
	public Response<ShopGiftConfig> findShopGift(Long shopId);

}
