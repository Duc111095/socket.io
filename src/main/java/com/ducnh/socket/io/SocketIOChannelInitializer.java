package com.ducnh.socket.io;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.namespace.NamespacesHub;
import com.ducnh.socket.io.protocol.JsonSupport;
import com.ducnh.socket.io.protocol.PacketDecoder;
import com.ducnh.socket.io.protocol.PacketEncoder;
import com.ducnh.socket.io.scheduler.CancelableScheduler;
import com.ducnh.socket.io.scheduler.HashedWheelTimeoutScheduler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;

public class SocketIOChannelInitializer extends ChannelInitializer<Channel> implements DisconnectableHub{

	public static final String SOCKETIO_ENCODER = "socketioEncoder";
	public static final String WEB_SOCKET_TRANSPORT_COMPRESSION = "webSocketTransportCompression";
	public static final String WEB_SOCKET_TRANSPORT = "webSocketTransport";
	public static final String WEB_SOCKET_AGGREGATOR = "webSocketAggregator";
	public static final String XHR_POLLING_TRANSPORT = "xhrPollingTransport";
	public static final String AUTHORIZXE_HANDLER = "authorizeHandler";
	public static final String PACKET_HANDLER = "packetHandler";
	public static final String HTTP_ENCODER = "httpEncoder";
	public static final String HTTP_COMPRESSION = "httpCompression";
	public static final String HTTP_AGGREGATOR = "httpAggregator";
	public static final String HTTP_REQUEST_DECODER = "httpDecoder";
	public static final String SSL_HANDLER = "ssl";
	
	public static final String RESOURCE_HANDLER = "resourceHandler";
	public static final String WRONG_URL_HANDLER = "wrongUrlBlocker";
	
	private static final Logger log = LoggerFactory.getLogger(SocketIOChannelInitializer.class);
	
	private AckManager ackManager;
	
	private ClientBox clientBox = new ClientBox();
	private AuthorizeHandler authorizeHandler;
	private PollingTransport xhrPollingTransport;
	private WebsocketTransport webSocketTransport;
	private EncoderHandler encoderHandler;
	private WrongUrlHandler wrongUrlHandler;
	
	private CancelableScheduler scheduler = new HashedWheelTimeoutScheduler();
	
	private InPacketHandler packetHandler;
	private SSLContext sslContext;
	private Configuration configuration;
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		scheduler.update(ctx);
	}
	
	public void start(Configuration configuration, NamespacesHub namespacesHub) {
		this.configuration = configuration;
		
		ackManager = new AckManager(scheduler);
		
		JsonSupport jsonSupport = configuration.getJsonSupport();
		PacketEncoder encoder = new PacketEncoder(configuration, jsonSupport);
		PacketDecoder decoder = new PacketDecoder(jsonSupport, ackManager);
		
		String connectPath = configuration.getContext() + "/";
		
		boolean isSsl = configuration.getKeyStore() != null;
		if (isSsl) {
			try {
				sslContext = createSSLContext(configuration);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		
		StoreFactory factory = configuration.getStoreFactory();
		authorizeHeader = new AuthorizeHeader(connectPath, scheduler, configuration, namespaceHub, factory, this, ackManager, clientsBox);
		factory.init(namespaceHub, authorizeHandler, jsonSupport);
		xhrPollingTransport = new PollingTransport(decoder, authorizeHeader, clientsBox);
		webSocketTransport = new WebSocketTransport(isSsl, authorizeHeader, configuration, scheduler, clientsBox);
		
		PacketListener packetListener = new PacketListener(ackManager, namespaceHub, xhrPollingTransport, scheduler);
		
		try {
			encoderHandler = new EncoderHandler(configuration, encoder);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		wrongUrlHandler = new WrongUrlHandler();
	}
}
