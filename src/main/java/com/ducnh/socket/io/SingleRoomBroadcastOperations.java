package com.ducnh.socket.io;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import com.ducnh.socket.io.misc.IterableCollection;
import com.ducnh.socket.io.protocol.EngineIOVersion;
import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.protocol.PacketType;
import com.ducnh.socket.io.store.StoreFactory;
import com.ducnh.socket.io.store.pubsub.DispatchMessage;
import com.ducnh.socket.io.store.pubsub.PubSubType;

public class SingleRoomBroadcastOperations implements BroadcastOperations {

	private final String namespace;
	private final String room;
	private final Iterable<SocketIOClient> clients;
	private final StoreFactory storeFactory;
	
	public SingleRoomBroadcastOperations(String namespace, String room, Iterable<SocketIOClient> clients, StoreFactory storeFactory) {
		super();
		this.namespace = namespace;
		this.room = room;
		this.clients = clients;
		this.storeFactory = storeFactory;
	}
	
	private void dispatch(Packet packet) {
		this.storeFactory.pubSubStore().publish(
				PubSubType.DISPATCH,
				new DispatchMessage(this.room, packet, this.namespace));
	}
	
	@Override
	public void send(Packet packet) {
		for (SocketIOClient client : clients) {
			packet.setEngineIOVersion(client.getEngineIOVersion());
			client.send(packet);
		}
		dispatch(packet);
	}

	@Override
	public void disconnect() {
		for (SocketIOClient client : clients) {
			client.disconnect();
		}
	}

	@Override
	public void sendEvent(String name, Object... data) {
		Packet packet = new Packet(PacketType.MESSAGE, EngineIOVersion.UNKNOWN);
		packet.setSubType(PacketType.EVENT);
		packet.setName(name);
		packet.setData(Arrays.asList(data));
		send(packet);
	}

	@Override
	public Collection<SocketIOClient> getClients() {
		return new IterableCollection<SocketIOClient>(clients);
	}

	@Override
	public <T> void send(Packet packet, BroadcastAckCallback<T> ackCallback) {
		for (SocketIOClient client : clients) {
			client.send(packet, ackCallback.createClientCallback(client));
		}
	}

	@Override
	public void sendEvent(String name, SocketIOClient excludedClient, Object... data) {
		Predicate<SocketIOClient> excludedPredicate = (socketIOClient) -> Objects.equals(
				socketIOClient.getSessionId(), excludedClient.getSessionId());
		sendEvent(name, excludedPredicate, data);
	}

	@Override
	public void sendEvent(String name, Predicate<SocketIOClient> excludePredicate, Object... data) {
		Packet packet = new Packet(PacketType.MESSAGE, EngineIOVersion.UNKNOWN);
		packet.setSubType(PacketType.EVENT);
		packet.setName(name);
		packet.setData(Arrays.asList(data));
		
		for (SocketIOClient client : clients) {
			packet.setEngineIOVersion(client.getEngineIOVersion());
			if (excludePredicate.test(client)) {
				continue;
			}
			client.send(packet);
		}
		dispatch(packet);
	}

	@Override
	public <T> void sendEvent(String name, Object data, BroadcastAckCallback<T> ackCallback) {
		for (SocketIOClient client : clients) {
			client.sendEvent(name, ackCallback.createClientCallback(client), data);
		}
		ackCallback.loopFinished();
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
		for (SocketIOClient client : clients) {
			if (excludePredicate.test(client)) {
				continue;
			}
			client.sendEvent(name, ackCallback.createClientCallback(client), data);
 		}
		ackCallback.loopFinished();
	}

}
