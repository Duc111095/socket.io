package com.ducnh.socket.io.store;

import com.ducnh.socket.io.store.pubsub.PubSubListener;
import com.ducnh.socket.io.store.pubsub.PubSubMessage;
import com.ducnh.socket.io.store.pubsub.PubSubStore;
import com.ducnh.socket.io.store.pubsub.PubSubType;

public class MemoryPubSubStore implements PubSubStore{

	@Override
	public void publish(PubSubType type, PubSubMessage msg) {
		
	}

	@Override
	public <T extends PubSubMessage> void subscribe(PubSubType type, PubSubListener<T> listener, Class<T> clazz) {
		
	}

	@Override
	public void unsubscribe(PubSubType type) {
		
	}

	@Override
	public void shutdown() {
		
	}
}
