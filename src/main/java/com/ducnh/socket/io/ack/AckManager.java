package com.ducnh.socket.io.ack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.AckCallback;
import com.ducnh.socket.io.MultiTypeAckCallback;
import com.ducnh.socket.io.MultiTypeArgs;
import com.ducnh.socket.io.SocketIOClient;
import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.scheduler.CancelableScheduler;
import com.ducnh.socket.io.scheduler.SchedulerKey.Type;
import com.ducnh.socket.io.scheduler.SchedulerKey;

import io.netty.util.internal.PlatformDependent;

public class AckManager {

	static class AckEntry {
		final Map<Long, AckCallback<?>> ackCallbacks = PlatformDependent.newConcurrentHashMap();
		final AtomicLong ackIndex = new AtomicLong(-1);
		
		public long addAckCallback(AckCallback<?> callback) {
			long index = ackIndex.incrementAndGet();
			ackCallbacks.put(index, callback);
			return index;
		}
		
		public Set<Long> getAckIndexes() {
			return ackCallbacks.keySet();
		}
		
		public AckCallback<?> getCallback(Long index) {
			return ackCallbacks.get(index);
		}
		
		public AckCallback<?> removeCallback(Long index) {
			return ackCallbacks.remove(index);
		}
		
		public void initAckIndex(long index) {
			ackIndex.compareAndSet(-1, index);
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(AckManager.class);

	private final ConcurrentMap<UUID, AckEntry> ackEntries = PlatformDependent.newConcurrentHashMap();
	
	private final CancelableScheduler scheduler;

	public AckManager(CancelableScheduler scheduler) {
		super();
		this.scheduler = scheduler;
	}
	
	public void initAckIndex(UUID sessionId, long index) {
		AckEntry ackEntry = getAckEntry(sessionId);
		ackEntry.initAckIndex(index);
	}
	
	private AckEntry getAckEntry(UUID sessionId) {
		AckEntry ackEntry = ackEntries.get(sessionId);
		if (ackEntry == null) {
			ackEntry = new AckEntry();
			AckEntry oldAckEntry = ackEntries.putIfAbsent(sessionId, ackEntry);
			if (oldAckEntry != null) {
				ackEntry = oldAckEntry;
			}
		}
		return ackEntry;
	}
	
	@SuppressWarnings("unchecked")
	public void onAck(SocketIOClient client, Packet packet) {
		AckSchedulerKey key = new AckSchedulerKey(Type.ACK_TIMEOUT, client.getSessionId()hashCode(), packet.getAckId());
		scheduler.cancel(key);
		
		AckCallback callback = removeCallback(client.getSessionId(), packet.getAckId());
		if (callback == null) {
			return;
		}
		if (callback instanceof MultiTypeAckCallback) {
			callback.onSuccess(new MultiTypeArgs(packet.<List<Object>>getData()));
		} else {
			Object param = null;
			List<Object> args = packet.getData();
			if (!args.isEmpty()) {
				param = args.get(0);
			}
			if (args.size() > 1) {
				log.error("Wrong ack args amount. Should be only one argument, but current amount is: {}. Ack id: {}, sessionId: {}",
						args.size(), packet.getAckId(), client.getSessionId());
			}
			callback.onSuccess(param);
		}
	}
	
	private AckCallback<?> removeCallback(UUID sessionId, long index) {
		AckEntry ackEntry = ackEntries.get(sessionId);
		
		if (ackEntry != null) {
			return ackEntry.removeCallback(index);
		}
		return null;
	}
	
	public AckCallback<?> getCallback(UUID sessionId, long index) {
		AckEntry ackEntry = getAckEntry(sessionId);
		return ackEntry.getCallback(index);
	}
	
	public long registerAck(UUID sessionId, AckCallback<?> callback) {
		AckEntry ackEntry = getAckEntry(sessionId);
		ackEntry.initAckIndex(0);
		long index = ackEntry.addAckCallback(callback);
		
		if (log.isDebugEnabled()) {
			log.debug("Ack Callback registered with id: {} for client: {}", index, sessionId);
		}
		scheduleTimeout(index, sessionId, callback);
		return index;
	}
	
	private void scheduleTimeout(final long index, UUID sessionId, AckCallback<?> callback) {
		if (callback.getTimeout() == -1) {
			return;
		}
		SchedulerKey key = new AckSchedulerKey(Type.ACK_TIMEOUT, sessionId, index);
		scheduler.scheduleCallback(key, new Runnable() {
			@Override
			public void run() {
				AckCallback<?> cb = removeCallback(sessionId, index);
				if (cb != null) {
					cb.onTimeout();
				}
			}
		}, callback.getTimeout(), TimeUnit.SECONDS);
	}
	
	@Override
	public void onDisconnect(ClientHead client) {
		AckEntry e = ackEntries.remove(client.getSessionId());
		if (e == null) {
			return;
		}
		Set<Long> indexes = e.getAckIndexes();
		
		for (Long index : indexes) {
			AckCallback<?> callback = e.getAckCallback(index);
			if (callback != null) {
				callback.onTimeout();
			}
			SchedulerKey key = new AckSchedulerKey(Type.ACK_TIMEOUT, client.getSessionId(), index);
			scheduler.cancel(key);
		}
	}
}
