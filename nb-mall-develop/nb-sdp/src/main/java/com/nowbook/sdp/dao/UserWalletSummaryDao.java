package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserWallet;
import com.nowbook.sdp.model.UserWalletSummary;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserWalletSummaryDao extends SqlSessionDaoSupport {

    public void insert(UserWalletSummary userWalletSummary) {
        getSqlSession().insert("UserWalletSummaryMapper.insert", userWalletSummary);
    }

    public List<UserWalletSummary> selectByPayCode(UserWalletSummary userWalletSummary) {
        return getSqlSession().selectList("UserWalletSummaryMapper.selectByPayCode", userWalletSummary);
    }

    public List<UserWalletSummary> selectBy(UserWalletSummary userWalletSummary) {
        return getSqlSession().selectList("UserWalletSummaryMapper.selectBy", userWalletSummary);
    }

    public UserWalletSummary selectNum(UserWalletSummary userWalletSummary) {
        return getSqlSession().selectOne("UserWalletSummaryMapper.selectNum", userWalletSummary);
    }

    public List<UserWalletSummary> selectByUserIdFirst(UserWalletSummary userWalletSummary){
        return getSqlSession().selectList("UserWalletSummaryMapper.selectByUserIdFirst", userWalletSummary);
    }
}