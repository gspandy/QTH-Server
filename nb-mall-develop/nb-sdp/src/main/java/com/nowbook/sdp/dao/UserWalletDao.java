package com.nowbook.sdp.dao;


import com.nowbook.sdp.model.UserWallet;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserWalletDao extends SqlSessionDaoSupport {

    public void insert(UserWallet userWallet) {
        getSqlSession().insert("UserWalletMapper.insert", userWallet);
    }

    public void update(UserWallet userWallet) {
        getSqlSession().update("UserWalletMapper.updateByUserId", userWallet);
    }

    public List<UserWallet> selectBy(UserWallet userWallet) {
        return getSqlSession().selectList("UserWalletMapper.selectBy", userWallet);
    }

    public UserWallet selectByMobile(String mobile) {
        return getSqlSession().selectOne("UserWalletMapper.selectByMobile", mobile);
    }
}