package com.ducnh.socket.io.store;

import java.util.Map;
import java.util.UUID;

import com.ducnh.socket.io.Disconnectable;
import com.ducnh.socket.io.namespace.NamespacesHub;
import com.ducnh.socket.io.protocol.JsonSupport;
import com.ducnh.socket.io.store.pubsub.PubSubStore;

public interface StoreFactory extends Disconnectable{
	
	PubSubStore pubSubStore();
	 
	<K, V> Map<K, V> createMap(String name);
	
	Store createStore(UUID sessionId);
	
	void init(NamespacesHub namespacesHub, AuthorizeHandler authorizeHandler, JsonSupport jsonSupport);
	
	void shutdown();
}
