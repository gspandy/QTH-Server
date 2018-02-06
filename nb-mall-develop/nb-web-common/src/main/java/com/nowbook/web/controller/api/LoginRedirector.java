package com.nowbook.web.controller.api;

import com.nowbook.user.base.BaseUser;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2014-01-02
 */
public interface LoginRedirector {

    /**
     * 生成登陆后待跳转的url地址
     * @param target  如果指定了target,则直接返回target
     * @param baseUser  用户
     * @return 待跳转的url地址
     */
    String redirectTarget(String target, BaseUser baseUser);
}
