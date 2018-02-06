/*
 * Copyright (c) 2013 大连锦霖科技有限公司
 */

package com.nowbook.web.controller.api.userEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-10
 */
@Component
public class LoginTokenRefresher {

    private final UserEventBus eventBus;

    @Autowired
    public LoginTokenRefresher(UserEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    /*@Subscribe
    public void deleteSessionId(LogoutEvent logoutEvent){
        AFSessionManager.instance().removeSessionIdCookie(logoutEvent.getResponse());
    }*/
}
