package com.ducnh.socket.io.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ducnh.socket.io.namespace.Namespace;

public class OnConnectScanner implements AnnotationScanner{

	@Override
	public Class<? extends Annotation> getScanAnnotation() {
		return OnConnect.class;
	}

	@Override
	public void addListener(Namespace namespace, Object object, Method method, Annotation annotation) {
		namespace.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				try {
					method.invoke(object, client);
				} catch (InvocationTargetException e) {
					throw new SocketIOException(e.getCause());
				} catch (Exception e) {
					throw new SocketIOException(e);
				}
			}
		});
	}

	@Override
	public void validate(Method method, Class<?> clazz) {
		if (method.getParameterTypes().length != 1) {
			throw new IllegalArgumentException("Wrong OnConnect listener signature: " + clazz + "." + method.getName());
		}
		
		for (Class<?> eventType : method.getParameterTypes()) {
			if (SocketIOClient.class.equals(eventType)) {
				return;
			}
		}
		throw new IllegalArgumentException("Wrong OnConnect listener signature: " + clazz + "." + method.getName());
	}

}
