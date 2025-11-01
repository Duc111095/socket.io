package com.ducnh.socket.io.handler;

import java.util.Map;
import java.util.UUID;

import com.ducnh.socket.io.HandshakeData;

import io.netty.channel.Channel;
import io.netty.util.internal.PlatformDependent;

public class ClientsBox {
	private final Map<UUID, ClientHead> uuid2clients = PlatformDependent.newConcurrentHashMap();
	private final Map<Channel, ClientHead> channel2clients = PlatformDependent.newConcurrentHashMap();
	
	public HandshakeData getHandshakeData(UUID sessionId) {
		ClientHead client = uuid2clients.get(sessionId);
		if (client == null) {
			return null;
		}
		return client.getHandshakeData();
	}
	
	public void addClient(ClientHead clientHead) {
		uuid2clients.put(clientHead.getSessionId(), clientHead);
	}
	
	public ClientHead removeClient(UUID sessionId) {
		return uuid2clients.remove(sessionId);
	}
	
	public ClientHead get(UUID sessionId) {
		return uuid2clients.get(sessionId);
	}
	
	public void add(Channel channel, ClientHead clientHead) {
		channel2clients.put(channel, clientHead);
	}
	
	public void remove(Channel channel) {
		channel2clients.remove(channel);
	}
	
	public ClientHead get(Channel channel) {
		return channel2clients.get(channel);
	}
}
