package com.ducnh.socket.io.handler;

import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.SocketIOClient;
import com.ducnh.socket.io.AuthorizationResult;
import com.ducnh.socket.io.Configuration;
import com.ducnh.socket.io.Disconnectable;
import com.ducnh.socket.io.DisconnectableHub;
import com.ducnh.socket.io.HandshakeData;
import com.ducnh.socket.io.Transport;
import com.ducnh.socket.io.ack.AckManager;
import com.ducnh.socket.io.messages.HttpErrorMessage;
import com.ducnh.socket.io.namespace.Namespace;
import com.ducnh.socket.io.namespace.NamespacesHub;
import com.ducnh.socket.io.protocol.AuthPacket;
import com.ducnh.socket.io.protocol.EngineIOVersion;
import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.protocol.PacketType;
import com.ducnh.socket.io.scheduler.CancelableScheduler;
import com.ducnh.socket.io.scheduler.SchedulerKey;
import com.ducnh.socket.io.scheduler.SchedulerKey.Type;
import com.ducnh.socket.io.store.Store;
import com.ducnh.socket.io.store.StoreFactory;
import com.ducnh.socket.io.store.pubsub.ConnectMessage;
import com.ducnh.socket.io.store.pubsub.PubSubType;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

public class AuthorizeHandler extends ChannelInboundHandlerAdapter implements Disconnectable{

	private static final Logger log = LoggerFactory.getLogger(AuthorizeHandler.class);
	
	private final CancelableScheduler scheduler;
	
	private final String connectPath;
	private final Configuration configuration;
	private final NamespacesHub namespacesHub;
	private final StoreFactory storeFactory;
	private final DisconnectableHub disconnectable;
	private final AckManager ackManager;
	private final ClientsBox clientsBox;
	
	public AuthorizeHandler(String connectPath, CancelableScheduler scheduler, Configuration configuration, NamespacesHub namespacesHub, StoreFactory storeFactory,
			DisconnectableHub disconnectable, AckManager ackManager, ClientsBox clientsBox) {
		super();
		this.connectPath = connectPath;
		this.configuration = configuration;
		this.scheduler = scheduler;
		this.namespacesHub = namespacesHub;
		this.storeFactory = storeFactory;
		this.disconnectable = disconnectable;
		this.ackManager = ackManager;
		this.clientsBox = clientsBox;
	}
	
	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		SchedulerKey key = new SchedulerKey(Type.PING_TIMEOUT, ctx.channel());
		scheduler.schedule(key, new Runnable() {
			@Override
			public void run() {
				ctx.channel().close();
				log.debug("Client with ip {} opened channel but doesn't send any data! Channel closed!", ctx.channel().remoteAddress());
			}
		}, configuration.getFirstDataTimeout(), TimeUnit.MILLISECONDS);
		super.channelActive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		SchedulerKey key = new SchedulerKey(Type.PING_TIMEOUT, ctx.channel());
		scheduler.cancel(key);
		
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest req = (FullHttpRequest) msg;
			Channel channel = ctx.channel();
			QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
			
			if (!configuration.isAllowCustomRequests() && !queryDecoder.path().startsWith(connectPath)) {
				HttpResponse res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
				channel.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
				req.release();
				return;
			}	
			
			List<String> sid = queryDecoder.parameters().get("sid");
			if (queryDecoder.path().equals(connectPath)
					&& sid == null) {
				String origin = req.headers().get(HttpHeaderNames.ORIGIN);
				if (!authorize(ctx, channel, origin, queryDecoder.parameters(), req)) {
					req.release();
					return;
				
				}

			}
		}
		ctx.fireChannelRead(msg);
	}
	
	private boolean authorize(ChannelHandlerContext ctx, Channel channel, String origin, Map<String, List<String>> params, FullHttpRequest req) {
		Map<String, List<String>> headers = new HashMap<String, List<String>>(req.headers().names().size());
		for (String name : req.headers().names()) {
			List<String> values = req.headers().getAll(name);
			headers.put(name, values);
		}
		
		HandshakeData data = new HandshakeData(req.headers(), params,
				(InetSocketAddress) channel.remoteAddress(),
				(InetSocketAddress) channel.localAddress(),
				req.uri(), origin != null && !origin.equalsIgnoreCase("null"));
	
		boolean result = false;
		Map<String, Object> storeParams = Collections.emptyMap();
		try {
			AuthorizationResult authResult = configuration.getAuthorizationListener().getAuthorizationResult(data);
			result = authResult.isAuthorized();
			storeParams = authResult.getStoreParams();
		} catch (Exception e) {
			log.error("Authorization error", e);
		}
		
		if (!result) {
			HttpResponse res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
			channel.writeAndFlush(res)
					.addListener(ChannelFutureListener.CLOSE);
			log.debug("Handshake unauthorized, query params: {} headers: {}", params, headers);
			return false;
		}
		
		UUID sessionId = null;
		if (configuration.isRandomSession()) {
			sessionId = UUID.randomUUID();
		} else {
			sessionId = this.generateOrGetSessionIdFromRequest(req.headers());
		}
		
		List<String> transportValue = params.get("transport");
		if (transportValue == null) {
			log.error("Got no transports for request {}", req.uri());
			writeAndFlushTransportError(channel, origin);
			return false;
		}
		
		Transport transport = null;
		try {
			transport = Transport.valueOf(transportValue.get(0).toUpperCase());
		} catch (IllegalArgumentException e) {
			log.error("Unsupported transport for request {}", req.uri());
			writeAndFlushTransportError(channel, origin);
			return false;
		}
		
		ClientHead client = new ClientHead(sessionId, ackManager, disconnectable, storeFactory, data, clientsBox, transport, scheduler, configuration, params);
		Store store = client.getStore();
		storeParams.forEach(store::set);
		channel.attr(ClientHead.CLIENT).set(client);
		clientsBox.addClient(client);
		
		String[] transports = {};
		if (configuration.getTransports().contains(Transport.WEBSOCKET) &&
				!(EngineIOVersion.V4.equals(client.getEngineIOVersion()) && Transport.WEBSOCKET.equals(client.getCurrentTransport()))) {
			transports = new String[] {"websocket"};
		}
		
		AuthPacket authPacket = new AuthPacket(sessionId, transports, configuration.getPingInterval(), configuration.getPingTimeout());
		Packet packet = new Packet(PacketType.OPEN, client.getEngineIOVersion());
		packet.setData(authPacket);
		client.send(packet);
		
		client.schedulePing();
		client.schedulePingTimeout();
		log.debug("Handshake authorized for sessionId: {}, query params: {} headers: {}", sessionId, params, headers);
		return true;
	}
	
	private void writeAndFlushTransportError(Channel channel, String origin) {
		Map<String, Object> errorData = new HashMap<String, Object>();
		errorData.put("code", 0);
		errorData.put("message", "Transport unknown");
		
		channel.attr(EncoderHandler.ORIGIN).set(origin);
		channel.writeAndFlush(new HttpErrorMessage(errorData));
	}
	
	private UUID generateOrGetSessionIdFromRequest(HttpHeaders headers) {
		List<String> values = headers.getAll("io");
		if (values.size() == 1) {
			try {
				return UUID.fromString(values.get(0));
			} catch (IllegalArgumentException iaex) {
				log.warn("Malformed UUID received for session! io=" + values.get(0));
			}
		}
		for (String cookieHeader : headers.getAll(HttpHeaderNames.COOKIE)) {
			Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieHeader);
			
			for (Cookie cookie : cookies) {
				if (cookie.name().equals("io")) {
					try {
						return UUID.fromString(cookie.value());
					} catch (IllegalArgumentException iaex) {
						log.warn("Malformed UUID received for session! io=" + cookie.value());
					}
				}
			}
		}
		return UUID.randomUUID();
	}
	
	public void connect(UUID sessionId) {
		SchedulerKey key = new SchedulerKey(Type.PING_TIMEOUT, sessionId);
		scheduler.cancel(key);
	}
	
	public void connect(ClientHead client) {
		Namespace ns = namespacesHub.get(Namespace.DEFAULT_NAME);
		
		if (!client.getNamespace().contains(ns)) {
			Packet packet = new Packet(PacketType.MESSAGE, client.getEngineIOVersion());
			packet.setSubType(PacketType.CONNECT);
			if (!EngineIOVersion.V4.equals(client.getEngineIOVersion())) {
				client.send(packet);
			}
			configuration.getStoreFactory().pubSubStore().publish(PubSubType.CONNECT, new ConnectMessage(client.getSessionId()));
			SocketIOClient nsClient = client.addNamespaceClient(ns);
			ns.onConnect(nsClient);
		}
		
	}
	
	@Override
	public void onDisconnect(ClientHead client) {
		clientsBox.removeClient(client.getSessionId());
	}

}
