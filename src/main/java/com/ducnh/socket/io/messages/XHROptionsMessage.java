package com.ducnh.socket.io.messages;

import java.util.UUID;

public class XHROptionsMessage extends XHRPostMessage{

	public XHROptionsMessage(String origin, UUID sessionId) {
		super(origin, sessionId);
	}
}
