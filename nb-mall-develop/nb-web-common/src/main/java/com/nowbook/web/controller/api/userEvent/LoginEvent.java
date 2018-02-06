/*
 * Copyright (c) 2013 大连锦霖科技有限公司
 */

package com.nowbook.web.controller.api.userEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-10
 */
public class LoginEvent extends SessionEvent {

    public LoginEvent(long userId, HttpServletRequest request, HttpServletResponse response) {
        super(userId, request, response);
    }

}
