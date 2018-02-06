package com.nowbook.third.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowbook.third.common.CommonUtils;
import com.nowbook.third.common.ThirdPartEnum;
import com.nowbook.third.dao.ThirdPartLoginDao;
import com.nowbook.third.dao.UserPaymentTokenDao;
import com.nowbook.third.model.ThirdUser;
import com.nowbook.third.model.token.JwtToken;
import com.nowbook.third.utils.IDSAPIClient;
import com.nowbook.user.model.User;
import com.nowbook.user.mysql.UserDao;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaop01 on 2014/9/2.
 */
@Service
public class UserPaymentTokenServiceImpl implements  UserPaymentTokenService {

    @Autowired
    private UserPaymentTokenDao userPaymentTokenDao;

    @Override
    public void saveUserPaymentToken(Long userId,String prepayId,JwtToken jwtToken) {
        userPaymentTokenDao.setUserPaymentToken(userId,prepayId,jwtToken.getToken());
    }

    @Override
    public void delUserPaymentToken(Long userId, String prepayId) {
        userPaymentTokenDao.delUserPaymentToken(userId,prepayId);
    }

    @Override
    public JwtToken getUserPaymentToken(Long userId,String prepayId) {
        JwtToken result = new JwtToken();
        String token = userPaymentTokenDao.getUserPaymentToken(userId,prepayId);
        result.setToken(token);
        return result;
    }
}
