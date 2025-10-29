package com.ducnh.socket.io.listener;

import java.util.List;

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
		
	}
	
	@Override
	public void onPingException(Exception e, SocketIOClient client) {
		
	}
	
	@Override
	public void onPongException(Exception e, SocketIOClient client) {
		
	}
}
