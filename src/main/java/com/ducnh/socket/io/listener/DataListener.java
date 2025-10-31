package com.ducnh.socket.io.listener;

import com.ducnh.socket.io.AckRequest;
import com.ducnh.socket.io.SocketIOClient;

public interface DataListener<T> {

	void onData(SocketIOClient client, T data, AckRequest ackSender) throws Exception;
}
