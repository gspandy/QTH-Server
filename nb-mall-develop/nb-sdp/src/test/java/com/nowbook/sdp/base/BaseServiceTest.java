package com.nowbook.sdp.base;/*
 * Copyright (c) 2012 大连锦霖科技有限公司
 */

import com.nowbook.redis.utils.JedisTemplate;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2012-08-29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/redis-dao-context-test.xml",
        "classpath:spring/mysql-dao-context-test.xml",
        "classpath:spring/sdp-service-context.xml"
})
public abstract class BaseServiceTest {
    @Autowired
    protected JedisTemplate template;


}
