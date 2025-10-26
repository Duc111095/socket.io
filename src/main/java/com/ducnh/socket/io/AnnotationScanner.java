package com.ducnh.socket.io;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface AnnotationScanner {

	Class<? extends Annotation> getScanAnnotation();
	
	void addListener(Namespace namespace, Object object, Method method, Annotation annotation);
	
	void validate(Method method, Class<?> clazz);
}
