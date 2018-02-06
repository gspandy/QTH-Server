package com.nowbook.sdp.service;

import com.nowbook.common.model.Response;
import com.nowbook.sdp.model.DistributorSet;

import java.util.HashMap;
import java.util.List;

public interface DistributorSetService {

    Response<List<DistributorSet>> distributorSetAll();

//    Response<Boolean> updateDistributor(String key,String value);
    Response<Boolean> updateDistributor(HashMap<String,String> map);




}
