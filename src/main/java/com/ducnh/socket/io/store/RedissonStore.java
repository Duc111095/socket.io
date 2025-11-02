package com.ducnh.socket.io.store;

import java.util.Map;
import java.util.UUID;

import org.redisson.api.RedissonClient;

public class RedissonStore implements Store{

	private final Map<String, Object> map;
	
	public RedissonStore(UUID sessionId, RedissonClient redisson) {
		this.map = redisson.getMap(sessionId.toString());
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
		map.remove(key);
	}

}
