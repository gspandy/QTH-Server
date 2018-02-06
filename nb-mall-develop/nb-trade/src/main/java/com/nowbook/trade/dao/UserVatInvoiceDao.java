package com.nowbook.trade.dao;

import com.nowbook.trade.model.UserVatInvoice;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;


/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-05 3:52 PM  <br>
 * Author:cheng
 */
@Repository
public class UserVatInvoiceDao extends SqlSessionDaoSupport {


    public UserVatInvoice get(Long id) {
        return getSqlSession().selectOne("UserVatInvoice.get", id);
    }

    public UserVatInvoice getByUserId(Long userId) {
        return getSqlSession().selectOne("UserVatInvoice.getByUserId", userId);
    }

    public Long create(UserVatInvoice userVatInvoice) {
        getSqlSession().insert("UserVatInvoice.create", userVatInvoice);
        return userVatInvoice.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("UserVatInvoice.delete", id) == 1;
    }

    public boolean update(UserVatInvoice userVatInvoice) {
        return getSqlSession().update("UserVatInvoice.update", userVatInvoice) == 1;
    }


}
