package com.nowbook.arrivegift.service;


import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.arrivegift.model.ReserveSmsConfig;
	
/**
 * Created by zf on 2014/10/16.
 */
public interface ReserveSmsConfigService {
		
	public Response<Integer> insertReserveSmsInfo(ReserveSmsConfig smsInfo);
	public Response<Boolean> updateReserveSmsInfo(ReserveSmsConfig smsInfo);
	public Response<Boolean> delReserveSmsInfo(Long id);	
	public Response<Paging<ReserveSmsConfig>> findReserveSmsInfo(@ParamInfo("baseUser") BaseUser baseUser,@ParamInfo("pageNo") Integer pageNo,
            @ParamInfo("size") Integer size);

    public Response<ReserveSmsConfig> querySmsConfigInfo(Long type, Long shopId);
}