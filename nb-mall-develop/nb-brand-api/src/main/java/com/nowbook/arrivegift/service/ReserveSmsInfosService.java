package com.nowbook.arrivegift.service;


import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.arrivegift.model.ReserveSmsInfos;

/**
 * Created by zhum01 on 2014/10/15.
 */
public interface ReserveSmsInfosService {
	public void create(ReserveSmsInfos reserveSmsInfos);
	
	public Response<Boolean> updateReserveSmsInfos(Long id);

    public Response<ReserveSmsInfos> checkSmsInfosBy(String sendTele, BaseUser baseUser, Long shopId, Long type);
}
