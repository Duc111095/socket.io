package com.ducnh.socket.io.store;

import java.util.Map;
import java.util.UUID;

import com.ducnh.socket.io.store.pubsub.BaseStoreFactory;
import com.ducnh.socket.io.store.pubsub.PubSubStore;

import io.netty.util.internal.PlatformDependent;

public class MemoryStoreFactory extends BaseStoreFactory{

	private final MemoryPubSubStore pubSubMemoryStore = new MemoryPubSubStore();	
	
	@Override
	public <K, V> Map<K, V> createMap(String name) {
		return PlatformDependent.newConcurrentHashMap();
	}

	@Override
	public Store createStore(UUID sessionId) {
		return new MemoryStore();
	}

	@Override
	public void shutdown() {
	}

	@Override
	public PubSubStore pubSubStore() {
		return pubSubMemoryStore;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " (local session store only)";
	}

}
