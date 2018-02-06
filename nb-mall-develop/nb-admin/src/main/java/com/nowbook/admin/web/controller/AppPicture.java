package com.nowbook.admin.web.controller;

import com.nowbook.redis.utils.JedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

/**
 * Created by Administrator on 2017/10/16.
 */
@Controller
@RequestMapping("/api/admin/appPicture")
public class AppPicture {
    private final static Logger log = LoggerFactory.getLogger(AppPicture.class);
    @Autowired
    public JedisTemplate jedisTemplate;

    // 启动页图片设置
    @RequestMapping(value = "/pictureStartUpSet", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String pictureStartUpSet(final String imageUrl) {
        if (imageUrl != null) {
            jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
                @Override
                public void action(Jedis jedis) {
                    jedis.set("picture_start_up", imageUrl.toString());
                }
            });
            return imageUrl;
        } else {
            log.error("app启动页设置失败，没有此图片");
        }
        return "fail";
    }


    // 引导页图片设置
    @RequestMapping(value = "/pictureGuideSet", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String pictureGuideSet(final String imageUrl,String image) {
        if (imageUrl != null) {
            jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
                @Override
                public void action(Jedis jedis) {
                    jedis.set("picture_guide", imageUrl.toString());
                }
            });
            return image;
        } else {
            log.error("app引导页设置失败，没有此图片");
        }
        return "fail";
    }
}
