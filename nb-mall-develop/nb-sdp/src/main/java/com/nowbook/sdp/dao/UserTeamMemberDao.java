package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserTeamMember;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserTeamMemberDao extends SqlSessionDaoSupport {
    public void insert(UserTeamMember userTeamMember) {
        getSqlSession().insert("UserTeamMemberMapper.insert", userTeamMember);
    }

    public void deleteByMember(UserTeamMember userTeamMember) {
        getSqlSession().delete("UserTeamMemberMapper.deleteByMember", userTeamMember);
    }

    public void updateByMember(UserTeamMember userTeamMember) {
        getSqlSession().update("UserTeamMemberMapper.updateByMember", userTeamMember);
    }

    public List<UserTeamMember> selectBy(UserTeamMember userTeamMember) {
        return getSqlSession().selectList("UserTeamMemberMapper.selectBy", userTeamMember);
    }
}