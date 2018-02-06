package com.nowbook.brand.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.common.model.Response;
import com.nowbook.user.base.BaseUser;
import com.nowbook.brand.model.SmsConfigDto;

public interface SmsConfigService {
		
	public void updateSmsConfig(SmsConfigDto smsConfigCndDto) throws Exception;

	public Response<SmsConfigDto> selectSmsConfig(
			@ParamInfo("baseUser") BaseUser baseUser);

}
