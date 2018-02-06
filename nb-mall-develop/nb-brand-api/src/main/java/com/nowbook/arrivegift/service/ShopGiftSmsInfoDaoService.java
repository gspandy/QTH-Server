package com.nowbook.arrivegift.service;

import java.util.Map;

import com.nowbook.common.model.Paging;
import com.nowbook.arrivegift.model.ReserveSmsInfos;
;

/**
 * Created by zf on 2014/10/15.
 */
public interface ShopGiftSmsInfoDaoService {

	public Paging<ReserveSmsInfos> findModelReserveSmsInfo(Map<String, Object> params,Integer offset, Integer limit);

}
