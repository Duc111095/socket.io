package com.ducnh.socket.io;

import java.util.Collections;
import java.util.Map;

public class AuthorizationResult {
	
	public static final AuthorizationResult SUCCESSFUL_AUTHORIZATION = new AuthorizationResult(true);
	public static final AuthorizationResult FAILED_AUTHORIZATION = new AuthorizationResult(false);
	private final boolean isAuthorized;
	private final Map<String, Object> storeParams;
	
	public AuthorizationResult(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
		this.storeParams = Collections.emptyMap(); 
	}
	
	public AuthorizationResult(boolean isAuthorized, Map<String, Object> storeParams) {
		this.isAuthorized = isAuthorized;
		this.storeParams = isAuthorized && storeParams != null ?
				Collections.unmodifiableMap(storeParams) : Collections.emptyMap();
	}
	
	public boolean isAuthorized() {
		return isAuthorized;
	}
	
	public Map<String, Object> getStoreParams() {
		return storeParams;
	}
}
