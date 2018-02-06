package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserTeam;
import com.nowbook.sdp.model.UserTeamHistory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserTeamHistoryDao extends SqlSessionDaoSupport {

    public void insert(UserTeamHistory userTeamHistory) {
        getSqlSession().insert("UserTeamHistoryMapper.insert", userTeamHistory);
    }

    public List<UserTeam> selectBy(UserTeamHistory userTeamHistory) {
        return getSqlSession().selectList("UserTeamHistoryMapper.selectBy", userTeamHistory);
    }
}