package com.ducnh.socket.io;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BroadcastAckCallback<T> {

	final AtomicBoolean loopFinished = new AtomicBoolean();
	final AtomicInteger counter =  new AtomicInteger();
	final AtomicBoolean successExecuted = new AtomicBoolean();
	final Class<?> resultClass;
	final int timeout;
	
	public BroadcastAckCallback(Class<T> resultClass, int timeout) {
		this.resultClass = resultClass;
		this.timeout = timeout;
	}
	
	public BroadcastAckCallback(Class<T> resultClass) {
		this(resultClass, -1);
	}
	
	final AckCallback<T> createClientCallback(final SocketIOClient client) {
		counter.getAndIncrement();
		return new AckCallback<T>(resultClass, timeout) {
			@Override
			public void onSuccess(T result) {
				counter.getAndDecrement();
				onClientSuccess(client, result);
				executeSuccess();
			}
			
			@Override
			public void onTimeout() {
				onClientTimeout(client);
			}
		};
	}
	
	protected void onClientTimeout(SocketIOClient client) {
		
	}
	
	protected void onClientSuccess(SocketIOClient client, T result) {
		
	}
	
	protected void onAllSuccess() {
		
	}
	
	private void executeSuccess() {
		if (loopFinished.get()
			&& counter.get() == 0
				&& successExecuted.compareAndSet(false, true)) {
			onAllSuccess();
		}
	}
	
	void loopFinished() {
		loopFinished.set(true);
		executeSuccess();
	}
}
