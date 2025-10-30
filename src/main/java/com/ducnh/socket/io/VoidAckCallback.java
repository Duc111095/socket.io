package com.ducnh.socket.io;

public abstract class VoidAckCallback extends AckCallback<Void>{

	public VoidAckCallback() {
		super(Void.class);
	}
	
	public VoidAckCallback(int timeout) {
		super(Void.class, timeout);
	}
	
	@Override
	public final void onSuccess(Void result) {
		onSuccess();
	}
	
	protected abstract void onSuccess();
}
