package com.ducnh.socket.io;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.protocol.AckArgs;
import com.ducnh.socket.io.protocol.JsonSupport;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

public class JsonSupportWrapper implements JsonSupport{

	private static final Logger log = LoggerFactory.getLogger(JsonSupportWrapper.class);
	
	private final JsonSupport delegate;
	
	public JsonSupportWrapper(JsonSupport delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public AckArgs readAckArgs(ByteBufInputStream src, AckCallback<?> callback) throws IOException {
		try {
			return delegate.readAckArgs(src, callback);
		} catch (Exception e) {
			src.reset();
			log.error("Can't read ack args: " + src.readLine() + " for type: " + callback.getResultClass(), e);
			throw new IOException(e);
		}
	}

	@Override
	public <T> T readValue(String namespace, ByteBufInputStream src, Class<T> valueType) throws IOException {
		try {
			return delegate.readValue(namespace, src, valueType);
		} catch (Exception ex) {
			src.reset();
			log.error("Can't read value: " + src.readLine() + " for type: " + valueType, ex);
			throw new IOException(ex);
		}
	}

	@Override
	public void writeValue(ByteBufOutputStream out, Object value) throws IOException {
		try {
			delegate.writeValue(out, value);
		} catch (Exception ex) {
			log.error("Can't write value: "  + value, ex);
			throw new IOException(ex);
		}
	}

	@Override
	public void addEventMapping(String namespaceName, String eventName, Class<?>... eventClass) {
		delegate.addEventMapping(namespaceName, eventName, eventClass);
	}

	@Override
	public void removeEventMapping(String namespaceName, String eventName) {
		delegate.removeEventMapping(namespaceName, eventName);
	}

	@Override
	public List<byte[]> getArrays() {
		return delegate.getArrays(); 
	}

	
}
