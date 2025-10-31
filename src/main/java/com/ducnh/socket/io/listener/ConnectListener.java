package com.ducnh.socket.io.listener;

import com.ducnh.socket.io.SocketIOClient;

public interface ConnectListener {
	
	void onConnect(SocketIOClient client);
}
