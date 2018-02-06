package com.nowbook.admin.service;

import com.nowbook.common.model.Response;
import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.rlt.coupon.model.Coupon;
import com.nowbook.rlt.coupon.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Effet on 4/26/14.
 */
@Slf4j
@Service
public class AppPictureService {

    @Autowired
    public JedisTemplate jedisTemplate;


    // 启动页图片获取
    public String pictureStartUpGet() {
        String imageUrl = "";
        String startUp = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get("picture_start_up");
            }
        });
        if(startUp !=null && !startUp.equals("")){
            imageUrl = startUp.toString();
        }
        return imageUrl;
    }

    // 引导页图片获取
    public Map<String,String> pictureGuideGet() {
        Map<String,String> imageUrl = new HashMap<String, String>();
        String startUp = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get("picture_guide");
            }
        });
        if(startUp !=null && !startUp.equals("")){
            Integer i = 1;
            for(String image : startUp.split(",")){
                imageUrl.put("imageUrl"+i,image);
                i++;
            }
        }
        return imageUrl;
    }
}
