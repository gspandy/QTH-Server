package com.nowbook.user.service;

import com.nowbook.user.model.LoginInfo;

import java.util.List;

/**
 * Created by robin on 17/8/4.
 */
public interface TokenService {
    /**
     * 保存用户登录信息到redis
     *
     * @param loginInfo 用户登录信息
     */
    void saveRedisToken(LoginInfo loginInfo);
    /**
     * 获取登录信息从redis
     *
     * @param id 用户id
     * @param deviceId 设备id
     * @param deviceType  设备类型
     */
    LoginInfo getRedisToken(Long id,String deviceId,int deviceType);
    /**
     * 验证token的有效性
     *
     * @param tokenPayload 用户token
     * @param deviceId 设备id
     * @param deviceType  设备类型
     */
    void verifyToken(String tokenPayload,long id,String deviceId,int deviceType);

    /**
     * 获取登录信息从redis
     *
     * @param id 用户id
     */
    List<LoginInfo> getRedisTokenByUserId(Long id);

    /**
     * 从redis删除登录信息
     *
     */
    void deleteToken(LoginInfo loginInfo);
}
