package com.nowbook.web.controller.api.userEvent;

import com.google.common.eventbus.AsyncEventBus;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component
public class PushOrderEventBus {
	private final AsyncEventBus eventBus;

	public PushOrderEventBus() {
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
