package com.ducnh.socket.io.store.pubsub;

public enum PubSubType {

	CONNECT, DISCONNECT, JOIN, BULK_JOIN, LEAVE, BULK_LEAVE, DISPATCH;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
