package com.ducnh.socket.io;

public interface AuthTokenListener {

	AuthTokenResult getAuthTokenResult(Object authToken, SocketIOClient client);
}
