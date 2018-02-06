package com.nowbook.sdp.dao;

import com.nowbook.common.model.Paging;
import com.nowbook.sdp.model.DistributorUser;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Created by pujian on 2016/5/25.
 */
@Repository
public class DistributorUserDao extends SqlSessionDaoSupport {
    public Paging<DistributorUser> getDistributorUser(DistributorUser distributorUser,Integer offset, Integer limit) {
        Long total = getSqlSession().selectOne("DistributorUser.getDistributorUserCount", distributorUser);
        if (total==0) {
            return new Paging<DistributorUser>(0L, Collections.<DistributorUser>emptyList());
        }

        distributorUser.setLimit(limit);
        distributorUser.setOffset(offset);

        List<DistributorUser> data = getSqlSession().selectList("DistributorUser.getDistributorUser", distributorUser);
        return new Paging<DistributorUser>(total, data);
    }
    public Long deleteDistributorUser(DistributorUser distributorUser) {
        getSqlSession().update("DistributorUser.deleteDistributorUser",distributorUser);
        return distributorUser.getId();
    }
    public Long insertDistributorUser(DistributorUser distributorUser) {
        getSqlSession().update("DistributorUser.insertDistributorUser",distributorUser);
        return distributorUser.getId();
    }
}
