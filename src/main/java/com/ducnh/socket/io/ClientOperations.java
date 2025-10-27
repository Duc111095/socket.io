package com.ducnh.socket.io;

import com.ducnh.socket.io.protocol.Packet;

/**
 * Available client operations
 * 
 */
public interface ClientOperations {

	/**
	 * Send custom packet.
	 * But {@link ClientOperations#sendEvent} method
	 * usage is enough for most cases.
	 * 
	 * @param packet - packet to send
	 */
	void send(Packet packet);
	
	/**
	 * Disconnect client
	 * 
	 */
	void disconnect();
	
	/**
	 * Send event
	 * 
	 * @param name - event name
	 * @param data - event data
	 */
	void sendEvent(String name, Object... data);
}
