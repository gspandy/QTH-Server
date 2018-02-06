package com.nowbook.user.util;

import com.nowbook.user.model.LoginInfo;

/**
 * 从线程里获取登录信息
 * Created by robin on 17/8/7.
 */
public final class LoginInfoUtil {
    private static ThreadLocal<LoginInfo> login = new ThreadLocal();
    /**
     * 保存用户登录信息
     *@param loginInfo  登录信息
     */
    public static void putLoginInfo(LoginInfo loginInfo) {
        login.set(loginInfo);
    }
    /**
     * 获取用户登录信息
     *
     */
    public static LoginInfo getLoginInfo() {
        return (LoginInfo)login.get();
    }
    /**
     * 删除用户登录信息
     *
     */
    public static void removeLoginInfo() {
        login.remove();
    }

}
