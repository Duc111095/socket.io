package com.ducnh.socket.io.store.pubsub;

import java.util.UUID;

public class DisconnectMessage extends PubSubMessage{

	private static final long serialVersionUID = -2724581974198805581L;

	private UUID sessionId;
	
	public DisconnectMessage() {
		
	}
	
	public DisconnectMessage(UUID sessionId) {
		super();
		this.sessionId = sessionId;
	}
	
	public UUID getSessionId() {
		return sessionId;
	}
}
