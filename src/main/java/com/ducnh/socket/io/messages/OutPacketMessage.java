package com.ducnh.socket.io.messages;

import com.ducnh.socket.io.Transport;
import com.ducnh.socket.io.handler.ClientHead;

public class OutPacketMessage extends HttpMessage{
	
	private final ClientHead clientHead;
	private final Transport transport;
	
	public OutPacketMessage(ClientHead clientHead, Transport transport) {
		super(clientHead.getOrigin(), clientHead.getSessionId());
		
		this.clientHead = clientHead;
		this.transport = transport;
	}
	
	public ClientHead getClientHead() {
		return clientHead;
	}
	
	public Transport getTransport() {
		return transport;
	}
}
