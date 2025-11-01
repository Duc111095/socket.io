package com.ducnh.socket.io.handler;

import com.ducnh.socket.io.AckRequest;
import com.ducnh.socket.io.Transport;
import com.ducnh.socket.io.ack.AckManager;
import com.ducnh.socket.io.namespace.Namespace;
import com.ducnh.socket.io.namespace.NamespacesHub;
import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.protocol.PacketType;
import com.ducnh.socket.io.scheduler.CancelableScheduler;
import com.ducnh.socket.io.transport.NamespaceClient;

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
		case PING:
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
	}
}
