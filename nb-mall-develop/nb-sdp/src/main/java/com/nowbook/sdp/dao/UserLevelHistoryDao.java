package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserLevel;
import com.nowbook.sdp.model.UserLevelHistory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserLevelHistoryDao extends SqlSessionDaoSupport {

    public void insert(UserLevelHistory userLevelHistory) {
        getSqlSession().insert("UserLevelHistoryMapper.insert", userLevelHistory);
    }

    public List<UserLevel> selectByUserId(UserLevelHistory userLevelHistory) {
        return getSqlSession().selectList("UserLevelHistoryMapper.selectByUserId", userLevelHistory);
    }
}