package com.ducnh.socket.io;

import com.ducnh.socket.io.handler.ClientHead;

public interface Disconnectable {

	void onDisconnect(ClientHead client);
}
