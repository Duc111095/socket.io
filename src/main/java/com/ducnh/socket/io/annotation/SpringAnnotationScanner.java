package com.ducnh.socket.io.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

public class SpringAnnotationScanner implements BeanPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(SpringAnnotationScanner.class);
	
	private final List<Class<? extends Annotation>> annotations =
				Arrays.asList(OnConnect.class, OnDisconnect.class, OnEvent.class);

	private final SocketIOServer socketIOServer;
	
	private Class originalBeanClass;
	
	private Object originalBean;
	
	private String originalBeanName;
	
	public SpringAnnotationScanner(SocketIOServer socketIOServer) {
		super();
		this.socketIOServer = socketIOServer;
	}
	
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (originalBeanClass != null) {
			socketIOServer.addListeners(originalBean, originalBeanClass);
			log.info("{} bean listeners added", originalBeanName);
			originalBeanClass = null;
			originalBeanName = null;	
		}
		return bean;
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throw BeansException {
		final AtomicBoolean add = new AtomicBoolean();
		ReflectionUtils.doWithMethods(bean.getClass(), 
				new MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				add.set(true);
			}
		}, 
		new MethodFilter() {
			@Override
			public boolean matches(Method method) {
				for (Class<? extends Annotation> annotationClass : annotations) {
					if (method.isAnnotationPresent(annotationClass)) {
						return true;
					}
				}
				return false;
			}
		});
		
		if (add.get()) {
			originalBeanClass = bean.getClass();
			originalBean = bean;
			originalBeanName = beanName;
		}
		return bean;
	}
}
