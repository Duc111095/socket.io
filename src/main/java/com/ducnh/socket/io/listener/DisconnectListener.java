package com.ducnh.socket.io.listener;

import com.ducnh.socket.io.SocketIOClient;

public interface DisconnectListener {

	void onDisconnect(SocketIOClient client);
}
