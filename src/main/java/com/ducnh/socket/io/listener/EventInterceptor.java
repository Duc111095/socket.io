package com.ducnh.socket.io.listener;

import java.util.List;

import com.ducnh.socket.io.AckRequest;
import com.ducnh.socket.io.transport.NamespaceClient;

public interface EventInterceptor {

	void onEvent(NamespaceClient client, String eventName, List<Object> args, AckRequest ackRequest);
}
