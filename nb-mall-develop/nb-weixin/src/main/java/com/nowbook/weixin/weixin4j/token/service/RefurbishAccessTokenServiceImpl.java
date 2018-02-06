package com.nowbook.weixin.weixin4j.token.service;

import com.nowbook.open.dto.NbResponse;
import com.nowbook.weixin.weixin4j.token.dao.RefurbishAccessTokenDao;
import com.nowbook.weixin.weixin4j.token.model.AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2016/7/12.
 */
@Service
@Slf4j
public class RefurbishAccessTokenServiceImpl implements  RefurbishAccessTokenService{
    @Autowired
    private RefurbishAccessTokenDao refurbishAccessTokenDao;
    @Override
    public NbResponse<AccessToken> selectAccessToken() {
        NbResponse<AccessToken> result = new NbResponse<AccessToken>();
        AccessToken accessToken =  refurbishAccessTokenDao.selectAccessToken();
        if(accessToken ==null){
            result.setError("accessToken.not.found");
            return result;
        }
        result.setResult(accessToken);
        result.setSuccess(true);
        return result;
    }

    @Override
    public void updateAccessToken(String access_token) {
        refurbishAccessTokenDao.updateAccessToken(access_token);
    }

    @Override
    public long creatAccessToken(AccessToken accessToken) {
        return refurbishAccessTokenDao.creatAccessToken(accessToken);
    }
}
