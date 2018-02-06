package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserBank;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserBankDao extends SqlSessionDaoSupport {

    public void insert(UserBank userBank) {
        getSqlSession().insert("UserBankMapper.insert", userBank);
    }

    public void updateByUserId(UserBank userBank) {
        getSqlSession().update("UserBankMapper.updateByUserId", userBank);
    }

    public void deleteById(UserBank userBank) {
        getSqlSession().update("UserBankMapper.deleteById", userBank);
    }

    public List<UserBank> selectByUserId(UserBank userBank) {
        return getSqlSession().selectList("UserBankMapper.selectByUserId", userBank);
    }
}