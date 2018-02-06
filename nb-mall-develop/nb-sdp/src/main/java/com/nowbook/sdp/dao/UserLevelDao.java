package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserLevel;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserLevelDao extends SqlSessionDaoSupport {

    public void insert(UserLevel userLevel) {
        getSqlSession().insert("UserLevelMapper.insert", userLevel);
    }

    public void updateByUserId(UserLevel userLevel) {
        getSqlSession().update("UserLevelMapper.updateByUserId", userLevel);
    }

    public List<UserLevel> selectByUserId(UserLevel userLevel) {
        return getSqlSession().selectList("UserLevelMapper.selectByUserId", userLevel);
    }

    public List<UserLevel> selectByInviterAndLevel(UserLevel userLevel) {
        return getSqlSession().selectList("UserLevelMapper.selectByInviterAndLevel", userLevel);
    }
}