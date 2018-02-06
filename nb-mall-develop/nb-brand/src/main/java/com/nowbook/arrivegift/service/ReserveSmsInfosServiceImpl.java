package com.nowbook.arrivegift.service;

import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.arrivegift.model.ReserveSmsInfos;
import com.nowbook.arrivegift.dao.ReserveSmsInfosDao;
import com.nowbook.brand.service.BrandClubServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 到店有礼预约短信管理 Created by zf on 2014/10/15.
 */
@Service
public class ReserveSmsInfosServiceImpl implements ReserveSmsInfosService {
	private final static Logger log = LoggerFactory
			.getLogger(BrandClubServiceImpl.class);

	@Autowired
	private ReserveSmsInfosDao reserveSmsInfosDao;

	@Override
	public void create(ReserveSmsInfos reserveSmsInfos) {
		reserveSmsInfosDao.create(reserveSmsInfos);
	}



	@Override
	public Response<Boolean> updateReserveSmsInfos(Long id) {
		Response<Boolean> result = new Response<Boolean>();
		try {
			Boolean istrue = reserveSmsInfosDao.updateReserveSmsInfos(id);
			result.setResult(istrue);
			return result;
		} catch (Exception e) {
			log.error("failed to update updateReserveSmsInfos, cause:", e);
			result.setError("ReserveSmsInfosServiceImpl.update.fail");
			return result;
		}
	}

	@Override
	public Response<ReserveSmsInfos> checkSmsInfosBy(String sendTele,
			BaseUser baseUser, Long shopId, Long type) {
		Response<ReserveSmsInfos> result = new Response<ReserveSmsInfos>();
		try {
			ReserveSmsInfos reserveSmsInfos = reserveSmsInfosDao
					.checkSmsInfosBy(sendTele, baseUser, shopId, type);
			result.setResult(reserveSmsInfos);
			return result;
		} catch (Exception e) {
			log.error("failed to update updateReserveSmsInfos, cause:", e);
			result.setError("ReserveSmsInfosServiceImpl.update.fail");
			return result;
		}
	}
}
