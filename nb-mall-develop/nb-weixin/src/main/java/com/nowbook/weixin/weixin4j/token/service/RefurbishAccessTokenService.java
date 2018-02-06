package com.nowbook.weixin.weixin4j.token.service;

import com.nowbook.annotations.ParamInfo;
import com.nowbook.open.dto.NbResponse;
import com.nowbook.weixin.weixin4j.token.model.AccessToken;

/**
 * Created by Administrator on 2016/7/12.
 */
public interface RefurbishAccessTokenService {
    public NbResponse<AccessToken> selectAccessToken();
    public  void updateAccessToken(@ParamInfo("access_token") String access_token);
    public  long creatAccessToken(AccessToken accessToken);
}
