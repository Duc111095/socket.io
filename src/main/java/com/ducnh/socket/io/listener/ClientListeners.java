package com.ducnh.socket.io.listener;

public interface ClientListeners {

	void addMultiTypeEventListener(String eventName, MultiTypeEventListener listener, Class<?>...eventClasses);
	
	<T> void addEventListener(String eventName, Class<T> eventClass, DataListener<T> listener);
	
	void addEventInterceptor(EventInterceptor eventInterceptor);
	
	void addDisconnectListener(DisconnectListener listener);
	
	void addConnectListener(ConnectListener listener);
	
	void addPingListener(PingListener listener);
	
	void addPongListener(PongListener listener);
	
	void addListeners(Object listeners);
	
	<L> void addListeners(Iterable<L> listeners);
	
	void addListeners(Object listeners, Class<?> listenersClass);
	
	void removeAllListeners(String eventName);
}
