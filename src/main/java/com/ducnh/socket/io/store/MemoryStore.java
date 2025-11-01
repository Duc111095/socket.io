package com.ducnh.socket.io.store;

import java.util.Map;

import io.netty.util.internal.PlatformDependent;

public class MemoryStore implements Store{
	
	private final Map<String, Object> store = PlatformDependent.newConcurrentHashMap();
	
	@Override
	public void set(String key, Object val) {
		store.put(key, val);
	}

	@Override
	public <T> T get(String key) {
		return (T) store.get(key);
	}

	@Override
	public boolean has(String key) {
		return store.containsKey(key);
	}

	@Override
	public void del(String key) {
		store.remove(key);
	}
}
