package com.ducnh.socket.io.listener;

import java.util.List;

import com.ducnh.socket.io.SocketIOClient;

import io.netty.channel.ChannelHandlerContext;

public interface ExceptionListener {

	void onEventException(Exception e, List<Object> args, SocketIOClient client);
	
	void onDisconnectException(Exception e, SocketIOClient client);
	
	void onConnectException(Exception e, SocketIOClient client);
	
	void onPingException(Exception e, SocketIOClient client);
	
	void onPongException(Exception e, SocketIOClient client);
	
	boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception;

	void onAuthException(Throwable e, SocketIOClient client);
}
