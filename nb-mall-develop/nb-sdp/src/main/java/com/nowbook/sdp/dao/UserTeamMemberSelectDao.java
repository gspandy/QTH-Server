package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserTeamMember;
import com.nowbook.sdp.model.UserTeamMemberSelect;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserTeamMemberSelectDao extends SqlSessionDaoSupport {
    public List<UserTeamMemberSelect> selectUser(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectList("UserTeamMemberSelectMapper.selectUser", userTeamMemberSelect);
    }

    public UserTeamMemberSelect selectUserNum(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectOne("UserTeamMemberSelectMapper.selectUserNum", userTeamMemberSelect);
    }
    public List<UserTeamMemberSelect> selectMyLevel(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectList("UserTeamMemberSelectMapper.selectMyLevel", userTeamMemberSelect);
    }

    public List<UserTeamMemberSelect> selectMyInviter(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectList("UserTeamMemberSelectMapper.selectMyInviter", userTeamMemberSelect);
    }

    public List<UserTeamMemberSelect> selectUserTeamMember(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectList("UserTeamMemberSelectMapper.selectUserTeamMember", userTeamMemberSelect);
    }

    public UserTeamMemberSelect selectMyInviterNum(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectOne("UserTeamMemberSelectMapper.selectMyInviterNum", userTeamMemberSelect);
    }

    public UserTeamMemberSelect selectMemberNum(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectOne("UserTeamMemberSelectMapper.selectMemberNum", userTeamMemberSelect);
    }

    public UserTeamMemberSelect selectInviterNum(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectOne("UserTeamMemberSelectMapper.selectInviterNum", userTeamMemberSelect);
    }

    public List<UserTeamMemberSelect> selectMyInviterForBlack(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectList("UserTeamMemberSelectMapper.selectMyInviterForBlack", userTeamMemberSelect);
    }

    public UserTeamMemberSelect selectMyInviterNumForBlack(UserTeamMemberSelect userTeamMemberSelect) {
        return getSqlSession().selectOne("UserTeamMemberSelectMapper.selectMyInviterNumForBlack", userTeamMemberSelect);
    }
}