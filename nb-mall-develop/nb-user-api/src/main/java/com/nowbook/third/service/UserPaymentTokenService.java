package com.nowbook.third.service;

import com.nowbook.third.model.ThirdUser;
import com.nowbook.third.model.token.JwtToken;
import com.nowbook.user.model.User;

/**
 * Created by zhaop01 on 2014/9/2.
 */
public interface UserPaymentTokenService {

    void saveUserPaymentToken(Long userId ,String prepayId,JwtToken jwtToken);

    void delUserPaymentToken(Long userId ,String prepayId);
    JwtToken getUserPaymentToken(Long userId,String prepayId);
}
