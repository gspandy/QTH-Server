package com.nowbook.user.redis;

import com.nowbook.common.utils.JsonMapper;
import com.nowbook.redis.dao.RedisBaseDao;
import com.nowbook.redis.utils.JedisTemplate;
import com.nowbook.user.model.LoginInfo;
import com.nowbook.user.util.RedisKeyUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.*;

/**
 * Author:  <a href="mailto:robin@nowbook.com">jl</a>
 * Date: 2017-08-04
 */
@Repository
public class RedisTokenDao extends RedisBaseDao<LoginInfo> {

    private static final JsonMapper mapper = JsonMapper.nonEmptyMapper();

    @Autowired
    public RedisTokenDao(JedisTemplate template) {
        super(template);
    }
    /**
     * 保存用户登录信息到redis
     *
     * @param loginInfo 用户登录信息
     */
    public void save(final LoginInfo loginInfo) {

        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
//                Transaction t = jedis.multi();
//                //添加token
//                t.hmset(getKey(loginInfo), stringHashMapper.toHash(loginInfo));
//                t.exec();
                Transaction t = jedis.multi();
                //添加token
                Map<String,String> map=new HashMap<String, String>();
                //key为redis的key,subKey是key下面所存的map里的key值，通过subKey找到map中该key值对应的value值
                String  subKey=getSubKey(loginInfo);
                String key=getKey2(loginInfo);
                JSONObject jsonObject= JSONObject.fromObject(loginInfo);
                map.put(subKey,jsonObject.toString());
                t.hmset(key, map);
                t.exec();
            }
        });
    }

    private String getKey(LoginInfo loginInfo){
        return RedisKeyUtils.apiToken(Long.parseLong(loginInfo.getId()),loginInfo.getDeviceId(), loginInfo.getDeviceType());
    }
    /**
     * 获取登录信息从redis
     *
     * @param loginInfo  登录信息
     */

    public LoginInfo findByLoginInfo(final LoginInfo loginInfo) {
        Map hash = (Map)this.template.execute(new JedisTemplate.JedisAction() {
            public Map<String, String> action(Jedis jedis) {
                Map<String, String> map=new HashMap<String, String>();
                //key为redis的key,subKey是key下面所存的map里的key值，通过subKey找到map中该key值对应的value值
                String  subKey=getSubKey(loginInfo);
                String key=getKey2(loginInfo);
                List<String> list = jedis.hmget(key, subKey);
                if(list.size()>0&&list.get(0)!=null){
                    map=toHashMap(list.get(0));
                }
                return map;
            }
        });

        return this.stringHashMapper.fromHash(hash);
    }
    /**
     * 获取登录信息从redis
     *
     * @param id 用户id
     * @param deviceId 设备id
     * @param deviceType  设备类型
     */
    public LoginInfo findById(final Long id,final String deviceId,final int deviceType) {
        Map hash = (Map)this.template.execute(new JedisTemplate.JedisAction() {
            public Map<String, String> action(Jedis jedis) {
                Map<String, String> map=new HashMap<String, String>();
                //key为redis的key,subKey是key下面所存的map里的key值，通过subKey找到map中该key值对应的value值
                String key=RedisKeyUtils.apiToken(id);
                String subKey=RedisKeyUtils.apiToken(deviceId,deviceType);
                List<String> list = jedis.hmget(key, subKey);
                if(list.size()>0&&list.get(0)!=null){
                    map=toHashMap(list.get(0));
                }
                return map;
            }
        });

        return this.stringHashMapper.fromHash(hash);
    }
    /**
     * 从redis删除登录信息
     *
     * @param loginInfo  登录信息
     */    public void delete(final LoginInfo loginInfo) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                //key为redis的key,subKey是key下面所存的map里的key值，通过subKey找到map中该key值对应的value值
                String  subKey=getSubKey(loginInfo);
                String key=getKey2(loginInfo);
                jedis.hdel(key,subKey);
            }
        });
    }


    /**
     *通过用户id获得登录的设备id
     *
     * @param id 用户id
     */
    public List<LoginInfo> findByUserId(final Long id){

        Map hash = (Map)this.template.execute(new JedisTemplate.JedisAction() {
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(RedisKeyUtils.apiToken(id));
            }
        });
        Collection values=hash.values();
        List<LoginInfo> list=new ArrayList<LoginInfo>();
        for (Object object : values) {
            Map<String, String> map=new HashMap<String, String>();
            map=toHashMap(object.toString());
            LoginInfo info=this.stringHashMapper.fromHash(map);
            list.add(info);
        }
        return list;
    }

    //获取key
    private static String getKey2(LoginInfo loginInfo){
        return RedisKeyUtils.apiToken(Long.parseLong(loginInfo.getId()));
    }

    //获取获取key下面的map的key
    private static String getSubKey(LoginInfo loginInfo){
        return RedisKeyUtils.apiToken(loginInfo.getDeviceId(), loginInfo.getDeviceType());
    }

    //将从redis上获取到的json转成map
    private static HashMap<String, String> toHashMap(Object object)
    {
        HashMap<String, String> data = new HashMap<String, String>();
        // 将json字符串转换成jsonObject
        JSONObject jsonObject = JSONObject.fromObject(object);
        Iterator it = jsonObject.keys();
        // 遍历jsonObject数据，添加到Map对象
        while (it.hasNext())
        {
            String key = String.valueOf(it.next());
            String value =jsonObject.get(key).toString();
            data.put(key, value);
        }
        return data;
    }






}
