package com.ducnh.socket.io.namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.ducnh.socket.io.AckMode;
import com.ducnh.socket.io.AckRequest;
import com.ducnh.socket.io.AuthTokenListener;
import com.ducnh.socket.io.AuthTokenResult;
import com.ducnh.socket.io.BroadcastOperations;
import com.ducnh.socket.io.Configuration;
import com.ducnh.socket.io.MultiRoomBroadcastOperations;
import com.ducnh.socket.io.MultiTypeArgs;
import com.ducnh.socket.io.SingleRoomBroadcastOperations;
import com.ducnh.socket.io.SocketIOClient;
import com.ducnh.socket.io.SocketIONamespace;
import com.ducnh.socket.io.annotation.ScannerEngine;
import com.ducnh.socket.io.listener.ConnectListener;
import com.ducnh.socket.io.listener.DataListener;
import com.ducnh.socket.io.listener.DisconnectListener;
import com.ducnh.socket.io.listener.EventInterceptor;
import com.ducnh.socket.io.listener.ExceptionListener;
import com.ducnh.socket.io.listener.MultiTypeEventListener;
import com.ducnh.socket.io.listener.PingListener;
import com.ducnh.socket.io.listener.PongListener;
import com.ducnh.socket.io.protocol.JsonSupport;
import com.ducnh.socket.io.protocol.Packet;
import com.ducnh.socket.io.store.StoreFactory;
import com.ducnh.socket.io.store.pubsub.BulkJoinLeaveMessage;
import com.ducnh.socket.io.store.pubsub.JoinLeaveMessage;
import com.ducnh.socket.io.store.pubsub.PubSubType;
import com.ducnh.socket.io.transport.NamespaceClient;

import io.netty.util.internal.PlatformDependent;

/**
 * Hub object for all clients in one namespace.
 * Namespace shares by different namespace-clients.
 * 
 */
public class Namespace implements SocketIONamespace {
	
	public static final String DEFAULT_NAME = "";
	
	private final ScannerEngine engine = new ScannerEngine();
	private final ConcurrentMap<String, EventEntry<?>> eventListeners = PlatformDependent.newConcurrentHashMap();
	private final Queue<ConnectListener> connectListeners = new ConcurrentLinkedQueue<ConnectListener>();
	private final Queue<DisconnectListener> disconnectListeners = new ConcurrentLinkedQueue<DisconnectListener>();
	private final Queue<PingListener> pingListeners = new ConcurrentLinkedQueue<PingListener>();
	private final Queue<PongListener> pongListeners = new ConcurrentLinkedQueue<PongListener>();
	private final Queue<EventInterceptor> eventInterceptors = new ConcurrentLinkedQueue<EventInterceptor>();
	
	private final Queue<AuthTokenListener> authDataInterceptors = new ConcurrentLinkedQueue<>();
	
	private final Map<UUID, SocketIOClient> allClients = PlatformDependent.newConcurrentHashMap();
	private final ConcurrentMap<String, Set<UUID>> roomClients = PlatformDependent.newConcurrentHashMap();
	private final ConcurrentMap<UUID, Set<String>> clientRooms = PlatformDependent.newConcurrentHashMap();
	
	private final String name;
	private final AckMode ackMode;
	private final JsonSupport jsonSupport;
	private final StoreFactory storeFactory;
	private final ExceptionListener exceptionListener;
	
	public Namespace(String name, Configuration configuration) {
		super();
		this.name = name;
		this.jsonSupport = configuration.getJsonSupport();
		this.storeFactory = configuration.getStoreFactory();
		this.exceptionListener	= configuration.getExceptionListener();
		this.ackMode = configuration.getAckMode();
	}
	
	public void addClient(SocketIOClient client) {
		allClients.put(client.getSessionId(), client);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void addMultiTypeEventListener(String eventName, MultiTypeEventListener listener, Class<?>... eventClasses) {
		EventEntry entry = eventListeners.get(eventName);
	    if (entry == null) {
			entry = new EventEntry();
			EventEntry<?> oldEntry = eventListeners.putIfAbsent(eventName, entry);
			if (oldEntry != null) {
				entry = oldEntry;
			}
		}
	    entry.addListener(listener);
	    jsonSupport.addEventMapping(name, eventName, eventClasses);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public <T> void addEventListener(String eventName, Class<T> eventClass, DataListener<T> listener) {
		EventEntry entry = eventListeners.get(eventName);
		if (entry == null) {
			entry = new EventEntry();
			EventEntry<?> oldEntry = eventListeners.putIfAbsent(eventName, entry);
			if (oldEntry != null) {
				entry = oldEntry;
			}
		}
		entry.addListener(listener);
		jsonSupport.addEventMapping(name, eventName, eventClass);
	}

	@Override
	public void addEventInterceptor(EventInterceptor eventInterceptor) {
		eventInterceptors.add(eventInterceptor);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void onEvent(NamespaceClient client, String eventName, List<Object> args, AckRequest ackRequest) {
		EventEntry entry = eventListeners.get(eventName);
		if (entry == null) {
			return;
		}
		try {
			Queue<DataListener> listeners = entry.getListeners();
			for (DataListener dataListener : listeners) {
				Object data = getEventData(args, dataListener);
				dataListener.onData(client, data, ackRequest);
			}
			
			for (EventInterceptor eventInterceptor : eventInterceptors) {
				eventInterceptor.onEvent(client, eventName, args, ackRequest);
			}
		} catch (Exception e) {
			exceptionListener.onEventException(e, args, client);
			if (ackMode == AckMode.AUTO_SUCCESS_ONLY) {
				return;
			}
		}
		sendAck(ackRequest);
	}
	
	private void sendAck(AckRequest ackRequest) {
		if (ackMode == AckMode.AUTO || ackMode == AckMode.AUTO_SUCCESS_ONLY) {
			ackRequest.sendAckData(Collections.emptyList());
		}
	}
	
	private Object getEventData(List<Object> args, DataListener<?> dataListener) {
		if (dataListener instanceof MultiTypeEventListener) {
			return new MultiTypeArgs(args);
		} else {
			if (!args.isEmpty()) {
				return args.get(0);
			}
		}
		return null;
	}
	
	@Override
	public void addDisconnectListener(DisconnectListener listener) {
		disconnectListeners.add(listener);
	}

	public void onDisconnect(SocketIOClient client) {
		Set<String> joinedRooms = client.getAllRooms();
		allClients.remove(client.getSessionId());
		final Set<String> roomsToLeave = new HashSet<>(joinedRooms);
		
		for (String joinedRoom : joinedRooms) {
			leave(roomClients, joinedRoom, client.getSessionId());
		}
		clientRooms.remove(client.getSessionId());
		storeFactory.pubSubStore().publish(PubSubType.BULK_LEAVE, new BulkJoinLeaveMessage(client.getSessionId(), roomsToLeave, getName()));
		
		try {
			for (DisconnectListener listener : disconnectListeners) {
				listener.onDisconnect(client);
			}
		} catch (Exception e) {
			exceptionListener.onDisconnectException(e, client);
		}
	}
	
	@Override
	public void addConnectListener(ConnectListener listener) {
		connectListeners.add(listener);
	}
	
	public void onConnect(SocketIOClient client) {
		if (roomClients.containsKey(getName()) && 
				roomClients.get(getName()).contains(client.getSessionId())) {
			return;
		}
		
		join(getName(), client.getSessionId());
		storeFactory.pubSubStore().publish(PubSubType.JOIN, new JoinLeaveMessage(client.getSessionId(), getName(), getName()));
	
		try {
			for (ConnectListener listener : connectListeners) {
				listener.onConnect(client);
			}
		} catch (Exception e) {
			exceptionListener.onConnectException(e, client);
		}
	} 

	@Override
	public void addPingListener(PingListener listener) {
		pingListeners.add(listener);
	}

	@Override
	public void addPongListener(PongListener listener) {
		pongListeners.add(listener);
	}
	
	public void onPing(SocketIOClient client) {
		try {
			for (PingListener listener : pingListeners) {
				listener.onPing(client);
			}
		} catch (Exception e) {
			exceptionListener.onPingException(e, client);
		}
 	}

	public void onPong(SocketIOClient client) {
		try {
			for (PongListener listener : pongListeners) {
				listener.onPong(client);
			}
		} catch (Exception e) {
			exceptionListener.onPongException(e, client);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addListeners(Object listeners) {
		if (listeners instanceof Iterable) {
			addListeners((Iterable<? extends Object>) listeners);
			return;
		}
		addListeners(listeners, listeners.getClass());
	}

	@Override
	public <L> void addListeners(Iterable<L> listeners) {
		for (L next : listeners) {
			addListeners(next, next.getClass());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addListeners(Object listeners, Class<?> listenersClass) {
		if (listeners instanceof Iterable) {
			addListeners((Iterable<? extends Object>) listeners);
			return;
		}
		engine.scan(this, listeners, listenersClass);
	}
	
	public void joinRoom(String room, UUID sessionId) {
		join(room, sessionId);
		storeFactory.pubSubStore().publish(PubSubType.JOIN, new JoinLeaveMessage(sessionId, room, getName()));
	} 
	
	public void joinRooms(Set<String> rooms, UUID sessionId) {
		for (String room : rooms) {
			join(room, sessionId);
		}
		storeFactory.pubSubStore().publish(PubSubType.BULK_JOIN, new BulkJoinLeaveMessage(sessionId, rooms, getName()));
	} 
	
	public void dispatch(String room, Packet packet) {
		Iterable<SocketIOClient> clients = getRoomClients(room);
		for (SocketIOClient client : clients) {
			client.send(packet);
		}
	}
	
	private <K, V> void join(ConcurrentMap<K, Set<V>> map, K key, V value) {
		Set<V> clients = map.get(key);
		if (clients == null) {
			clients = Collections.newSetFromMap(PlatformDependent.<V, Boolean>newConcurrentHashMap());
			Set<V> oldClients = map.putIfAbsent(key, clients);
			if (oldClients != null) {
				clients = oldClients;
			}
		}
		clients.add(value);
		if (clients != map.get(key) ) {
			join(map, key, value);
		}
	}
	
	public void join(String room, UUID sessionId) {
		join(roomClients, room, sessionId);
		join(clientRooms, sessionId, room);
	}
	
	public void leaveRoom(String room, UUID sessionId) {
		leave(room, sessionId);
		storeFactory.pubSubStore().publish(PubSubType.LEAVE, new JoinLeaveMessage(sessionId, room, getName()));
	}
	
	public void leaveRooms(Set<String> rooms, final UUID sessionId) {
		for (String room : rooms) {
			leave(room, sessionId);
		}
		storeFactory.pubSubStore().publish(PubSubType.BULK_LEAVE, new BulkJoinLeaveMessage(sessionId, rooms, getName()));
	}
	
	private <K, V> void leave(ConcurrentMap<K, Set<V>> map, K room, V sessionId) {
		Set<V> clients = map.get(room);
		if (clients == null) {
			return;
		}
		clients.remove(sessionId);
		
		if (clients.isEmpty()) {
			map.remove(room, Collections.emptySet());
		}
	}
	
	public void leave(String room, UUID sessionId) {
		leave(roomClients, room, sessionId);
		leave(clientRooms, sessionId, room);
	}
	
	public Set<String> getRooms(SocketIOClient client) {
		Set<String> res = clientRooms.get(client.getSessionId());
		if (res == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(res);
	}
	
	public Set<String> getRooms() {
		return roomClients.keySet();
	}
	
	public Iterable<SocketIOClient> getRoomClients(String room) {
		Set<UUID> sessionIds = roomClients.get(room);
		
		if (sessionIds == null) {
			return Collections.emptyList();
		}
		
		List<SocketIOClient> result = new ArrayList<SocketIOClient>();
	
		for (UUID sessionId : sessionIds) {
			SocketIOClient client = allClients.get(sessionId);
			if (client != null) {
				result.add(client);
			}
		}
		return result;
	}
	
	public int getRoomClientsInCluster(String room) {
		Set<UUID> sessionIds = roomClients.get(room);
		return sessionIds == null ? 0 : sessionIds.size();
	}
 	
	@Override
	public void removeAllListeners(String eventName) {
		EventEntry<?> entry = eventListeners.remove(eventName);
		if (entry != null) {
			jsonSupport.removeEventMapping(name, eventName);
		}
	}

	@Override
	public BroadcastOperations getBroadcastOperations() {
		return new SingleRoomBroadcastOperations(getName(), getName(), allClients.values(), storeFactory);
	}

	@Override
	public BroadcastOperations getRoomOperations(String room) {
		return new SingleRoomBroadcastOperations(getName(), room, getRoomClients(room), storeFactory);
	}

	@Override
	public BroadcastOperations getRoomOperations(String... rooms) {
		List<BroadcastOperations> list = new ArrayList<>();
		for (String room : rooms) {
			list.add(new SingleRoomBroadcastOperations(getName(), room, getRoomClients(room), storeFactory));
		}
		return new MultiRoomBroadcastOperations(list);
	}

	@Override
	public Collection<SocketIOClient> getAllClients() {
		return Collections.unmodifiableCollection(allClients.values());
	}

	@Override
	public SocketIOClient getClient(UUID uuid) {
		return allClients.get(uuid);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Namespace other = (Namespace) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)){ 
			return false;
		}
		return true;
	}
	
	@Override
	public void addAuthTokenListener(AuthTokenListener listener) {
		this.authDataInterceptors.add(listener);
	}
	
	public AuthTokenResult onAuthData(SocketIOClient client, Object authData) {
		try {
			for (AuthTokenListener listener : authDataInterceptors) {
				final AuthTokenResult result = listener.getAuthTokenResult(authData, client);
				if (!result.isSuccess()) {
					return result;
				}
			}
			return AuthTokenResult.AuthTokenResultSuccess;
		} catch (Exception e) {
			exceptionListener.onAuthException(e, client);
		}
		return new AuthTokenResult(false, "Internal error");
	}

}
