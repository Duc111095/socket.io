package com.ducnh.socket.io.store;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import com.ducnh.socket.io.store.pubsub.PubSubListener;
import com.ducnh.socket.io.store.pubsub.PubSubMessage;
import com.ducnh.socket.io.store.pubsub.PubSubStore;
import com.ducnh.socket.io.store.pubsub.PubSubType;

import io.netty.util.internal.PlatformDependent;

public class RedissonPubSubStore implements PubSubStore {

	private final RedissonClient redissonPub;
	private final RedissonClient redissonSub;
	private Long nodeId;
	
	private final ConcurrentMap<String, Queue<Integer>> map = PlatformDependent.newConcurrentHashMap();

	public RedissonPubSubStore(RedissonClient redissonPub, RedissonClient redissonSub, Long nodeId) {
		this.redissonPub = redissonPub;
		this.redissonSub = redissonSub;
		this.nodeId = nodeId;
	}
	
	@Override
	public void publish(PubSubType type, PubSubMessage msg) {
		msg.setNodeId(nodeId);
		redissonPub.getTopic(type.toString()).publish(msg);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <T extends PubSubMessage> void subscribe(PubSubType type, PubSubListener<T> listener, Class<T> clazz) {
		String name = type.toString();
		RTopic topic = redissonSub.getTopic(name);
		int regId = topic.addListener(PubSubMessage.class, new MessageListener<PubSubMessage>() {
			@Override
			public void onMessage(CharSequence channel, PubSubMessage msg) {
				if (!nodeId.equals(msg.getNodeId())) {
					listener.onMessage((T) msg);
				}
			}
		});
		
		Queue<Integer> list = map.get(name);
		if (list == null) {
			list = new ConcurrentLinkedQueue<Integer>();
			Queue<Integer> oldList = map.putIfAbsent(name, list);
			if (oldList != null) {
				list = oldList;
			}
		}
		list.add(regId);
	}

	@Override
	public void unsubscribe(PubSubType type) {
		String name = type.toString();
		Queue<Integer> regIds = map.remove(name);
		RTopic topic = redissonSub.getTopic(name);
		for (Integer id : regIds) {
			topic.removeListener(id);
		}
	}

	@Override
	public void shutdown() {
		
	}
	
	
} 
