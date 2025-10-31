package com.ducnh.socket.io.namespace;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ducnh.socket.io.listener.DataListener;

public class EventEntry<T> {

	private final Queue<DataListener<T>> listeners = new ConcurrentLinkedQueue<DataListener<T>>();
	
	public EventEntry() {
		
	}
	
	public void addListener(DataListener<T> listener) {
		listeners.add(listener);
	}
	
	public Queue<DataListener<T>> getListeners() {
		return listeners;
	}
}
