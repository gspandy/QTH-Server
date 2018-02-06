package com.nowbook.restful.event;

import com.google.common.eventbus.AsyncEventBus;
import org.springframework.stereotype.Controller;

import java.util.concurrent.Executors;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-07-07 2:10 PM  <br>
 * Author:cheng
 */
@Controller
public class RestEventBus {
    private final AsyncEventBus eventBus;

    public RestEventBus() {
        this.eventBus = new AsyncEventBus(Executors.newFixedThreadPool(2));
    }

    public void register(Object object) {
        eventBus.register(object);
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    @SuppressWarnings("unused")
    public void unRegister(Object object) {
        eventBus.unregister(object);
    }

}
