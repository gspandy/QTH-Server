package com.nowbook.coupons.dao;

import com.nowbook.coupons.model.NbCouOrderItem;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2014/8/24.
 */
@Repository
public class NbCouOrderItemDao extends SqlSessionDaoSupport {
    public Boolean saveCouOrderItem(NbCouOrderItem nbCouOrderItem) {
        return getSqlSession().insert("NbCouOrderItem.saveCouOrderItem",nbCouOrderItem) == 1;
    }
}
