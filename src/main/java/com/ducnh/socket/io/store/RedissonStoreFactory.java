package com.ducnh.socket.io.store;

import java.util.Map;
import java.util.UUID;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import com.ducnh.socket.io.namespace.NamespacesHub;
import com.ducnh.socket.io.protocol.JsonSupport;
import com.ducnh.socket.io.store.pubsub.BaseStoreFactory;
import com.ducnh.socket.io.store.pubsub.PubSubStore;

public class RedissonStoreFactory extends BaseStoreFactory {

	private final RedissonClient redisClient;
	private final RedissonClient redisPub;
	private final RedissonClient redisSub;
	
	private final PubSubStore pubSubStore;
	
	public RedissonStoreFactory() {
		this(Redisson.create());
	}
	
	public RedissonStoreFactory(RedissonClient redisson) {
		this.redisClient = redisson;
		this.redisPub = redisson;
		this.redisSub = redisson;
		
		this.pubSubStore = new RedissonPubSubStore(redisPub, redisSub, getNodeId());
	}
	
	public RedissonStoreFactory(Redisson redisClient, Redisson redisPub, Redisson redisSub) {
		this.redisClient = redisClient;
		this.redisPub = redisPub;
		this.redisSub = redisSub;
		
		this.pubSubStore = new RedissonPubSubStore(redisPub, redisSub, getNodeId());
	}
	
	@Override
	public <K, V> Map<K, V> createMap(String name) {
		return redisClient.getMap(name);
	}

	@Override
	public Store createStore(UUID sessionId) {
		return new RedissonStore(sessionId, redisClient);
	}

	@Override
	public void shutdown() {
		this.redisClient.shutdown();
		this.redisPub.shutdown();
		this.redisSub.shutdown();
	}

	@Override
	public PubSubStore pubSubStore() {
		return pubSubStore;
	}

}
