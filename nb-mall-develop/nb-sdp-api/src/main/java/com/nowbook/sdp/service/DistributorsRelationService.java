package com.nowbook.sdp.service;


import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.DistributorsRelation;

public interface DistributorsRelationService {
    Response<Boolean> deleteByPrimaryKey(Long id);

    Response<Long> insert(DistributorsRelation record);

    Response<Long> insertSelective(DistributorsRelation record);

    Response<DistributorsRelation> selectByPrimaryKey(Long id);

    Response<Boolean> updateByPrimaryKeySelective(DistributorsRelation record);

    Response<Boolean> updateByPrimaryKey(DistributorsRelation record);


}