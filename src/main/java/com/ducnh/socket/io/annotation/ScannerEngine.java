package com.ducnh.socket.io.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducnh.socket.io.namespace.Namespace;

public class ScannerEngine {
	private static final Logger log = LoggerFactory.getLogger(ScannerEngine.class);
	
	private static final List<? extends AnnotationScanner> annotations = 
			Arrays.asList(new OnConnectScanner(), new OnDisconnectScanner(), new OnEventScanner());
	
	private Method findSimilarMethod(Class<?> objectClazz, Method method) {
		Method[] methods = objectClazz.getDeclaredMethods();
		for (Method m : methods) {
			if (isEquals(m, method) ) {
				return m;
			}
		}
		
		return null;
	}
	
	public void scan(Namespace namespace, Object object, Class<?> clazz) 
			throws IllegalArgumentException {
		Method[] methods = clazz.getDeclaredMethods();
		
		if (!clazz.isAssignableFrom(object.getClass())) {
			for (Method method : methods) {
				for (AnnotationScanner annotationScanner : annotations) {
					Annotation ann = method.getAnnotation(annotationScanner.getScanAnnotation());
					if (ann != null) {
						annotationScanner.validate(method, clazz);
						
						Method m = findSimilarMethod(object.getClass(), method);
						if (m != null) {
							annotationScanner.addListener(namespace, object, m, ann);
						} else {
							log.warn("Method similar to " + method.getName() + " can't be found in " + object.getClass());;
						}
					}
				}
			}
		} else {
			for (Method method : methods) {
				for (AnnotationScanner annotationScanner : annotations) {
					Annotation ann = method.getAnnotation(annotationScanner.getScanAnnotation());
					if (ann != null) {
						annotationScanner.validate(method, clazz);
						makeAccessible(method);
						annotationScanner.addListener(namespace, object, method, ann);
					}
				}
			}
			
			if (clazz.getSuperclass() != null) {
				scan(namespace, object, clazz.getSuperclass());
			} else if (clazz.isInterface()) {
				for (Class<?> superIfc : clazz.getInterfaces()) {
					scan(namespace, object, superIfc);
				}
			}
		}
	}
	
	private boolean isEquals(Method method1, Method method2) {
		if (!method1.getName().equals(method2.getName())
			|| method1.getReturnType().equals(method2.getReturnType())) {
			return false;
		}
		
		return Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes());
	}
	
	private void makeAccessible(Method method) {
		if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) 
				&& !method.isAccessible()) {
			method.setAccessible(true);
		}
	}
}
