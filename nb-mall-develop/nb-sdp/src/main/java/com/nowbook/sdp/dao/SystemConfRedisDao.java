package com.nowbook.sdp.dao;

import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.user.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by Romo on 17-8-22.
 */
@Repository
public class SystemConfRedisDao {

    @Autowired
    protected JedisTemplate template;

    private static final String DISTRIBUTION_CONF = RedisKeyUtils.profitConf();

    private static final String OTHER_CONF = RedisKeyUtils.otherConf();

    // 获取所有返润日前设置
    public Map<String, String> getProfitDates() {
        return template.execute(new JedisTemplate.JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(DISTRIBUTION_CONF);
            }
        });
    }

    // 获取所有其他日前设置
    public Map<String, String> getOthers() {
        return template.execute(new JedisTemplate.JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(RedisKeyUtils.otherConf());
            }
        });
    }

    // 返润日期设置
    public void updateProfitDate(final Map<String, String> map) {
        Map<String, String> resultMap = template.execute(new JedisTemplate.JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(DISTRIBUTION_CONF);
            }
        });
        resultMap.put(map.get("key"), map.get("value"));
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hmset(DISTRIBUTION_CONF, map);
            }
        });
    }

    // 其他设置
    public void updateOthers(final Map<String, String> map) {
        Map<String, String> resultMap = template.execute(new JedisTemplate.JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(RedisKeyUtils.otherConf());
            }
        });
        resultMap.put(map.get("key"), map.get("value"));
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hmset(RedisKeyUtils.otherConf(), map);
            }
        });
    }

    // 获取单个返润日期
    public String getDate(final String key) {
        return template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hget(DISTRIBUTION_CONF, key);
            }
        });
    }

    // 获取其他设置
    public String getOtherDate(final String key) {
        return template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hget(OTHER_CONF, key);
            }
        });
    }

}
