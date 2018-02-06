package com.nowbook.rlt.settle.dao;

import com.nowbook.rlt.settle.model.AlipayTransLoad;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import static com.nowbook.common.utils.Arguments.notNull;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-05-15 2:06 PM  <br>
 * Author:cheng
 */
@Repository
public class AlipayTransLoadDao extends SqlSessionDaoSupport {

    public Long create(AlipayTransLoad alipayTransLoad) {
        getSqlSession().insert("AlipayTransLoad.create", alipayTransLoad);
        return alipayTransLoad.getId();
    }

    public AlipayTransLoad get(Long id) {
        return getSqlSession().selectOne("AlipayTransLoad.get", id);

    }

    public boolean delete(Long id) {
        return getSqlSession().delete("AlipayTransLoad.get", id) == 1;
    }

    public boolean createOrUpdate(AlipayTransLoad alipayTransLoad) {
        AlipayTransLoad load = getSqlSession().selectOne("AlipayTransLoad.getBy", alipayTransLoad);
        if (notNull(load)) {
            load.setStatus(alipayTransLoad.getStatus());
            return getSqlSession().update("AlipayTransLoad.update", alipayTransLoad) == 1;
        }

        return getSqlSession().insert("AlipayTransLoad.create", alipayTransLoad) == 1;
    }

    public AlipayTransLoad getBy(AlipayTransLoad alipayTransLoad) {
        return getSqlSession().selectOne("AlipayTransLoad.getBy", alipayTransLoad);
    }
}
