package com.nowbook.sdp.dao;

import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by zhum01 on 2014/7/30.
 */
@Repository
public class DistributorsAuditDao extends SqlSessionDaoSupport{

    public void updateAuditStatus(Long id, String auditStatus) {
        getSqlSession().update("DistributorsAudit.updateAuditStatus", ImmutableMap.of(
                "id", id, "auditStatus", auditStatus
        ));
    }

    public Boolean distributorsAuditCreat(Long id) {
        return getSqlSession().insert("DistributorsAudit.disAuditCreat",id)==1;
    }






}
