package com.ducnh.socket.io.store.pubsub;

public interface PubSubListener<T> {

	void onMessage(T data);
}
