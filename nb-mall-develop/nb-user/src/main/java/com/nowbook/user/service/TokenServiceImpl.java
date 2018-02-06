package com.nowbook.user.service;

import com.nowbook.user.model.LoginInfo;
import com.nowbook.user.redis.RedisTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.nowbook.common.utils.Arguments.notNull;

/**
 * Created by robin on 17/8/4.
 */
@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    private RedisTokenDao redisTokenDao;
    /**
     * 保存用户登录信息到redis
     *
     * @param loginInfo 用户登录信息
     */
    public void saveRedisToken(LoginInfo loginInfo){
        //通过用户id获取该用户下的所有登录信息，删除pushDeviceId相同的其他登录信息
        long userId = Long.parseLong(loginInfo.getId());
        List<LoginInfo> infoList= redisTokenDao.findByUserId(userId);
        if(infoList.size()>0){
            for(LoginInfo info:infoList){
                //删除极光id相同，deviceId不同的其他登录信息（避免同一账户安卓卸载重装之后deviceId不同的问题）
                if(!info.getDeviceId().equals(loginInfo.getDeviceId())&&info.getPushDeviceId().equals(loginInfo.getPushDeviceId())&&info.getDeviceType().equals(loginInfo.getDeviceType())){
                    redisTokenDao.delete(info);
                }
            }
        }
        redisTokenDao.save(loginInfo);
    }
    /**
     * 获取登录信息从redis
     *
     * @param id 用户id
     * @param deviceId 设备id
     * @param deviceType  设备类型
     */
    public LoginInfo getRedisToken(Long id, String deviceId, int deviceType){
       return redisTokenDao.findById(id,deviceId,deviceType);
    }
    /**
     * 验证token的有效性
     *
     * @param tokenPayload 用户token
     * @param deviceId 设备id
     * @param deviceType  设备类型
     */
    public void verifyToken(String tokenPayload,long id,String deviceId,int deviceType){

        LoginInfo info=redisTokenDao.findById(id, deviceId, deviceType);
        checkArgument(notNull(info), "token.invalid");
        checkArgument(info.getToken().equals(tokenPayload), "token.invalid");


    }

    /**
     * 获取登录信息从redis
     *
     * @param id 用户id
     */
    public List<LoginInfo> getRedisTokenByUserId(Long id){
        return redisTokenDao.findByUserId(id);
    }

    /**
     * 从redis删除登录信息
     *
     */
    public void deleteToken(LoginInfo loginInfo){
       redisTokenDao.delete(loginInfo);

    }


}
