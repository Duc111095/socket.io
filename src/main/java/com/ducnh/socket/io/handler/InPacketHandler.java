package com.ducnh.socket.io.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.AuthTokenResult;
import com.ducnh.socket.io.listener.ExceptionListener;
import com.ducnh.socket.io.messages.PacketsMessage;
import com.ducnh.socket.io.namespace.Namespace;
import com.ducnh.socket.io.namespace.NamespacesHub;
import com.ducnh.socket.io.protocol.ConnPacket;
import com.ducnh.socket.io.protocol.EngineIOVersion;
import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.protocol.PacketDecoder;
import com.ducnh.socket.io.protocol.PacketType;
import com.ducnh.socket.io.transport.NamespaceClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class InPacketHandler extends SimpleChannelInboundHandler<PacketsMessage>{

	private static final Logger log = LoggerFactory.getLogger(InPacketHandler.class);
	
	private final PacketListener packetListener;
	private final PacketDecoder decoder;
	private final NamespacesHub namespacesHub;
	private final ExceptionListener exceptionListener;
	
	public InPacketHandler(PacketListener packetListener, PacketDecoder decoder, NamespacesHub namespacesHub, ExceptionListener exceptionListener) {
		super();
		this.packetListener = packetListener;
		this.decoder = decoder;
		this.namespacesHub = namespacesHub;
		this.exceptionListener = exceptionListener;
	}
	
	@Override
	protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, PacketsMessage message) 
				throws Exception{
		ByteBuf content = message.getContent();
		ClientHead client = message.getClient();
		
		if (log.isTraceEnabled()) {
			log.trace("In message: {} sessionId: {}", content.toString(CharsetUtil.UTF_8), client.getSessionId());
		}
		
		while (content.isReadable()) {
			try {
				Packet packet = decoder.decodePackets(content, client);
				
				Namespace ns = namespacesHub.get(packet.getNsp());
				
				if (ns == null) {
					if (packet.getSubType() == PacketType.CONNECT) {
						Packet p = new Packet(PacketType.MESSAGE, client.getEngineIOVersion());
						p.setSubType(PacketType.ERROR);
						p.setNsp(packet.getNsp());
						p.setData("Invalid namespace");
						client.send(p);
						return;
					}
					log.debug("Can't find namespace for endpoint: {}, sessionId: {} probably it was removed.", packet.getNsp(), client.getSessionId());
					return;
				}
			
				if (packet.getSubType() == PacketType.CONNECT) {
					client.addNamespaceClient(ns);
					NamespaceClient nClient = client.getChildClient(ns);
					if (EngineIOVersion.V4.equals(client.getEngineIOVersion())) {
						if (packet.getData() != null) {
							final Object authData = packet.getData();
							client.getHandshakeData().setAuthToken(authData);
							final AuthTokenResult allowAuth = ns.onAuthData(nClient, authData);
							if (!allowAuth.isSuccess()) {
								Packet p = new Packet(PacketType.MESSAGE, client.getEngineIOVersion());
								p.setSubType(PacketType.ERROR);
								p.setNsp(packet.getNsp());
								final Object errorData = allowAuth.getErrorData();
								if (errorData != null) {
									p.setData(errorData);
								}
								client.send(p);
								return;
							}
						}
						Packet p = new Packet(PacketType.MESSAGE, client.getEngineIOVersion());
						p.setSubType(PacketType.CONNECT);
						p.setNsp(packet.getNsp());
						p.setData(new ConnPacket(client.getSessionId()));
						client.send(p);
					}
				}
				
				NamespaceClient nClient = client.getChildClient(ns);
				if (nClient == null) {
					log.debug("Can't find namespace client in namespace: {}, sessionId: {} probably it was disconnect.", ns.getName(), client.getSessionId());
					return;
				}
				if (packet.hasAttachments() && !packet.isAttachmentsLoaded()) {
					return;
				}
				packetListener.onPacket(packet, nClient, message.getTransport());
			} catch (Exception ex) {
				String c = content.toString(CharsetUtil.UTF_8);
				log.error("Error during data processing. Client sessionId: " + client.getSessionId() + ", data: " + c, ex);
				throw ex;
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
		if (!exceptionListener.exceptionCaught(ctx, e)) {
			super.exceptionCaught(ctx, e);
		}
	}
}
