package com.ducnh.socket.io.listener;

import java.util.List;

public interface EventInterceptor {

	void onEvent(NamespaceClient client, String eventName, List<Object> args, AckRequest ackRequest);
}
