package com.ducnh.socket.io.listener;

import com.ducnh.socket.io.SocketIOClient;

public interface PingListener {

	void onPing(SocketIOClient client);
}
