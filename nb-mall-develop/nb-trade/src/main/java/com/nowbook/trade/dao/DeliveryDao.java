/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

package com.nowbook.trade.dao;

import com.nowbook.trade.model.Delivery;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class DeliveryDao extends SqlSessionDaoSupport {

    public Delivery findById(Long id) {
        return getSqlSession().selectOne("Delivery.findById", id);
    }

    public void create(Delivery delivery) {
        getSqlSession().insert("Delivery.create", delivery);
    }

    public void delete(Long id) {
        getSqlSession().delete("Delivery.delete", id);
    }

    public void update(Long id, String trackCode) {
        getSqlSession().update("Delivery.update", ImmutableMap.of("id", id, "trackCode", trackCode.trim()));
    }
}
