package com.ducnh.socket.io.handler;

public class SocketIOException extends RuntimeException {

	private static final long serialVersionUID = -5923605540124106837L;

	public SocketIOException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SocketIOException(String message) {
		super(message);
	}
	
	public SocketIOException(Throwable cause) {
		super(cause);
	}
}
