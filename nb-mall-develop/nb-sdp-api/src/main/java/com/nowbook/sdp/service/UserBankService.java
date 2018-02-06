package com.nowbook.sdp.service;

import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.model.UserBank;
import com.nowbook.sdp.model.UserTeamMemberSelect;

import java.util.List;

/**
 * @author dpzh
 * @create 2017-07-26 10:22
 * @description: levelService
 **/
public interface UserBankService {
    Response<UserBank> viewBank(Long userId);

    Response<UserBank> viewBankForPayment(Long userId);

    Response<Boolean> bindingBank(UserBank userBank);

    Response<Boolean> unBindingBank(UserBank userBank);
}
