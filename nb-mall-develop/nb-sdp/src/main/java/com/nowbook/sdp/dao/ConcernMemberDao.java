package com.nowbook.sdp.dao;

import com.nowbook.common.model.Paging;
import com.nowbook.sdp.model.ConcernMember;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by pujian on 2016/5/24.
 */
@Repository
public class ConcernMemberDao extends SqlSessionDaoSupport {
    public Paging<ConcernMember> getConcernMember(ConcernMember concernMember,Integer offset, Integer limit) {
        Long total = getSqlSession().selectOne("ConcernMember.getConcernMemberCount", concernMember);
        if (total==0) {
            return new Paging<ConcernMember>(0L, Collections.<ConcernMember>emptyList());
        }

        concernMember.setLimit(limit);
        concernMember.setOffset(offset);

        List<ConcernMember> data = getSqlSession().selectList("ConcernMember.getConcernMember", concernMember);
        return new Paging<ConcernMember>(total, data);
    }

    public Long insertConcernMember(ConcernMember concernMember) {
        getSqlSession().update("ConcernMember.insertConcernMember",concernMember);
        return concernMember.getId();
    }
    public Long deleteConcernMember(ConcernMember concernMember) {
        getSqlSession().update("ConcernMember.deleteConcernMember",concernMember);
        return concernMember.getId();
    }

}
