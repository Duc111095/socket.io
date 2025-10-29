package com.ducnh.socket.io.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExceptionListener extends ExceptionListenerAdapter{

	private static final Logger log = LoggerFactory.getLogger(DefaultExceptionListener.class);
	
	@Override
	public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
		log.error(e.getMessage(), e);
	}
	
	@Override
	public void onDisconnectException(Exception e, SocketIOClient client) {
		log.error(e.getMessage(), e);
	}
	
	@Override
	public void onConnectException(Exception e, SocketIOClient client) {
		log.error(e.getMessage(), e);
	}
	
	@Override
	public void onPingException(Exception e, SocketIOClient client) {
		log.error(e.getMessage(), e);
	}
	
	@Override
	public void onPongException(Exception e, SocketIOClient client) {
		log.error(e.getMessage(), e);
	}
	
	@Override
	public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
		log.error(e.getMessage(), e);
	}
	
	@Override
	public void onAuthException(Throwable e, SocketIOClient client) {
		log.error(e.getMessage(), e);
	}
}
