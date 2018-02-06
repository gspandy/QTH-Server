package com.nowbook.sdp.service;

import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.ConcernMemberDao;
import com.nowbook.sdp.model.ConcernMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Created by pujian on 2016/5/24.
 */
@Service
public class ConcernMemberServiceImpl implements ConcernMemberService {
    private final static Logger log = LoggerFactory.getLogger(ConcernMemberServiceImpl.class);

    @Autowired
    private ConcernMemberDao concernMemberDao;
    @Override
    public Response<Paging<ConcernMember>> getConcernMember(ConcernMember concernMember,Integer pageNo, Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<ConcernMember>> result = new Response<Paging<ConcernMember>>();
        Paging<ConcernMember> concernMemberPaging;

        try {
            concernMemberPaging = concernMemberDao.getConcernMember(concernMember,page.getOffset(),page.getLimit());
            result.setResult(concernMemberPaging);
            return result;
        }catch (Exception e) {
            log.error("failed to find all ConcernMember, cause:", e);
            result.setError("ConcernMember.query.fail");
            return result;
        }
    }

    @Override
    public Response<Long> createConcernMember(ConcernMember concernMember) {
        Response<Long> result = new Response<Long>();
        try {
            //删除原来的userId和分销商Id的关系
            concernMemberDao.deleteConcernMember(concernMember);
            //新增userId和分销商Id的关系
            Long num = concernMemberDao.insertConcernMember(concernMember);
            result.setResult(num);
            return result;
        }catch (Exception e) {
            log.error("failed to find all ConcernMember, cause:", e);
            result.setError("api.ConcernMember.createConcernMember.fail");
            return result;
        }
    }
}
