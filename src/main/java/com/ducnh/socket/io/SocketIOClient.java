package com.ducnh.socket.io;

import java.net.SocketAddress;
import java.util.Set;
import java.util.UUID;

import com.ducnh.socket.io.protocol.EngineIOVersion;
import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.store.Store;

public interface SocketIOClient extends ClientOperations, Store{

	/**
	 * Handshake data used during client connection
	 * @return HandshakeData
	 */
	HandshakeData getHandshakeData();
	
	/**
	 * Current client transport protocol
	 * 
	 * @return transport protocol
	 */
	Transport getTransport();
	
	/**
	 * Engine IO Protocol version
	 * @return IO engine
	 */
	EngineIOVersion getEngineIOVersion();
	
	/**
	 * Returns true if and only if the I/O thread will perform the requested write operation immediately.
	 * Any write requests made when this method returns false are queued until the I/O thread is ready to process
	 * @return
	 */
	boolean isWritable();

	/**
	 * Send event with ack callback
	 * 
	 * @param name - event name
	 * @param data - event data
	 * @param ackCallback - ack callback
	 */
	void sendEvent(String name, AckCallback<?> ackCallback, Object... data);
	
	/**
	 * Send packet with ack callback
	 * 
	 * @param packet - packet to send
	 * @param ackCallback - ack callback
	 */
	void send(Packet packet, AckCallback<?> ackCallback);
	
	
	/**
	 * Client namespace
	 * 
	 * @return namespace
	 */
	SocketIONamespace getNamespace();
	
	/**
	 * Client session id, used {@link UUID} object
	 * 
	 * @return - session id
	 */
	UUID getSessionId();

	/**
	 * Get client remote address
	 * 
	 * @return remote address
	 */
	SocketAddress getRemoteAddress();

	/**
	 * Check is underlying channel open
	 * 
	 * @return <code>true</code> if channel open, otherwise <code>false</code>
	 */
	boolean isChannelOpen();
	
	/**
	 * Join client to room
	 * 
	 * @param room - name of room
	 */
	void joinRoom(String room);

	/**
	 * Join client to rooms
	 * 
	 * @param rooms - name of rooms
	 */
	void joinRooms(Set<String> rooms);

	/**
	 * Leave client from room
	 * 
	 * @param room - name of room
	 */
	void leaveRoom(String room);
	
	/**
	 * Leave client from rooms
	 * 
	 * @param rooms - names of room
	 */
	void leaveRooms(Set<String> rooms);
	
	/**
	 * Get all rooms a client is joined in.
	 * 
	 * @return name of rooms
	 */
	Set<String> getAllRooms();
	
	/**
	 * Get current room Size (contain in cluster)
	 * 
	 * @param room - name of room
	 * 
	 * @return int
	 */
	int getCurrentRoomSize(String room);
}


