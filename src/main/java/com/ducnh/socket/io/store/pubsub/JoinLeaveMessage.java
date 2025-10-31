package com.ducnh.socket.io.store.pubsub;

import java.util.UUID;

public class JoinLeaveMessage extends PubSubMessage{

	private static final long serialVersionUID = -3554670598832471357L;

	private UUID sessionId;
	private String namespace;
	private String room;
	
	public JoinLeaveMessage() {
	
	}
	
	public JoinLeaveMessage(UUID id, String room, String namespace) {
		super();
		this.sessionId = id;
		this.room = room;
		this.namespace = namespace;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getRoom() {
		return room;
	}
	
	public UUID getSessionId() {
		return sessionId;
	}
}
