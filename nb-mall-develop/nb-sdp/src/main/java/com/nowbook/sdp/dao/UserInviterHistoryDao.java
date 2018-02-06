package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserBank;
import com.nowbook.sdp.model.UserInviterHistory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserInviterHistoryDao extends SqlSessionDaoSupport {

    public void insert(UserInviterHistory userInviterHistory) {
        getSqlSession().insert("UserInviterHistoryMapper.insert", userInviterHistory);
    }

    public List<UserInviterHistory> selectByUserId(UserInviterHistory userInviterHistory) {
        return getSqlSession().selectList("UserInviterHistoryMapper.selectByUserId", userInviterHistory);
    }
}