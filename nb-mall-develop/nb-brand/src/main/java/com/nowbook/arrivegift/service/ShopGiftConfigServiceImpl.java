package com.nowbook.arrivegift.service;

import com.nowbook.arrivegift.dao.ShopGiftConfigDao;
import com.nowbook.arrivegift.model.ShopGiftConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nowbook.common.model.Response;
import com.nowbook.brand.service.BrandClubServiceImpl;

/**
 * 到店有礼商家店铺预约管理
 * Created by zf on 2014/10/15.
 */
@Service
public class ShopGiftConfigServiceImpl implements ShopGiftConfigService {
	private final static Logger log = LoggerFactory
			.getLogger(BrandClubServiceImpl.class);

	@Autowired
	private ShopGiftConfigDao shopGiftConfigDao;

	@Override
	public Response<Integer> insertShopGift(ShopGiftConfig shopGift) {
		Response<Integer> result = new Response<Integer>();
		try {
			shopGiftConfigDao.insertShopGift(shopGift);
			result.setResult(200);
			return result;	
		} catch (Exception e) {
			log.error("failed to insert shopGift, cause:", e);
			result.setError("ShopGiftConfig.insertShopGift.fail");
			return result;
		}
	}		

	@Override
	public Response<ShopGiftConfig> findShopGift(Long shopId) {
		Response<ShopGiftConfig> result = new Response<ShopGiftConfig>();
		try {
			result.setResult(shopGiftConfigDao.findByShopId(shopId));
			return result;
		} catch (Exception e) {
			log.error("failed to find ShopGift, cause:", e);
			result.setError("ShopGiftConfig.findShopGift.fail");
			return result;
		}
	}

	@Override
	public Response<Boolean> updateShopGift(ShopGiftConfig shopGift) {
		Response<Boolean> result = new Response<Boolean>();
		try {
			Boolean istrue = shopGiftConfigDao.updateShopGift(shopGift);
			result.setResult(istrue);
			return result;
		} catch (Exception e) {
			log.error("failed to update nb_shopgift_config, cause:", e);
			result.setError("ShopGiftConfig.update.fail");
			return result;
		}
	}

}
