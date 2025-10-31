package com.ducnh.socket.io.store.pubsub;

import com.ducnh.socket.io.protocol.Packet;

public class DispatchMessage extends PubSubMessage {

	private static final long serialVersionUID = -5254146754283040012L;

	private String room;
	private String namespace;
	private Packet packet;
	
	public DispatchMessage() {
		
	}
	
	public DispatchMessage(String room, String namespace, Packet packet) {
		this.room = room;
		this.namespace = namespace;
		this.packet = packet;
	}
	
	public String getRoom() {
		return this.room;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public Packet getPacket() {
		return packet;
	}
}
