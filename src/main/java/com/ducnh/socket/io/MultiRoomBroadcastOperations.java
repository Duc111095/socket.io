package com.ducnh.socket.io;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.ducnh.socket.io.protocol.Packet;

public class MultiRoomBroadcastOperations implements BroadcastOperations{

	private final Collection<BroadcastOperations> broadcastOperations;
	
	public MultiRoomBroadcastOperations(Collection<BroadcastOperations> broadcastOperations) {
		this.broadcastOperations = broadcastOperations;
	}
	
	@Override
	public void send(Packet packet) {
		if (this.broadcastOperations == null || this.broadcastOperations.size() == 0) {
			return;
		}
		for (BroadcastOperations b : this.broadcastOperations) {
			b.send(packet);
		}
	}

	@Override
	public void disconnect() {
		if (this.broadcastOperations == null || this.broadcastOperations.size() == 0) {
			return;
		}
		for (BroadcastOperations b : this.broadcastOperations) {
			b.disconnect();
		}
	}

	@Override
	public void sendEvent(String name, Object... data) {
		if (this.broadcastOperations == null || this.broadcastOperations.size() == 0) {
			return;
		}
		for (BroadcastOperations b : this.broadcastOperations) {
			b.sendEvent(name, data);
		}
	}

	@Override
	public Collection<SocketIOClient> getClients() {
		Set<SocketIOClient> clients = new HashSet<SocketIOClient>();
		if (this.broadcastOperations == null || this.broadcastOperations.size() == 0) {
			return clients;
		}
		for (BroadcastOperations b : this.broadcastOperations) {
			clients.addAll(b.getClients());
		}
		return clients;
	}

	@Override
	public <T> void send(Packet packet, BroadcastAckCallback<T> ackCallback) {
		if (this.broadcastOperations == null || this.broadcastOperations.size() == 0) {
			return;
		}
		for (BroadcastOperations b : this.broadcastOperations) {
			b.send(packet, ackCallback);
		}
	}

	@Override
	public void sendEvent(String name, SocketIOClient excludedClient, Object... data) {
		Predicate<SocketIOClient> excludePredicate = (socketIOClient) -> Objects.equals(
				socketIOClient.getSessionId(), excludedClient.getSessionId());
		sendEvent(name,excludePredicate, data);
	}

	@Override
	public void sendEvent(String name, Predicate<SocketIOClient> excludePredicate, Object... data) {
		if (this.broadcastOperations == null || this.broadcastOperations.size() == 0) {
			return;
		}
		for (BroadcastOperations b : this.broadcastOperations) {
			b.sendEvent(name, excludePredicate, data);
		}
	}

	@Override
	public <T> void sendEvent(String name, Object data, BroadcastAckCallback<T> ackCallback) {
		if (this.broadcastOperations == null || this.broadcastOperations.size() == 0) {
			return;
		}
		for (BroadcastOperations b : this.broadcastOperations) {
			b.sendEvent(name, data, ackCallback);
		}
	}

	@Override
	public <T> void sendEvent(String name, Object data, SocketIOClient excludedClient,
			BroadcastAckCallback<T> ackCallback) {
		Predicate<SocketIOClient> excludePredicate = (socketIOClient) -> Objects.equals(
				socketIOClient.getSessionId(), excludedClient.getSessionId());
		sendEvent(name, data, excludePredicate, ackCallback);
	}

	@Override
	public <T> void sendEvent(String name, Object data, Predicate<SocketIOClient> excludePredicate,
			BroadcastAckCallback<T> ackCallback) {
		if (this.broadcastOperations == null || this.broadcastOperations.size() == 0) {
			return;
		}
		for (BroadcastOperations b : this.broadcastOperations) {
			b.sendEvent(name, data, excludePredicate, ackCallback);
		}
	}
}
