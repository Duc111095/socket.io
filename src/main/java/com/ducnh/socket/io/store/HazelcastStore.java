package com.ducnh.socket.io.store;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class HazelcastStore implements Store{

	private final IMap<String, Object> map;
	
	public HazelcastStore(UUID sessionId, HazelcastInstance hazelcastInstance) {
		map = hazelcastInstance.getMap(sessionId.toString());
	}
	
	@Override
	public void set(String key, Object val) {
		map.put(key, val);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key) {
		return (T) map.get(key);
	}

	@Override
	public boolean has(String key) {
		return map.containsKey(key);
	}

	@Override
	public void del(String key) {
		map.delete(key);
	}
}
