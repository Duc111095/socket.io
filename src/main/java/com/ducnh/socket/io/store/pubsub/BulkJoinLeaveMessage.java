package com.ducnh.socket.io.store.pubsub;

import java.util.Set;
import java.util.UUID;

public class BulkJoinLeaveMessage extends PubSubMessage{

	private static final long serialVersionUID = -2243874574324514902L;

	private UUID sessionId;
	private String namespace;
	private Set<String> rooms;
	
	public BulkJoinLeaveMessage(UUID id, Set<String> rooms, String namespace) {
		super();
		this.sessionId = id;
		this.rooms = rooms;
		this.namespace = namespace;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public UUID getSessionId() {
		return sessionId;
	}
	 
	public Set<String> getRooms() {
		return rooms;
	}
}
