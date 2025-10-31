package com.ducnh.socket.io.listener;

import java.util.List;

import com.ducnh.socket.io.SocketIOClient;

import io.netty.channel.ChannelHandlerContext;

public abstract class ExceptionListenerAdapter implements ExceptionListener{

	@Override
	public void onEventException(Exception e, List<Object> data, SocketIOClient client) {
		
	}
	
	@Override
	public void onDisconnectException(Exception e, SocketIOClient client) {
		
	}
	
	@Override
	public void onConnectException(Exception e, SocketIOClient client) {
		
	}
	
	@Override
	public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
		return false;
	}
	
	@Override
	public void onPingException(Exception e, SocketIOClient client) {
		
	}
	
	@Override
	public void onPongException(Exception e, SocketIOClient client) {
		
	}
}
