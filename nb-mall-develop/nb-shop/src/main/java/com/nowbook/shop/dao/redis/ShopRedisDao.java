package com.nowbook.shop.dao.redis;

import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.redis.utils.KeyUtils;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import com.nowbook.user.util.RedisKeyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangzefeng on 13-12-16
 */
@Repository
public class ShopRedisDao {

    private final JedisTemplate jedisTemplate;

    @Autowired
    public ShopRedisDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    public String findById(final long shopId) {
        String exist = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.shopItemCount(shopId));
            }
        });
        //缓存失效返回-1
        if (Strings.isNullOrEmpty(exist))
            exist = "-1";
        return exist;
    }

    public void incrShopSoldQuantityCount(final long shopId, final long soldQuantity) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.incrBy(KeyUtils.shopSoldQuantityCount(shopId), soldQuantity);
            }
        });
    }

    public void incrShopSalesCount(final long shopId, final long sales) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.incrBy(KeyUtils.shopSalesCount(shopId), sales);
            }
        });
    }

    public String findShopSoldQuantityById(final long shopId) {
        String soldQuantity = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.shopSoldQuantityCount(shopId));
            }
        });
        if(Strings.isNullOrEmpty(soldQuantity)) {
            return "0";
        }
        return soldQuantity;
    }

    public String findShopSalesById(final long shopId) {
        String sales = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.shopSalesCount(shopId));
            }
        });
        if(Strings.isNullOrEmpty(sales)) {
            return "0";
        }
        return sales;
    }
    public void setShopEvaluation(final long shopId,final HashMap<String, String> hashMap) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hmset(RedisKeyUtils.shopEvaluation(shopId), hashMap);
            }
        });
    }

    //获取商铺评价
    public String getShopEvaluation(final long shopId,final String k1) {
        String result =  jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(RedisKeyUtils.shopEvaluation(shopId)).get(k1);
            }
        });
        return result;
    }
    //获取商铺评价MAP
    public HashMap<String, String> getShopEvaluationMap(final long shopId) {
        HashMap<String, String> resultMap = null;
        resultMap = (HashMap<String, String>) jedisTemplate.execute(new JedisTemplate.JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(RedisKeyUtils.shopEvaluation(shopId));
            }
        });
        return resultMap;
    }
    //获取商铺评价集合
    public List<String> getShopEvaluationList(final long shopId, final String... strings) {
        List<String> str = jedisTemplate.execute(new JedisTemplate.JedisAction<List<String>>() {
            @Override
            public List<String> action(Jedis jedis) {
                return jedis.hmget(RedisKeyUtils.shopEvaluation(shopId), strings);
            }
        });
        return str;
    }
}
