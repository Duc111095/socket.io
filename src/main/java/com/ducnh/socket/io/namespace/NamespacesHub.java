package com.ducnh.socket.io.namespace;

import java.util.concurrent.ConcurrentMap;

import com.ducnh.socket.io.Configuration;
import com.ducnh.socket.io.SocketIONamespace;

import io.netty.util.internal.PlatformDependent;

public class NamespacesHub {

	private final ConcurrentMap<String, SocketIONamespace> namespaces = PlatformDependent.newConcurrentHashMap();
	private final Configuration configuration;
	
	public NamespacesHub(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public Namespace create(String name) {
		Namespace namespace = (Namespace) namespaces.get(name);
	}
}
