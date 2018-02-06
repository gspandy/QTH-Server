package com.nowbook.sdp.service;

import com.nowbook.common.model.Response;
import com.nowbook.sdp.dao.DistributorRedisDao;
import com.nowbook.sdp.model.DistributorSet;
import com.nowbook.web.misc.MessageSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by mark on 2014/7/11.
 */
@Service
public class DistributorSetServiceImpl implements DistributorSetService {

    private final static Logger log = LoggerFactory.getLogger(DistributorSetServiceImpl.class);

    @Autowired
    private DistributorRedisDao distributorRedisDao;

    @Autowired
    private MessageSources messageSources;

    private  Properties prop;

    @Override
    public Response<List<DistributorSet>> distributorSetAll() {

        Response<List<DistributorSet>> distributorSet = new Response<List<DistributorSet>>();

        Map<String, String> map = distributorRedisDao.getAllDistributionConfKey();
        DistributorSet model = null;
        List<DistributorSet> list = new ArrayList<DistributorSet>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            model = new DistributorSet();
            if("".equals(getDistributorCofig().getProperty(key))){
                model.setKey(key);
            }else {
                model.setKey(getDistributorCofig().getProperty(key));
            }
            model.setValue(value);
            list.add(model);
        }
        distributorSet.setResult(list);

        return distributorSet;
    }

    @Override
    public Response<Boolean> updateDistributor(HashMap<String, String> map) {
        Response<Boolean> ret = new Response<Boolean>();
        distributorRedisDao.setDistributionConfKey(map);
        ret.setResult(Boolean.TRUE);
        return ret;
    }

    private Properties getDistributorCofig(){

        if(prop==null){
            prop = new Properties();
            try {
                prop.load(new InputStreamReader(DistributorSetServiceImpl.class.getClassLoader().getResourceAsStream("distributor.properties"), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return prop;
    }


}
