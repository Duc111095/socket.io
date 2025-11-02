package com.ducnh.socket.io.messages;

import com.ducnh.socket.io.Transport;
import com.ducnh.socket.io.handler.ClientHead;

import io.netty.buffer.ByteBuf;

public class PacketsMessage {

	private final ClientHead client;
	private final ByteBuf content;
	private final Transport transport;
	
	public PacketsMessage(ClientHead client, ByteBuf content, Transport transport) {
		this.client = client;
		this.content = content;
		this.transport = transport;
	}
	
	public Transport getTransport() {
		return transport;
	}
	
	public ClientHead getClient() {
		return client;
	}
	
	public ByteBuf getContent() {
		return content;
	}
}
