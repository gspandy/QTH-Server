/*
 * Copyright (c) 2013 大连锦霖科技有限公司
 */

package com.nowbook.web.controller.api.userEvent;

import com.google.common.base.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-10
 */
public abstract class SessionEvent {
    protected final long userId;
    protected final HttpServletRequest request;
    protected final HttpServletResponse response;

    public SessionEvent(long userId, HttpServletRequest request, HttpServletResponse response) {
        this.response = response;
        this.userId = userId;
        this.request = request;
    }

    public long getUserId() {
        return userId;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public String getCookie(String name) {
        for (Cookie cookie : request.getCookies()) {
            if (Objects.equal(cookie.getName(), name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
