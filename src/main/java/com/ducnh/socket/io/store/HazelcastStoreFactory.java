package com.ducnh.socket.io.store;

import java.util.Map;
import java.util.UUID;

import com.ducnh.socket.io.store.pubsub.BaseStoreFactory;
import com.ducnh.socket.io.store.pubsub.PubSubStore;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastStoreFactory extends BaseStoreFactory{

	private final HazelcastInstance hazelcastClient;
	private final HazelcastInstance hazelcastPub;
	private final HazelcastInstance hazelcastSub;
	
	private final PubSubStore pubSubStore;
	
	public HazelcastStoreFactory() {
		this(HazelcastClient.newHazelcastClient());
	}
	
	public HazelcastStoreFactory(HazelcastInstance instance) {
		this.hazelcastClient = instance;
		this.hazelcastSub = instance;
		this.hazelcastPub = instance;
		
		this.pubSubStore = new HazelcastPubSubStore(hazelcastPub, hazelcastSub, getNodeId());
	}
	
	public HazelcastStoreFactory(HazelcastInstance hazelcastClient, HazelcastInstance hazelcastPub, HazelcastInstance hazelcastSub) {
		this.hazelcastClient = hazelcastClient;
		this.hazelcastPub = hazelcastPub;
		this.hazelcastSub = hazelcastSub;
		
		this.pubSubStore = new HazelcastPubSubStore(hazelcastPub, hazelcastSub, getNodeId());
	}
	
	@Override
	public Store createStore(UUID sessionId) {
		return new HazelcastStore(sessionId, hazelcastClient);
	}
	
	@Override
	public PubSubStore pubSubStore() {
		return pubSubStore;
	}

	@Override
	public void shutdown() {
		hazelcastClient.shutdown();
		hazelcastPub.shutdown();
		hazelcastSub.shutdown();
	}
	
	@Override
	public <K, V> Map<K, V> createMap(String name) {
		return hazelcastClient.getMap(name);
	}
}
