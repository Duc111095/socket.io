package com.ducnh.socket.io.store;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.ducnh.socket.io.store.pubsub.PubSubListener;
import com.ducnh.socket.io.store.pubsub.PubSubMessage;
import com.ducnh.socket.io.store.pubsub.PubSubStore;
import com.ducnh.socket.io.store.pubsub.PubSubType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import io.netty.util.internal.PlatformDependent;

public class HazelcastPubSubStore implements PubSubStore{
	
	private final HazelcastInstance hazelcastPub;
	private final HazelcastInstance hazelcastSub;
	private final Long nodeId;
	
	private final ConcurrentMap<String, Queue<String>> map = PlatformDependent.newConcurrentHashMap();
	
	public HazelcastPubSubStore(HazelcastInstance hazelcastPub, HazelcastInstance hazelcastSub, Long nodeId) {
		this.hazelcastPub = hazelcastPub;
		this.hazelcastSub = hazelcastSub;
		this.nodeId = nodeId;
	}
	
	@Override
	public void publish(PubSubType type, PubSubMessage msg) {
		msg.setNodeId(nodeId);
		hazelcastPub.getTopic(type.toString()).publish(msg);
	}

	@Override
	public <T extends PubSubMessage> void subscribe(PubSubType type, PubSubListener<T> listener, Class<T> clazz) {
		String name = type.toString();
		ITopic<T> topic = hazelcastSub.getTopic(name);
		String regId = topic.addMessageListener(new MessageListener<T>() {
			@Override
			public void onMessage(Message<T> message) {
				PubSubMessage msg = message.getMessageObject();
				if (!nodeId.equals(msg.getNodeId())) {
					listener.onMessage(message.getMessageObject());
				}
			} 
		});
		
		Queue<String> list = map.get(name);
		if (list == null) {
			list = new ConcurrentLinkedQueue<String>();
			Queue<String> oldList = map.putIfAbsent(name, list);
			if (oldList != null) {
				list = oldList;
			}
		}
		list.add(regId);
		
	}

	@Override
	public void unsubscribe(PubSubType type) {
		String name = type.toString();
		Queue<String> regIds = map.remove(name);
		ITopic<Object> topic = hazelcastSub.getTopic(name);
		for (String id : regIds) {
			topic.removeMessageListener(id);
		}
	}

	@Override
	public void shutdown() {
		
	}
	
}
