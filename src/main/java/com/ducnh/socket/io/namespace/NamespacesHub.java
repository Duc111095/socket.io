package com.ducnh.socket.io.namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.ducnh.socket.io.Configuration;
import com.ducnh.socket.io.SocketIOClient;
import com.ducnh.socket.io.SocketIONamespace;
import com.ducnh.socket.io.misc.CompositeIterable;

import io.netty.util.internal.PlatformDependent;

public class NamespacesHub {

	private final ConcurrentMap<String, SocketIONamespace> namespaces = PlatformDependent.newConcurrentHashMap();
	private final Configuration configuration;
	
	public NamespacesHub(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public Namespace create(String name) {
		Namespace namespace = (Namespace) namespaces.get(name);
		if (namespace == null) {
			namespace = new Namespace(name, configuration);
			Namespace oldNamespace = (Namespace) namespaces.putIfAbsent(name, namespace);
			if (oldNamespace != null) {
				namespace = oldNamespace;
			}
		}
		return namespace;
	}
	
	public Iterable<SocketIOClient> getRoomClient(String room) {
		List<Iterable<SocketIOClient>> allClients = new ArrayList<Iterable<SocketIOClient>>();
		for (SocketIONamespace namespace : namespaces.values()) {
			Iterable<SocketIOClient> clients = ((Namespace) namespace).getRoomClients(room);
			allClients.add(clients);
		}
		return new CompositeIterable<SocketIOClient>(allClients);
	}
	
	public Namespace get(String name) {
		return (Namespace) namespaces.get(name);
	}
	
	public void remove(String name) {
		SocketIONamespace namespace = namespaces.remove(name);
		if (namespace != null) {
			namespace.getBroadcastOperations().disconnect();
		}
	}
	
	public Collection<SocketIONamespace> getAllNamespaces() {
		return namespaces.values();
	}
}
