package com.nowbook.arrivegift.service;

import com.nowbook.common.model.Response;
import com.nowbook.arrivegift.dao.ExperinceDao;
import com.nowbook.arrivegift.model.Experince;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by zhum01 on 2014/10/24.
 */
@Service
public class ExperinceServiceImpl implements ExperinceService{
    @Autowired
    private ExperinceDao experinceDao;

    @Override
    public Response<Experince> queryExperinceByMap(Long shopId) {
        Response<Experince> result = new Response<Experince>();
        Experince experince =  experinceDao.queryExperinceByMap(shopId);
        result.setResult(experince);
        return result;
    }
}
