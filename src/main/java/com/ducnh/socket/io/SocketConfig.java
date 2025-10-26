package com.ducnh.socket.io;


/**
 * TCP socket configuration contains configuration for main server channel
 * and client channels
 * 
 * @see java.net.SocketOptions
 */
public class SocketConfig {

	private boolean tcpNoDelay = true;
	
	private int tcpSendBufferSize = -1;
	
	private int tcpReceiveBufferSize = -1;
	
	private boolean tcpKeepAlive = false;
	
	private int soLinger = -1;
	
	private boolean reuseAddress = false;
	
	private int acceptBackLog = 1024;
	
	private int writeBufferWaterMarkLow = -1;
	
	private int writeBufferWaterMarkHigh = -1;
	
	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}
	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}
	
	public int getTcpSendBufferSize() {
		return tcpSendBufferSize;
	}
	public void setTcpSendBufferSize(int tcpSendBufferSize) {
		this.tcpSendBufferSize = tcpSendBufferSize;
	}
	
	public int getTcpReceiveBufferSize() {
		return tcpReceiveBufferSize;
	}
	public void setTcpReceiveBufferSize(int tcpReceiveBufferSize) {
		this.tcpReceiveBufferSize = tcpReceiveBufferSize;
	}
	
	public boolean isTcpKeepAlive() {
		return tcpKeepAlive;
	}
	public void setTcpKeepAlive(boolean tcpKeepAlive) {
		this.tcpKeepAlive = tcpKeepAlive;
	}
	
	public int getSoLinger() {
		return soLinger;
	}
	public void setSoLinger(int soLinger) {
		this.soLinger = soLinger;
	}
	
	public boolean isReuseAddress() {
		return reuseAddress;
	}
	public void setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
	}
	
	public int getAcceptBackLog() {
		return acceptBackLog;
	}
	public void setAcceptBackLog(int acceptBackLog) {
		this.acceptBackLog = acceptBackLog;
	}
	
	public int getWriteBufferWaterMarkLow() {
		return writeBufferWaterMarkLow;
	}
	public void setWriteBufferWaterMarkLow(int writeBufferWaterMarkLow) {
		this.writeBufferWaterMarkLow = writeBufferWaterMarkLow;
	}
	
	public int getWriteBufferWaterMarkHigh() {
		return writeBufferWaterMarkHigh;
	}
	public void setWriteBufferWaterMarkHigh(int writeBufferWaterMarkHigh) {
		this.writeBufferWaterMarkHigh = writeBufferWaterMarkHigh;
	}
}
