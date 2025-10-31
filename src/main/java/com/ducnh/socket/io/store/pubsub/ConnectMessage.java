package com.ducnh.socket.io.store.pubsub;

import java.util.UUID;

public class ConnectMessage extends PubSubMessage{

	private static final long serialVersionUID = 1L;

	private UUID sessionId;
	
	public ConnectMessage() {
		
	}
	
	public ConnectMessage(UUID sessionId) {
		this.sessionId = sessionId;
	}
	
	public UUID getSessionId() {
		return sessionId;
	}
}
