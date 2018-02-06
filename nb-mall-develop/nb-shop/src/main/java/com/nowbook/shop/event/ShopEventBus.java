package com.nowbook.shop.event;

import com.google.common.eventbus.AsyncEventBus;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-07 5:02 PM  <br>
 * Author:cheng
 */
@Component
public class ShopEventBus {


    private final AsyncEventBus eventBus;

    public ShopEventBus() {
        this.eventBus = new AsyncEventBus(Executors.newFixedThreadPool(4));
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
