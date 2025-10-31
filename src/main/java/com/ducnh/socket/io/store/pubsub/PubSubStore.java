package com.ducnh.socket.io.store.pubsub;

public interface PubSubStore {

	void publish(PubSubType type, PubSubMessage msg);
	
	<T extends PubSubMessage> void subscribe(PubSubType type, PubSubListener<T> listener, Class<T> clazz);
	
	void unsubscribe(PubSubType type);
	
	void shutdown();
}
