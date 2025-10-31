package com.ducnh.socket.io.messages;

import java.util.UUID;

public class XHROptionMessage extends XHRPostMessage{

	public XHROptionMessage(String origin, UUID sessionId) {
		super(origin, sessionId);
	}
}
