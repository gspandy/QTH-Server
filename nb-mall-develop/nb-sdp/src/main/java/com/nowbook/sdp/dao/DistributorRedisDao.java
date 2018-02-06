package com.nowbook.sdp.dao;

import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.user.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kevin on 16-4-2.
 */
@Repository
public class DistributorRedisDao {

    @Autowired
    protected JedisTemplate template;

    private static final String DISTRIBUTION_CONF = RedisKeyUtils.distributionConf();

    //    public void setDistributorConfKey(final String key, final String value) {
//        template.execute(new JedisTemplate.JedisActionNoResult() {
//            @Override
//            public void action(Jedis jedis) {
//                jedis.set(key,value);
//            }
//        });
//    }
    @Autowired
    public DistributorRedisDao(JedisTemplate template) {
        this.template = template;
    }

    public HashMap<String, String> getAllDistributionConfKey() {
        HashMap<String, String> resultMap = null;
        resultMap = (HashMap<String, String>) template.execute(new JedisTemplate.JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(DISTRIBUTION_CONF);
            }
        });
        return resultMap;
    }

    public List<String> findBySpuIds(final String... strings) {
        List<String> str = template.execute(new JedisTemplate.JedisAction<List<String>>() {
            @Override
            public List<String> action(Jedis jedis) {
                return jedis.hmget(DISTRIBUTION_CONF, strings);
            }
        });
        return str;
    }


    //分销商设置 分销等级设定
    public void setDistributionConfKey(final HashMap<String, String> hashMap) {
        HashMap<String, String> resultMap = null;
        resultMap = (HashMap<String, String>) template.execute(new JedisTemplate.JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(DISTRIBUTION_CONF);
            }
        });
        resultMap.put(hashMap.get("key"), hashMap.get("value"));
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hmset(DISTRIBUTION_CONF, hashMap);
            }
        });
    }

    public String getDistributionConfKey(final String k1) {
        String result = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(DISTRIBUTION_CONF).get(k1);
            }
        });
        return result;
    }

    public void deleteDistributionConf() {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.del(DISTRIBUTION_CONF);
            }
        });
    }



//    HashMap<String, String> hashMap = new HashMap();
//
//    String k1 = "settlement_days";
//    String v1 = "3";
//    String k2 = "withdrawal_standard";
//    String v2 = "0";
//    String k3 = "withdrawal_consumption_amount";
//    String v3 = "0";
//    String k4 = "withdrawal_consumption_amount_proportion";
//    String v4 = "0";
//    String k5 = "purchase_amount";
//    String v5 = "0";
//    String k6 = "is_audit";
//    String v6 = "1";
//    String k7 = "level_1_commission_ratio";
//    String v7 = "0003";
//    String k8 = "level_2_commission_ratio";
//    String v8 = "0003";
//    String k9 = "level_3_commission_ratio";
//    String v9 = "0003";
//    String k10 = "level_4_commission_ratio";
//    String v10 = "0003";
//
//
//
//    hashMap.put(k1,v1);
//    hashMap.put(k2,v2);
//    hashMap.put(k3,v3);
//    hashMap.put(k4,v4);
//    hashMap.put(k5,v5);
//
//    hashMap.put(k6,v6);
//    hashMap.put(k7,v7);
//    hashMap.put(k8,v8);
//    hashMap.put(k9,v9);
//    hashMap.put(k10,v10);
//    System.out.println(hashMap);
//    testRedisDao.setDistributionConfKey(hashMap);
//
//
//
//    String abc = testRedisDao.getDistributionConfKey("c");
//    System.out.print("----------"+abc);
//
//    testRedisDao.deleteDistributionConf();


}
