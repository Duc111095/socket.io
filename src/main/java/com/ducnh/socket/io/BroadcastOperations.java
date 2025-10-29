package com.ducnh.socket.io;

import java.util.Collection;
import java.util.function.Predicate;

import com.ducnh.socket.io.protocol.Packet;

public interface BroadcastOperations extends ClientOperations{

	Collection<SocketIOClient> getClients();
	
	<T> void send(Packet packet, BroadcastAckCallback<T> ackCallback);
	
	void sendEvent(String name, SocketIOClient excludedClient, Object... data);
	
	void sendEvent(String name, Predicate<SocketIOClient> excludePredicate, Object... data);
	
	<T> void sendEvent(String name, Object data, BroadcastAckCallback<T> ackCallback);
	
	<T> void sendEvent(String name, Object data, SocketIOClient excludedClient, BroadcastAckCallback<T> ackCallback);
	
	<T> void sendEvent(String name, Object data, Predicate<SocketIOClient> excludePredicate, BroadcastAckCallback<T> ackCallback);
	
}
