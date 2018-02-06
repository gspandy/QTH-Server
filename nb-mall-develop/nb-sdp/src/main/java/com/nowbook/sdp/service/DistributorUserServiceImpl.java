package com.nowbook.sdp.service;

import com.nowbook.common.model.PageInfo;
import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.DistributorUserDao;
import com.nowbook.sdp.model.DistributorUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by pujian on 2016/5/25.
 */
@Service
public class DistributorUserServiceImpl implements DistributorUserService{
    private final static Logger log = LoggerFactory.getLogger(DistributorUserServiceImpl.class);
    @Autowired
    private DistributorUserDao distributorUserDao;

    @Override
    public Response<Paging<DistributorUser>> getDistributorUser(DistributorUser distributorUser,Integer pageNo, Integer size) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<DistributorUser>> result = new Response<Paging<DistributorUser>>();
        Paging<DistributorUser> distributorUserPaging;

        try {
            distributorUserPaging = distributorUserDao.getDistributorUser(distributorUser,page.getOffset(),page.getLimit());
            result.setResult(distributorUserPaging);
            return result;
        }catch (Exception e) {
            log.error("failed to find all DistributorUser, cause:", e);
            result.setError("DistributorUser.query.fail");
            return result;
        }
    }

    @Override
    public Response<Long> createDistributorUser(DistributorUser distributorUser) {
        Response<Long> result = new Response<Long>();
        try {
            distributorUserDao.deleteDistributorUser(distributorUser);
            Long num = distributorUserDao.insertDistributorUser(distributorUser);
            result.setResult(num);
            return result;
        }catch (Exception e) {
            log.error("failed to find all createDistributorUser, cause:", e);
            result.setError("api.DistributorUser.createDistributorUser.fail");
            return result;
        }
    }
}
