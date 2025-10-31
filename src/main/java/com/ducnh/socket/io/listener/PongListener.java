package com.ducnh.socket.io.listener;

import com.ducnh.socket.io.SocketIOClient;

public interface PongListener {
	
	void onPong(SocketIOClient client);
}
