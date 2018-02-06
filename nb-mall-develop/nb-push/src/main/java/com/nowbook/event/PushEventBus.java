package com.nowbook.event;

import com.google.common.eventbus.AsyncEventBus;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component
public class PushEventBus {
	private final AsyncEventBus eventBus;

	public PushEventBus() {
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
