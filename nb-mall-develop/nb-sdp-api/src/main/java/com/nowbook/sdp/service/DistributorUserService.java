package com.nowbook.sdp.service;

import com.nowbook.common.model.Paging;
import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.DistributorUser;

/**
 * Created by pujian on 2016/5/25.
 */
public interface DistributorUserService {
    Response<Paging<DistributorUser>> getDistributorUser(DistributorUser distributorUser,Integer pageNo, Integer size);
    Response<Long> createDistributorUser(DistributorUser distributorUser);
}
