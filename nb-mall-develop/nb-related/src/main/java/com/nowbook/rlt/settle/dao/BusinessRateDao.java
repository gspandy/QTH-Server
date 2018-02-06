package com.nowbook.rlt.settle.dao;

import com.nowbook.rlt.settle.model.BusinessRate;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-03-14 10:21 AM  <br>
 * Author:cheng
 */
@Repository
public class BusinessRateDao extends SqlSessionDaoSupport {

    public Long create(BusinessRate rate) {
        getSqlSession().insert("BusinessRate.create", rate);
        return rate.getId();
    }

    public BusinessRate get(Long id) {
        return getSqlSession().selectOne("BusinessRate.get", id);
    }

    public BusinessRate findByBusiness(Long business) {
        return getSqlSession().selectOne("BusinessRate.findByBusiness",
                ImmutableMap.of("business", business));
    }
}
