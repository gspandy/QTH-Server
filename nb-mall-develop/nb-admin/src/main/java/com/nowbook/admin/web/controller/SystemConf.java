package com.nowbook.admin.web.controller;

import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.sdp.model.DistributorSet;
import com.nowbook.sdp.model.Level;
import com.nowbook.sdp.service.LevelService;
import com.nowbook.sdp.service.SystemConfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Romo on 2017/8/21.
 */

@Controller
@Slf4j
@RequestMapping("/api/systemConf")
public class SystemConf {

    @Autowired
    protected JedisTemplate template;

    @Autowired
    protected SystemConfService systemConfService;

    @Autowired
    protected LevelService levelService;

    // 添加返润周期设置
    @RequestMapping(value = "/profitDateNew", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<String> profitDateNew() {
        List<String> result = new ArrayList<String>();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(SystemConf.class.getClassLoader().getResourceAsStream("profitConf.properties"), "UTF-8");
            Properties properties = new Properties();
            properties.load(inputStreamReader);
            Set<Object> keys = properties.keySet();
            for (Object key : keys) {
                result.add(properties.getProperty(key.toString()));
            }
        } catch (IOException e) {
            log.error("配置文件读取失败", e);
        }
        return result;
    }

    // 更新返润周期设置
    @RequestMapping(value = "/profitDateSet", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String profitDateSet(DistributorSet distributorSet) {
        String value = distributorSet.getValue();
        try {
            Double.valueOf(value);
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(SystemConf.class.getClassLoader().getResourceAsStream("profitConf.properties"), "UTF-8");
                Properties properties = new Properties();
                properties.load(inputStreamReader);
                Set<Object> keys = properties.keySet();
                for (Object key : keys) {
                    if (distributorSet.getKey().equals(properties.getProperty(key.toString()))) {
                        distributorSet.setKey(key.toString());
                    }
                }
                Map<String, String> map = new HashMap<String, String>();
                map.put(distributorSet.getKey(), distributorSet.getValue());
                systemConfService.updateProfitDate(map);
            } catch (IOException ee) {
                log.error("无法在配置文件中根据value找到key", ee);
            }
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }

    // 更新会员参数设置
    @RequestMapping(value = "/memberSet", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void memberSet(Level level) {
        levelService.updateById(level);
    }

    // 添加其他设置
    @RequestMapping(value = "/otherNew", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<String> otherNew() {
        List<String> result = new ArrayList<String>();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(SystemConf.class.getClassLoader().getResourceAsStream("otherConf.properties"), "UTF-8");
            Properties properties = new Properties();
            properties.load(inputStreamReader);
            Set<Object> keys = properties.keySet();
            for (Object key : keys) {
                result.add(properties.getProperty(key.toString()));
            }
        } catch (IOException e) {
            log.error("配置文件读取失败", e);
        }
        return result;
    }

    // 更新其他设置
    @RequestMapping(value = "/otherSet", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void otherSet(DistributorSet distributorSet) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(SystemConf.class.getClassLoader().getResourceAsStream("otherConf.properties"), "UTF-8");
            Properties properties = new Properties();
            properties.load(inputStreamReader);
            Set<Object> keys = properties.keySet();
            for (Object key : keys) {
                if (distributorSet.getKey().equals(properties.getProperty(key.toString()))) {
                    distributorSet.setKey(key.toString());
                }
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put(distributorSet.getKey(), distributorSet.getValue());
            systemConfService.updateOthers(map);
        } catch (IOException e) {
            log.error("无法在配置文件中根据value找到key", e);
        }
    }

}
