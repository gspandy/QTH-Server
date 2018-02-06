package com.nowbook.sdp.service;

import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.ConcernMember;

/**
 * Created by pujian on 2016/5/25.
 */
public interface ConcernMemberService {
    Response<Paging<ConcernMember>> getConcernMember(ConcernMember concernMember,Integer pageNo, Integer size);
    Response<Long> createConcernMember(ConcernMember concernMember);
}
