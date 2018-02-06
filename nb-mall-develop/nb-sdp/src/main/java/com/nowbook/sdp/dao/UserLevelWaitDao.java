package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserLevelWait;
import com.nowbook.sdp.model.UserTeam;
import com.nowbook.sdp.model.UserTeamHistory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserLevelWaitDao extends SqlSessionDaoSupport {

    public void insert(UserLevelWait userLevelWait) {
        getSqlSession().insert("UserLevelWaitMapper.insert", userLevelWait);
    }

    public List<UserLevelWait> selectBy(UserLevelWait userLevelWait) {
        return getSqlSession().selectList("UserLevelWaitMapper.selectBy", userLevelWait);
    }

    public void update(UserLevelWait userLevelWait) {
        getSqlSession().update("UserLevelWaitMapper.update", userLevelWait);
    }
}