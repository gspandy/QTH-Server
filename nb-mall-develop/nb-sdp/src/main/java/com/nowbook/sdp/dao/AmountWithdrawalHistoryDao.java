package com.nowbook.sdp.dao;


import com.nowbook.common.model.Paging;
import com.nowbook.sdp.model.AmountWithdrawalHistory;
import com.nowbook.sdp.model.AmountWithdrawalHistoryForQuery;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class AmountWithdrawalHistoryDao extends SqlSessionDaoSupport {
    public void deleteByPrimaryKey(Long id){
        getSqlSession().delete("AmountWithdrawalHistoryMapper.deleteByPrimaryKey", id);
    }

    public void insert(AmountWithdrawalHistory record){
        getSqlSession().insert("AmountWithdrawalHistoryMapper.insert", record);
    }

    public void insertSelective(AmountWithdrawalHistory record){
        getSqlSession().insert("AmountWithdrawalHistoryMapper.insertSelective", record);
    }

    public AmountWithdrawalHistory selectByPrimaryKey(Long id){
        return getSqlSession().selectOne("AmountWithdrawalHistoryMapper.selectByPrimaryKey", id);
    }

    public void updateByPrimaryKeySelective(AmountWithdrawalHistory record){
        getSqlSession().update("AmountWithdrawalHistoryMapper.updateByPrimaryKeySelective", record);
    }

    public void updateByPrimaryKey(AmountWithdrawalHistory record){
        getSqlSession().update("AmountWithdrawalHistoryMapper.updateByPrimaryKey", record);
    }
    public Paging<AmountWithdrawalHistoryForQuery> AmountWithdrawalHistoryForQuery(String shopName,Integer offset, Integer limit) {
        Long total = getSqlSession().selectOne("AmountWithdrawalHistoryMapper.amountWithdrawalHistoryCount", ImmutableMap.of("shopName", shopName, "offset", offset, "limit", limit));
        if(total == 0) {
            return new Paging<AmountWithdrawalHistoryForQuery>(0L, Collections.<AmountWithdrawalHistoryForQuery>emptyList());
        }
        List<AmountWithdrawalHistoryForQuery> disAllPage = getSqlSession().selectList("AmountWithdrawalHistoryMapper.amountWithdrawalHistory", ImmutableMap.of("shopName", shopName,"offset", offset, "limit", limit));
        return new Paging<AmountWithdrawalHistoryForQuery>(total, disAllPage);
    }


    public Paging<AmountWithdrawalHistory> selectWithdrawal(AmountWithdrawalHistory withdrawal,Integer offset, Integer limit) {
        withdrawal.setOffset(offset);
        withdrawal.setLimit(limit);
        Long total = getSqlSession().selectOne("AmountWithdrawalHistoryMapper.withdrawalCount", withdrawal);
        if(total == 0) {
            return new Paging<AmountWithdrawalHistory>(0L, Collections.<AmountWithdrawalHistory>emptyList());
        }
        List<AmountWithdrawalHistory> disAllPage = getSqlSession().selectList("AmountWithdrawalHistoryMapper.withdrawal",withdrawal);
        return new Paging<AmountWithdrawalHistory>(total, disAllPage);
    }
}