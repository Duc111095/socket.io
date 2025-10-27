package com.ducnh.socket.io.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.ducnh.socket.io.namespace.Namespace;

public interface AnnotationScanner {

	Class<? extends Annotation> getScanAnnotation();
	
	void addListener(Namespace namespace, Object object, Method method, Annotation annotation);
	
	void validate(Method method, Class<?> clazz);
}
