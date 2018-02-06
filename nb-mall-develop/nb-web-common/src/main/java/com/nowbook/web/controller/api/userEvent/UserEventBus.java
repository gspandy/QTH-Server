/*
 * Copyright (c) 2013 大连锦霖科技有限公司
 */

package com.nowbook.web.controller.api.userEvent;

import com.google.common.eventbus.AsyncEventBus;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-03-10
 */
@Component
public class UserEventBus {

    private final AsyncEventBus eventBus;

    public UserEventBus() {
        this.eventBus = new AsyncEventBus(Executors.newFixedThreadPool(4));
    }


    public void register(Object object) {
        eventBus.register(object);
    }


    public void post(Object event) {
        eventBus.post(event);
    }


    public void unregister(Object object) {
        eventBus.unregister(object);
    }
}
