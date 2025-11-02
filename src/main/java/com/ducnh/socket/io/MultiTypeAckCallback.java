package com.ducnh.socket.io;

public abstract class MultiTypeAckCallback extends AckCallback<MultiTypeArgs>{

	private Class<?>[] resultClasses;
	
	public MultiTypeAckCallback(Class<?>... resultClasses) {
		super(MultiTypeArgs.class);
		this.resultClasses = resultClasses;
	}
	
	public Class<?>[] getResultClasses() {
		return resultClasses;
	}
}
