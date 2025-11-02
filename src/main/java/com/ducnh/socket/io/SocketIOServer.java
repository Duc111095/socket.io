package com.ducnh.socket.io;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.listener.ClientListeners;
import com.ducnh.socket.io.listener.ConnectListener;
import com.ducnh.socket.io.listener.DataListener;
import com.ducnh.socket.io.listener.DisconnectListener;
import com.ducnh.socket.io.listener.EventInterceptor;
import com.ducnh.socket.io.listener.MultiTypeEventListener;
import com.ducnh.socket.io.listener.PingListener;
import com.ducnh.socket.io.listener.PongListener;
import com.ducnh.socket.io.namespace.Namespace;
import com.ducnh.socket.io.namespace.NamespacesHub;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.ServerChannel;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

public class SocketIOServer implements ClientListeners{

	private static final Logger log = LoggerFactory.getLogger(SocketIOServer.class);
	
	private final Configuration configCopy;
	private final Configuration configuration;
	
	private final NamespacesHub namespacesHub;
	private final SocketIONamespace mainNamespace;
	
	private SocketIOChannelInitializer pipelineFactory = new SocketIOChannelInitializer();
	
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	public SocketIOServer(Configuration configuration) {
		this.configuration = configuration;
		this.configCopy = new Configuration(configuration);
		namespacesHub = new NamespacesHub(configCopy);
		mainNamespace = addNamespace(Namespace.DEFAULT_NAME);
	}
	
	public void setPipelineFactory(SocketIOChannelInitializer pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
	}
	
	public Collection<SocketIOClient> getAllClients() {
		return namespacesHub.get(Namespace.DEFAULT_NAME).getAllClients();
	}
	
	/**
	 * Get Client by uuid from default namespace
	 * 
	 * @param uuid - id of client
	 * @return client
	 */
	public SocketIOClient getClient(UUID uuid) {
		return namespacesHub.get(Namespace.DEFAULT_NAME).getClient(uuid);
	}
	
	/**
	 * Get all namespaces
	 * 
	 * @return namespaces collection
	 */
	public Collection<SocketIONamespace> getAllNamespaces() {
		return namespacesHub.getAllNamespaces();
	}
	
	public BroadcastOperations getBroadcastOperations() {
		Collection<SocketIONamespace> namespaces = namespacesHub.getAllNamespaces();
		List<BroadcastOperations> list = new ArrayList<BroadcastOperations>();
		BroadcastOperations broadcast = null;
		if (namespaces != null && namespaces.size() > 0) {
			for (SocketIONamespace n : namespaces) {
				broadcast = n.getBroadcastOperations();
				list.add(broadcast);
			}
		}
		return new MultiRoomBroadcastOperations(list);
	}
	
	/**
	 * Get broadcast operations for clients within rooms by <code>rooms</code> names
	 * 
	 * @param rooms rooms' names
	 * @return broadcast operations
	 */
	public BroadcastOperations getRoomOperations(String... rooms) {
		Collection<SocketIONamespace> namespaces = namespacesHub.getAllNamespaces();
		List<BroadcastOperations> list = new ArrayList<BroadcastOperations>();
		BroadcastOperations broadcast = null;
		if (namespaces != null && namespaces.size() > 0) {
			for (SocketIONamespace n : namespaces) {
				for (String room : rooms) {
					broadcast = n.getRoomOperations(room);
					list.add(broadcast);
				}
			}
		}
		return new MultiRoomBroadcastOperations(list);
	}
	
	/**
	 * Start server
	 */
	public void start() {
		startAsync().syncUninterruptibly();
	}
	
	/**
	 * Start server asynchronously
	 * 
	 * @return void
	 */
	public Future<Void> startAsync() {
		log.info("Session store / pubsub factory used: {}", configCopy.getStoreFactory());
		initGroups();
		
		pipelineFactory.start(configCopy, namespacesHub);
		
		Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
		if (configCopy.isUseLinuxNativeEpoll()) {
			channelClass = EpollServerSocketChannel.class;
		}
		
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
		.channel(channelClass)
		.childHandler(pipelineFactory);
		applyConnectionOptions(b);
		
		InetSocketAddress addr = new InetSocketAddress(configCopy.getPort());
		if (configCopy.getHostname() != null) {
			addr = new InetSocketAddress(configCopy.getHostname(), configCopy.getPort());
		}
		
		return b.bind(addr).addListener(new FutureListener<Void>() {
			@Override
			public void operationComplete(Future<Void> future) throws Exception {
				if (future.isSuccess()) {
					log.info("SocketIO Server started at port: {}", configCopy.getPort());
				} else {
					log.error("SocketIO server start failed at port: {}", configCopy.getPort());;
				}
			}
		});
	}
	
	protected void applyConnectionOptions(ServerBootstrap bootstrap) {
		SocketConfig config = configCopy.getSocketConfig();
		bootstrap.childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay());
		if (config.getTcpSendBufferSize() != -1) {
			bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getTcpReceiveBufferSize());
		}
		if (config.getTcpReceiveBufferSize() != -1) {
			bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getTcpReceiveBufferSize());
			bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(config.getTcpReceiveBufferSize()));
		}
		if (config.getWriteBufferWaterMarkLow() != -1 && config.getWriteBufferWaterMarkHigh() != -1) {
			bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
					config.getWriteBufferWaterMarkLow(), config.getWriteBufferWaterMarkHigh()));
		}
		
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, config.isTcpKeepAlive());
		bootstrap.childOption(ChannelOption.SO_LINGER, config.getSoLinger());
		
		bootstrap.option(ChannelOption.SO_REUSEADDR, config.isReuseAddress());
		bootstrap.option(ChannelOption.SO_BACKLOG, config.getAcceptBackLog());
	} 
	
	protected void initGroups() {
		if (configCopy.isUseLinuxNativeEpoll()) {
			bossGroup = new EpollEventLoopGroup(configCopy.getBossThreads());
			workerGroup = new EpollEventLoopGroup(configCopy.getWorkerThreads());
		} else {
			bossGroup = new NioEventLoopGroup(configCopy.getBossThreads());
			workerGroup = new NioEventLoopGroup(configCopy.getWorkerThreads());
		}
	}
	
	public void stop() {
		bossGroup.shutdownGracefully().syncUninterruptibly();
		workerGroup.shutdownGracefully().syncUninterruptibly();
		
		pipelineFactory.stop();
		log.info("SocketIO server stopped");
	}
	
	public SocketIONamespace addNamespace(String name) {
		return namespacesHub.create(name);
	}
	
	public SocketIONamespace getNamespace(String name) {
		return namespacesHub.get(name);
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	@Override
	public void addMultiTypeEventListener(String eventName, MultiTypeEventListener listener, Class<?>... eventClass) {
		mainNamespace.addMultiTypeEventListener(eventName, listener, eventClass);
	}
	
	@Override
	public <T> void addEventListener(String eventName, Class<T> eventClass, DataListener<T> listener) {
		mainNamespace.addEventListener(eventName, eventClass, listener);
	}
	
	@Override
	public void addEventInterceptor(EventInterceptor eventInterceptor) {
		mainNamespace.addEventInterceptor(eventInterceptor);
	}
	
	@Override
	public void removeAllListeners(String eventName) {
		mainNamespace.removeAllListeners(eventName);
	}
	
	@Override
	public void addDisconnectListener(DisconnectListener listener) {
		mainNamespace.addDisconnectListener(listener);
	}
	
	@Override
	public void addConnectListener(ConnectListener listener) {
		mainNamespace.addConnectListener(listener);
	}
	
	@Override
	public void addPingListener(PingListener listener) {
		mainNamespace.addPingListener(listener);
	}
	
	@Override
	public void addPongListener(PongListener listener) {
		mainNamespace.addPongListener(listener);
	}
	
	@Override
	public void addListeners(Object listeners) {
		mainNamespace.addListeners(listeners);
	}
	
	@Override
	public <L> void addListeners(Iterable<L> listeners) {
		mainNamespace.addListeners(listeners);
	}
	
	@Override
	public void addListeners(Object listeners, Class<?> listenersClass) {
		mainNamespace.addListeners(listeners, listenersClass);
	}
}
