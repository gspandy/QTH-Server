package com.nowbook.weixin.weixin4j.token.dao;

import com.google.common.collect.ImmutableMap;
import com.nowbook.weixin.weixin4j.token.model.AccessToken;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2016/7/12.
 */
@Repository
public class RefurbishAccessTokenDao extends SqlSessionDaoSupport {
    public boolean updateAccessToken(String access_token ) {
        return getSqlSession().update("AccessToken.updateAccessToken",
                ImmutableMap.of("access_token", access_token)) == 1;
    }

    public AccessToken selectAccessToken(){
        return getSqlSession().selectOne("AccessToken.selectAccessToken");
    }

    public Long creatAccessToken(AccessToken accessToken){
        getSqlSession().insert("AccessToken.createAccessToken", accessToken);
        return accessToken.getId();
    }
}
