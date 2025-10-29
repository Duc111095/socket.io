package com.ducnh.socket.io;


/*
 * Base ack callback class.
 * 
 * Notifies about acknowledgement received from client
 * via {@link #onSuccess} callback method.
 * 
 * By default it may wait acknowledgement from client while
 * {@link SocketIOClient} is alive. Timeout can be defined
 * {@link #timeout} as constructor argument.
 * 
 * This object is NOT actual anymore if {@link #onSuccess} or 
 * {@link #onTimeout} was executed.
 * 
 * @param <T> -- any serializable type
 * 
 */
public abstract class AckCallback<T> {

	protected final Class<T> resultClass;
	protected final int timeout;
	
	/*
	 * Create AckCallback
	 * 
	 * @param resultClass - result class
	 */
	public AckCallback(Class<T> resultClass) {
		this(resultClass, -1);
	}
	
	/**
	 * Create AckCallback with timeout
	 * 
	 * @param resultClass2 - resultClass
	 * @param timeout - callback timeout in seconds
	 */
	public AckCallback(Class<T> resultClass2, int timeout) {
		this.resultClass = resultClass2;
		this.timeout = timeout;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	/**
	 * Executes only once when acknowledgement received from client.
	 * 
	 * @param result - object sender by client
	 */
	public abstract void onSuccess(T result);

	/**
	 * Invoked only once then <code>timeout</code> defined
	 * 
	 */
	public void onTimeout() {
		
	}
	
	/** 
	 * Returns class of argument in {@link #onSuccess} method
	 * 
	 * @return - result class
	 */
	public Class<T> getResultClass() {
		return resultClass;
	}
}
