package com.ducnh.socket.io.listener;

public interface DataListener<T> {

	void onData(SocketIOClient client, T data, AckRequest ackSender) throws Exception;
}
