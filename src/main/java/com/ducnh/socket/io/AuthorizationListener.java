package com.ducnh.socket.io;

public interface AuthorizationListener {

	AuthorizationResult getAuthorizationResult(HandshakeData data);
}
