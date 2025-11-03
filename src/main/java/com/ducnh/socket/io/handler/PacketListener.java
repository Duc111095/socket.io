package com.ducnh.socket.io.handler;

import java.util.Collections;
import java.util.List;

import com.ducnh.socket.io.AckRequest;
import com.ducnh.socket.io.Transport;
import com.ducnh.socket.io.ack.AckManager;
import com.ducnh.socket.io.namespace.Namespace;
import com.ducnh.socket.io.namespace.NamespacesHub;
import com.ducnh.socket.io.protocol.EngineIOVersion;
import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.protocol.PacketType;
import com.ducnh.socket.io.scheduler.CancelableScheduler;
import com.ducnh.socket.io.scheduler.SchedulerKey;
import com.ducnh.socket.io.transport.NamespaceClient;
import com.ducnh.socket.io.transport.PollingTransport;

public class PacketListener {

	private final NamespacesHub namespacesHub;
	private final AckManager ackManager;
	private final CancelableScheduler scheduler;

	public PacketListener(AckManager ackManager, NamespacesHub namespacesHub, PollingTransport xhrPollingTransport,
			CancelableScheduler scheduler) {
		this.ackManager = ackManager;
		this.namespacesHub = namespacesHub;
		this.scheduler = scheduler;
	}
	
	public void onPacket(Packet packet, NamespaceClient client, Transport transport) {
		final AckRequest ackRequest = new AckRequest(packet, client);
		
		if (packet.isAckRequested()) {
			ackManager.initAckIndex(client.getSessionId(), packet.getAckId());
		}
		
		switch (packet.getType()) {
		case PING: {
			Packet outPacket = new Packet(PacketType.PONG, client.getEngineIOVersion());
			outPacket.setData(packet.getData());
			client.getBaseClient().send(outPacket, transport);
			if ("probe".equals(packet.getData())) {
				client.getBaseClient().send(new Packet(PacketType.NOOP, client.getEngineIOVersion()), Transport.POLLING);
			} else {
				client.getBaseClient().schedulePingTimeout();
			}
			Namespace namespace = namespacesHub.get(packet.getNsp());
			namespace.onPing(client);
			break;
		}
		case PONG: {
			client.getBaseClient().schedulePingTimeout();
			Namespace namespace = namespacesHub.get(packet.getNsp());
			namespace.onPong(client);
			break;
		}
		
		case UPGRADE: {
			client.getBaseClient().schedulePingTimeout();
			
			SchedulerKey key = new SchedulerKey(SchedulerKey.Type.UPGRADE_TIMEOUT, client.getSessionId());
			scheduler.cancel(key);
			
			client.getBaseClient().upgradeCurrentTransport(transport);
			break;
		}
		
		case MESSAGE: {
			client.getBaseClient().schedulePingTimeout();
			
			if (packet.getSubType() == PacketType.DISCONNECT) {
				client.onDisconnect();
			}
			
			if (packet.getSubType() == PacketType.CONNECT) {
				Namespace namespace = namespacesHub.get(packet.getNsp());
				namespace.onConnect(client);
				
				if (!EngineIOVersion.V4.equals(client.getEngineIOVersion())) {
					client.getBaseClient().send(packet, transport);
				}
			}
			
			if (packet.getSubType() == PacketType.ACK || packet.getSubType() == PacketType.BINARY_ACK) {
				ackManager.onAck(client, packet);
			}
			
			if (packet.getSubType() == PacketType.EVENT || packet.getSubType()  == PacketType.BINARY_EVENT) {
				Namespace namespace = namespacesHub.get(packet.getNsp());
				List<Object> args = Collections.emptyList();
				if (packet.getData() != null) {
					args = packet.getData();
				}
				namespace.onEvent(client, packet.getName(), args, ackRequest);
			} 
			break;
		}
		
		case CLOSE: {
			client.getBaseClient().onChannelDisconnect();
			break;
		}
		
		default:
			break;
		}
		
	}
}
