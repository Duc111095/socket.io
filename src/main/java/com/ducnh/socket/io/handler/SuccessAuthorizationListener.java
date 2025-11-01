package com.ducnh.socket.io.handler;

import com.ducnh.socket.io.AuthorizationListener;
import com.ducnh.socket.io.AuthorizationResult;
import com.ducnh.socket.io.HandshakeData;

public class SuccessAuthorizationListener implements AuthorizationListener{

	@Override
	public AuthorizationResult getAuthorizationResult(HandshakeData data) {
		return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
	}

}
