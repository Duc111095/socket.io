package com.ducnh.socket.io;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.protocol.PacketType;

public class AckRequest {

	private final Packet originalPacket;
	private final SocketIOClient client;
	private final AtomicBoolean sended = new AtomicBoolean();

	public AckRequest(Packet originalPacket, SocketIOClient client) {
		this.originalPacket = originalPacket;
		this.client = client;
	}
	
	public boolean isAckRequested() {
		return originalPacket.isAckRequested();
	}
	
	public void sendAckData(Object... objects ) {
		List<Object> args = Arrays.asList(objects);
		sendAckData(args);
	}
	
	public void sendAckData(List<Object> objs) {
		if (!isAckRequested() || !sended.compareAndSet(false, true)) {
			return;
		}
		
		Packet ackPacket = new Packet(PacketType.MESSAGE, client.getEngineIOVersion());
		ackPacket.setSubType(PacketType.ACK);
		ackPacket.setAckId(originalPacket.getAckId());
		ackPacket.setData(objs);
		client.send(ackPacket);
	}
}
