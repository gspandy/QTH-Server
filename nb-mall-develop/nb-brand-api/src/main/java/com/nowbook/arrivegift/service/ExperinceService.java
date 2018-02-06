package com.nowbook.arrivegift.service;

import com.nowbook.common.model.Response;
import com.nowbook.arrivegift.model.Experince;

/**
 * Created by zhum01 on 2014/10/24.
 */
public interface ExperinceService {
    public Response<Experince> queryExperinceByMap(Long shopId);
}
