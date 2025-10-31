package com.ducnh.socket.io.transport;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.SocketIOClient;
import com.ducnh.socket.io.namespace.Namespace;
import com.ducnh.socket.io.protocol.EngineIOVersion;

public class NamespaceClient implements SocketIOClient{

	private static final Logger log = LoggerFactory.getLogger(NamespaceClient.class);

	private final AtomicBoolean disconnected = new AtomicBoolean();
	private final ClientHead baseClient;
	private final Namespace namespace;
	
	public NamespaceClient(ClientHead baseClient, Namespace namespace) {
		this.baseClient = baseClient;
		this.namespace = namespace;
		namespace.addClient(this);
	}
	
	public ClientHead getBaseClient() {
		return baseClient;
	}
	
	@Override
	public Transport getTransport() {
		return baseClient.getCurrentTransport();
	}
	
	@Override
	public EngineIOVersion getEngineIOVersion() {
		return baseClient.getCurrentTransport();
	}
	
	@Override
	public boolean isChannelOpen() {
		return baseClient.isChannelOpen();
	}
}
