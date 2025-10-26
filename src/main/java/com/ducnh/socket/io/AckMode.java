package com.ducnh.socket.io;

public enum AckMode {
	
	/**
	 * Send ack-response automatically on each ack-request
	 * <b>skip</b> exceptions during packet handling
	 */
	AUTO,
	
	/**
	 * Send ack-response automatically on each ack-request
	 * only after <b>success</b> packet handling
	 */
	AUTO_SUCCESS_ONLY,
	
	/**
	 * Turn off auto ack-response sending.
	 * Use AckRequest.sendAckData to send ack-response each time.
	 */
	MANUNAL
}
