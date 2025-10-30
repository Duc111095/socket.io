package com.ducnh.socket.io;

public abstract class MultiTypeAckCallback extends AckCallback<MultiTypeArgs>{

	private Class<?>[] resultClasses;
	
	public MultiTypeAckCallback(Class<?>... resultClasses) {
		super(MultiTypeAck.class);
		this.resultClasses = resultClasses;
	}
	
	public Class<?>[] getResultClasses() {
		return resultClasses;
	}
}
