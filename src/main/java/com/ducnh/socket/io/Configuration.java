package com.ducnh.socket.io;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;

import com.ducnh.socket.io.listener.DefaultExceptionListener;
import com.ducnh.socket.io.listener.ExceptionListener;
import com.ducnh.socket.io.protocol.JsonSupport;
import com.ducnh.socket.io.store.MemoryStoreFactory;
import com.ducnh.socket.io.store.StoreFactory;

import io.netty.handler.codec.http.HttpDecoderConfig;

public class Configuration {
	private ExceptionListener exceptionListener = new DefaultExceptionListener();
	
	private String context = "/socket.io";
	
	private List<Transport> transports = Arrays.asList(Transport.WEBSOCKET, Transport.POLLING);
	
	private int bossThreads = 0;
	private int workerThreads = 0;
	private boolean useLinuxNativeEpoll;
	
	private boolean allowCustomRequests = false;
	
	private int upgradeTimeout = 10000;
	private int pingTimeout = 60000;
	private int pingInterval = 25000;
	private int firstDataTimeout = 5000;
	
	private int maxHttpContentLength = 64 * 1024;
	private int maxFramePayloadLength = 64 * 1024;
	
	private String packagePrefix;
	private String hostname;
	private int port = -1;
	
	private String sslProtocol = "TLSv1";
	
	private String keyStoreFormat = "JKS";
	private InputStream keyStore;
	private String keyStorePassword;

	private String allowHeaders;
	
	private String trustStoreFormat = "JKS";
	private InputStream trustStore;
	private String trustStorePassword;
	
	private String keyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
	
	private	 boolean preferDirectBuffer = true;
	
	private SocketConfig socketConfig =  new SocketConfig();
	
	private StoreFactory storeFactory = new MemoryStoreFactory();
	
	private JsonSupport jsonSupport;
	
	private AuthorizationListener authorizationListener = new SuccessAuthorizationListener();

	private AckMode ackMode = AckMode.AUTO_SUCCESS_ONLY;
	
	private boolean addVersionHeader = true;
	
	private String origin;
	
	private boolean enableCors = true;
	
	private boolean httpCompression = true;
	
	private boolean webSocketCompression = true;
	
	private boolean randomSession = false;
	
	private boolean needClientAuth = false;

	private HttpRequestDecoderConfiguration httpRequestDecoderConfiguration = new HttpRequestDecoderConfiguration();
	
	public Configuration() {
		
	}
	
	Configuration(Configuration conf) {
		setBossThreads(conf.getBossThreads());
		setWorkerThreads(conf.getWorkerThreads());
		setUseLinuxNativeEpoll(conf.isUseLinuxNativeEpoll());
		
		setPingInterval(conf.getPingInterval());
		setPingTimeout(conf.getPingTimeout());
		setFirstDataTimeout(conf.getFirstDataTimeout());
		
		setHostname(conf.getHostname());
		setPort(conf.getPort());
		
		if (conf.getJsonSupport() == null) {
			try {
				getClass().getClassLoader().loadClass("com.fasterxml.jackson.databind.ObjectMapper");
				try {
					Class<?> jjs = getClass().getClassLoader().loadClass("com.ducnh.socket.io.protocol.JacksonJsonSupport");
					JsonSupport js = (JsonSupport) jjs.getConstructor().newInstance();
					conf.setJsonSupport(js);
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Can't find jackson lib in classpath", e);
			}
		}
		
		setJsonSupport(new JsonSupportWrapper(conf.getJsonSupport()));
		setContext(conf.getContext());
		setAllowCustomRequests(conf.isAllowCustomRequests());
		
		setKeyStorePassword(conf.getKeyStorePassword());
		setKeyStore(conf.getKeyStore());
		setKeyStoreFormat(conf.getKeyStoreFormat());
		setTrustStore(conf.getTrustStore());
		setTrustStoreFormat(conf.getTrustStoreFormat());
		setTrustStorePassword(conf.getTrustStorePassword());
		setKeyManagerFactoryAlgorithm(conf.getKeyManagerFactoryAlgorithm());
		
		setTransports(conf.getTransports().toArray(new Transport[0]));
		setMaxHttpContentLength(conf.getMaxHttpContentLength());
		setPackagePrefix(conf.getPackagePrefix());
		
		setPreferDirectBuffer(conf.isPreferDirectBuffer());
		setStoreFactory(conf.getStoreFactory());
		setAuthorizationListener(conf.getAuthorizationListener());
		setExceptionListener(conf.getExceptionListener());
		setSocketConfig(conf.getSocketConfig());
		setAckMode(conf.getAckMode());
		setMaxFramePayloadLength(conf.getMaxFramePayloadLength());
		setUpgradeTimeout(conf.getUpgradeTimeout());
		
		setAddVersionHeader(conf.isAddVersionHeader());
		setOrigin(conf.getOrigin());
		setEnableCors(conf.isEnableCors());
		setAllowHeaders(conf.getAllowHeaders());
		setSSLProtocol(conf.getSSLProtocol());
		
		setHttpCompression(conf.isHttpCompression());
		setWebsocketCompression(conf.isWebsocketCompression());
		setRandomSession(conf.randomSession);
		setNeedClientAuth(conf.isNeedClientAuth());
		setHttpRequestDecoderConfiguration(conf.getHttpRequestDecoderConfiguration());
	}
	
	public JsonSupport getJsonSupport() {
		return jsonSupport;
	}
	
	public void setJsonSupport(JsonSupport jsonSupport) {
		this.jsonSupport = jsonSupport;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getBossThreads() {
		return bossThreads;
	}
	
	public void setBossThreads(int bossThreads) {
		this.bossThreads = bossThreads;
	}
	
	public int getWorkerThreads() {
		return workerThreads;
	}
	
	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}
	
	public int getPingInterval() {
		return pingInterval;
	}
	
	public void setPingInterval(int pingInterval) {
		this.pingInterval = pingInterval;
	}
	
	public int getPingTimeout() {
		return pingTimeout;
	}

	public void setPingTimeout(int pingTimeout) {
		this.pingTimeout = pingTimeout;
	}
	
	public boolean isHeartbeatsEnabled() {
		return pingTimeout > 0;
	}
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
	public boolean isAllowCustomRequests() {
		return allowCustomRequests;
	}
	
	public void setAllowCustomRequests(boolean allowCustomRequests) {
		this.allowCustomRequests = allowCustomRequests;
	}
	
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}
	
	public String getKeyStorePassword() {
		return keyStorePassword;
	}
	
	public void setKeyStore(InputStream keyStore) {
		this.keyStore = keyStore;
	}
	
	public InputStream getKeyStore() {
		return keyStore;
	}
	
	public void setKeyStoreFormat(String keyStoreFormat) {
		this.keyStoreFormat = keyStoreFormat;
	}
	
	public String getKeyStoreFormat() {
		return keyStoreFormat;
	}
	
	public void setMaxHttpContentLength(int value) {
		this.maxHttpContentLength = value;
	}
	
	public int getMaxHttpContentLength() {
		return this.maxHttpContentLength;
	}
	
	public void setTransports(Transport ...transports) {
		if (transports.length == 0) {
			throw new IllegalArgumentException("Transports list can't be empty");
		}
		this.transports = Arrays.asList(transports);
	}
	
	public List<Transport> getTransports() {
		return transports;
	}
	
	public void setPackagePrefix(String packagePrefix) {
		this.packagePrefix = packagePrefix;
	}
	
	public String getPackagePrefix() {
		return packagePrefix;
	}
	
	public void setPreferDirectBuffer(boolean preferDirectBuffer) {
		this.preferDirectBuffer = preferDirectBuffer;
	}
	
	public boolean isPreferDirectBuffer() {
		return preferDirectBuffer;
	}
	
	public void setStoreFactory(StoreFactory storeFactory) {
		this.storeFactory = storeFactory;
	}
	
	public StoreFactory getStoreFactory() {
		return storeFactory;
	}
	
	public void setAuthorizationListener(AuthorizationListener authorizationListener) {
		this.authorizationListener = authorizationListener;
	}
	
	public AuthorizationListener getAuthorizationListener() {
		return authorizationListener;
	}
	
	public void setExceptionListener(ExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}
	
	public ExceptionListener getExceptionListener() {
		return this.exceptionListener;
	}
	
	public SocketConfig getSocketConfig() {
		return socketConfig;
	}
	
	public void setSocketConfig(SocketConfig socketConfig) {
		this.socketConfig = socketConfig;
	}
	
	public void setAckMode(AckMode ackMode) {
		this.ackMode = ackMode;
	}
	
	public AckMode getAckMode() {
		return this.ackMode;
	}
	
	public String getTrustStoreFormat() {
		return trustStoreFormat;
	}
	
	public void setTrustStoreFormat(String trustStoreFormat) {
		this.trustStoreFormat = trustStoreFormat;
	}
	
	public InputStream getTrustStore() {
		return trustStore;
	}
	
	public void setTrustStore(InputStream trustStore) {
		this.trustStore = trustStore;
	}
	
	public String getTrustStorePassword() {
		return trustStorePassword;
	}
	
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}
	
	public String getKeyManagerFactoryAlgorithm() {
		return keyManagerFactoryAlgorithm;
	}
	
	public void setKeyManagerFactoryAlgorithm(String keyManagerFactoryAlgorithm) {
		this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
	}
	
	public void setMaxFramePayloadLength(int maxFramePayloadLength) {
		this.maxFramePayloadLength = maxFramePayloadLength;
	}
	
	public int getMaxFramePayloadLength() {
		return this.maxFramePayloadLength;
	}
	
	public void setUpgradeTimeout(int upgradeTimeout) {
		this.upgradeTimeout = upgradeTimeout;
	}
	
	public int getUpgradeTimeout() {
		return this.upgradeTimeout;
	}
	
	public void setAddVersionHeader(boolean addVersionHeader) {
		this.addVersionHeader = addVersionHeader;
	}
	
	public boolean isAddVersionHeader() {
		return addVersionHeader;
	}
	
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	public String getOrigin() {
		return this.origin;
	}
	
	public void setEnableCors(boolean enableCors) {
		this.enableCors = enableCors;
	}
	
	public boolean isEnableCors() {
		return enableCors;
	}
	
	public boolean isUseLinuxNativeEpoll() {
		return useLinuxNativeEpoll;
	}
	
	public void setUseLinuxNativeEpoll(boolean useLinuxNativeEpoll) {
		this.useLinuxNativeEpoll = useLinuxNativeEpoll;
	}
	
	public void setSSLProtocol(String sslProtocol) {
		this.sslProtocol = sslProtocol;
	}
	
	public String getSSLProtocol() {
		return this.sslProtocol;
	}
	
	public void setAllowHeaders(String allowHeaders) {
		this.allowHeaders = allowHeaders;
	}
	
	public String getAllowHeaders() {
		return allowHeaders;
	}
	
	public void setFirstDataTimeout(int firstDataTimeout) {
		this.firstDataTimeout = firstDataTimeout;
	}
	
	public int getFirstDataTimeout() {
		return firstDataTimeout;
	}
	
	public void setHttpCompression(boolean httpCompression) {
		this.httpCompression = httpCompression;
	}
	
	public boolean isHttpCompression() {
		return this.httpCompression;
	}
	
	public void setWebsocketCompression(boolean webSocketCompression) {
		this.webSocketCompression = webSocketCompression;
	}
	
	public boolean isWebsocketCompression() {
		return webSocketCompression;
	}
	
	public boolean isRandomSession() {
		return randomSession;
	}
	
	public void setRandomSession(boolean randomSession) {
		this.randomSession = randomSession;
	}
	
	public void setNeedClientAuth(boolean needClientAuth) {
		this.needClientAuth = needClientAuth;
	}
	
	public boolean isNeedClientAuth() {
		return needClientAuth;
	}
	
	public HttpRequestDecoderConfiguration getHttpRequestDecoderConfiguration() {
		return httpRequestDecoderConfiguration;
	}
	
	public void setHttpRequestDecoderConfiguration(HttpRequestDecoderConfiguration httpRequestDecoderConfiguration) {
		this.httpRequestDecoderConfiguration = httpRequestDecoderConfiguration;
	}
	
	public HttpDecoderConfig getHttpDecoderConfig() {
		return new HttpDecoderConfig()
				.setMaxInitialLineLength(httpRequestDecoderConfiguration.getMaxInitialLineLength())
				.setMaxHeaderSize(httpRequestDecoderConfiguration.getMaxHeaderSize())
				.setMaxChunkSize(httpRequestDecoderConfiguration.getMaxChunkSize());
	}
}   

