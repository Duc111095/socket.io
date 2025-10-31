package com.ducnh.socket.io.store.pubsub;

import java.io.Serializable;

public abstract class PubSubMessage implements Serializable {

	private static final long serialVersionUID = -8465453079060592395L;

	private Long nodeId;
	
	public Long getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}
}
