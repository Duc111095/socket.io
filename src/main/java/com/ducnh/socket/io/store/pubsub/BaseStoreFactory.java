package com.ducnh.socket.io.store.pubsub;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.namespace.Namespace;
import com.ducnh.socket.io.protocol.JsonSupport;
import com.ducnh.socket.io.store.StoreFactory;

public abstract class BaseStoreFactory implements StoreFactory {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private Long nodeId = (long) (Math.random() * 1000000);
	
	protected Long getNodeId() {
		return nodeId;
	}
	
	@Override
	public void init(final NamespaceHub namespacesHub, final AuthorizeHeader authorizeHeader, JsonSupport jsonSupport) {
		pubSubStore().subscribe(PubSubType.DISCONNECT, new PubSubListener<DisconnectMessage>() {
			@Override
			public void onMessage(DisconnectMessage msg) {
				log.debug("{} sessionId: {}", PubSubType.CONNECT, msg.getSessionId());
			}
		}, DisconnectMessage.class);
		
		pubSubStore().subscribe(PubSubType.CONNECT, new PubSubListener<ConnectMessage>() {
			@Override
			public void onMessage(ConnectMessage msg){
				authorizeHeader.connect(msg.getSessionId());
				log.debug("{} sessionId: {}", PubSubType.CONNECT, msg.getSessionId());
			}
		}, ConnectMessage.class);
		
		pubSubStore().subscribe(PubSubType.DISPATCH, new PubSubListener<DispatchMessage>() {
			@Override
			public void onMessage(DispatchMessage msg) {
				String name = msg.getRoom();
				Namespace n = namespacesHub.get(msg.getNamespace());
				
				if (n != null) {
					n.dispatch(name, msg.getPacket());
				}
				log.debug("{} packet: {}", PubSubType.DISPATCH, msg.getPacket());
			}
		}, DispatchMessage.class);
	
		pubSubStore().subscribe(PubSubType.JOIN, new PubSubListener<JoinLeaveMessage>() {
			@Override
			public void onMessage(JoinLeaveMessage msg) {
				String name = msg.getRoom();
				
				Namespace n = namespacesHub.get(msg.getNamespace());
				if (n != null) {
					n.join(name, msg.getSessionId());
				} 
				log.debug("{} sessionId: {}", PubSubType.JOIN, msg.getSessionId());
			}
		}, JoinLeaveMessage.class);
	
		pubSubStore().subscribe(PubSubType.BULK_JOIN, new PubSubListener<BulkJoinLeaveMessage>( ) {
			@Override
			public void onMessage(BulkJoinLeaveMessage msg) {
				Set<String> rooms = msg.getRooms();
				for (String room : rooms) {
					Namespace n = namespacesHub.get(msg.getNamespace());
					if (n != null) {
						n.join(room, msg.getSessionId());
					}
				}
				log.debug("{} sessionId: {}" , PubSubType.BULK_JOIN, msg.getSessionId());
			} 
		}, BulkJoinLeaveMessage.class);
	
		pubSubStore().subscribe(PubSubType.LEAVE, new PubSubListener<JoinLeaveMessage> () {
			@Override
			public void onMessage(JoinLeaveMessage msg) {
				String name = msg.getRoom();
				
				Namespace n = namespacesHub.get(msg.getNamespace());
				if (n != null) {
					n.leave(name, msg.getSessionId());
				}
				log.debug("{} sessionId: {}", PubSubType.LEAVE, msg.getSessionId());
			}
		}, JoinLeaveMessage.class);
	
		pubSubStore().subscribe(PubSubType.BULK_LEAVE, new PubSubListener<BulkJoinLeaveMessage>( ) {
			@Override
			public void onMessage(BulkJoinLeaveMessage msg) {
				Set<String> rooms = msg.getRooms();
				
				for (String room : rooms) {
					Namespace n = namespacesHub.get(msg.getNamespace());
					if (n != null) {
						n.leave(room, msg.getSessionId());
					}
				}
				log.debug("{} sessionId: {}", PubSubType.BULK_LEAVE, msg.getSessionId());
			}
		}, BulkJoinLeaveMessage.class);
	}
	
	
	@Override
	public abstract PubSubStore pubSubStore();
	
	@Override
	public void onDisconnect(ClientHead client) {
		
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " (distributed session store, distributed publish/subscribe";
	}
}
