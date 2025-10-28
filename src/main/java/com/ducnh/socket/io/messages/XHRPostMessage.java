package com.ducnh.socket.io.messages;

import java.util.UUID;

public class XHRPostMessage extends HttpMessage{
	
	public XHRPostMessage(String origin, UUID sessionId) {
		super(origin, sessionId);
	}
}
