package com.nowbook.rlt.settle.dao;

import com.nowbook.rlt.settle.model.AbnormalTrans;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-01-23 2:04 PM  <br>
 * Author:cheng
 */
@Repository
public class AbnormalTransDao extends SqlSessionDaoSupport{

    public Long create(AbnormalTrans abnormalTrans) {
        checkNotNull(abnormalTrans.getSettlementId());
        checkNotNull(abnormalTrans.getOrderId());

        getSqlSession().insert("AbnormalTrans.create", abnormalTrans);
        return abnormalTrans.getId();
    }


    public AbnormalTrans get(Long id) {
        return getSqlSession().selectOne("AbnormalTrans.get", id);
    }
}
