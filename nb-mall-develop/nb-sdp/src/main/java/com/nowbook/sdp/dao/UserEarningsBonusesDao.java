package com.nowbook.sdp.dao;


import com.nowbook.common.model.Paging;
import com.nowbook.sdp.model.UserEarningsBonuses;
import com.nowbook.sdp.model.UserWallet;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserEarningsBonusesDao extends SqlSessionDaoSupport {

    public void insert(UserEarningsBonuses userEarningsBonuses) {
        getSqlSession().insert("UserEarningsBonusesMapper.insert", userEarningsBonuses);
    }

    public void update(UserEarningsBonuses userEarningsBonuses) {
        getSqlSession().update("UserEarningsBonusesMapper.update", userEarningsBonuses);
    }

    public List<UserEarningsBonuses> selectBy(UserEarningsBonuses userEarningsBonuses) {
        return getSqlSession().selectList("UserEarningsBonusesMapper.selectBy", userEarningsBonuses);
    }

    public List<UserEarningsBonuses> sum(UserEarningsBonuses userEarningsBonuses) {
        return getSqlSession().selectList("UserEarningsBonusesMapper.sum", userEarningsBonuses);
    }

    public Paging<UserEarningsBonuses> selectOrderDetail(UserEarningsBonuses userEarningsBonuses) {
        Paging<UserEarningsBonuses> result = new Paging<UserEarningsBonuses>();
        Long total = getSqlSession().selectOne("UserEarningsBonusesMapper.countOrderDetail", userEarningsBonuses);
        List<UserEarningsBonuses> list = getSqlSession().selectList("UserEarningsBonusesMapper.selectOrderDetail", userEarningsBonuses);
        result.setData(list);
        result.setTotal(total);
        return result;
    }

}