package com.ducnh.socket.io.protocol;

import java.io.IOException;

import com.ducnh.socket.io.AckCallback;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.util.List;

/**
 * JSON infrastructure interface.
 * Allows to implement custom realizations
 * to JSON support operations.
 */
public interface JsonSupport {

	AckArgs readAckArgs(ByteBufInputStream src, AckCallback<?> callback) throws IOException;

	<T> T readValue(String namespace, ByteBufInputStream src, Class<T> valueType) throws IOException;
	
	void writeValue(ByteBufOutputStream out, Object value) throws IOException;
	
	void addEventMapping(String namespaceName, String eventName, Class<?>... eventClass);
	
	void removeEventMapping(String namespaceName, String eventName);
	
	List<byte[]> getArrays();
}
