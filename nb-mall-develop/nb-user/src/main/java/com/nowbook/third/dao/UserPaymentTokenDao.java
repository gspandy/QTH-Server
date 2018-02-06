package com.nowbook.third.dao;

import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.redis.utils.KeyUtils;
import com.nowbook.user.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

/**
 * Created by Administrator on 2017/8/3.
 */
@Repository
public class UserPaymentTokenDao {
    private final JedisTemplate jedisTemplate;

    @Autowired
    public UserPaymentTokenDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    public void setUserPaymentToken(final long userId,final String prepayId, final String token) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hset("payment-token:" + userId,prepayId,token);
            }
        });
    }

    public String getUserPaymentToken(final long userId,final String prepayId) {
        return   jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hget("payment-token:" + userId,prepayId);
            }
        });
    }

    public void delUserPaymentToken(final long userId,final String prepayId) {
            jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hdel("payment-token:" + userId,prepayId);
            }
        });
    }
}
