package com.nowbook.item.dao.redis;

import com.nowbook.redis.utils.JedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import com.nowbook.user.util.RedisKeyUtils;
/**
 * Created by yangzefeng on 14-5-22
 */
@Repository
public class ItemDetaiImageDao {

    private final JedisTemplate jedisTemplate;

    @Autowired
    public ItemDetaiImageDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    //模板详细页图片地址存储
    public void setSpuIdEvaluation(final long spuId, final String strings) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.set(RedisKeyUtils.spuIdEvaluation(spuId), strings);
            }
        });
    }
    public String getSpuIdEvaluation(final long spuId) {
        return   jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(RedisKeyUtils.spuIdEvaluation(spuId));
            }
        });
    }
    //详细页图片地址存储
    public void setItemIdEvaluation(final long itemId, final String strings) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.set(RedisKeyUtils.itemIdEvaluation(itemId), strings);
            }
        });
    }
    public String getItemIdEvaluation(final long itemId) {
        return   jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
               return jedis.get(RedisKeyUtils.itemIdEvaluation(itemId));
            }
        });
    }
}
