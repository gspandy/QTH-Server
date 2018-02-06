package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserTeam;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserTeamDao extends SqlSessionDaoSupport {

    public void insert(UserTeam userTeam) {
        getSqlSession().insert("TeamMapper.insert", userTeam);
    }

    public void delete(UserTeam userTeam) {
        getSqlSession().delete("TeamMapper.delete", userTeam);
    }

    public void update(UserTeam userTeam) {
        getSqlSession().update("TeamMapper.update", userTeam);
    }

    public List<UserTeam> selectBy(UserTeam userTeam) {
        return getSqlSession().selectList("TeamMapper.selectBy", userTeam);
    }
}