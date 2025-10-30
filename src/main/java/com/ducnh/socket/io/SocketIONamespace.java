package com.ducnh.socket.io;

import java.util.Collection;
import java.util.UUID;

import com.ducnh.socket.io.listener.ClientListeners;

public interface SocketIONamespace extends ClientListeners{

	String getName();
	
	BroadcastOperations getBroadcastOperations();
	
	BroadcastOperations getRoomOperations(String room);
	
	BroadcastOperations getRoomOperations(String... rooms);
	
	Collection<SocketIOClient> getAllClients();
	
	SocketIOClient getClient(UUID uuid);
	
	void addAuthTokenListener(AuthTokenListener listener);
}
