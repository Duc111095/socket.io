package com.ducnh.socket.io;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.HttpHeaders;

public class HandshakeData implements Serializable{

	private static final long serialVersionUID = -8903479016341138093L;

	private HttpHeaders headers;
	private InetSocketAddress address;
	private Date time = new Date();
	private InetSocketAddress local;
	private String url;
	private Map<String, List<String>> urlParams;
	private boolean xdomain;
	private Object authToken;
	
	public HandshakeData() {
		
	}
	
	public HandshakeData(HttpHeaders headers, Map<String, List<String>> urlParams, InetSocketAddress address, String url, boolean xdomain) {
		this(headers, urlParams, address, null, url,xdomain);
	}
	
	public HandshakeData(HttpHeaders headers, Map<String, List<String>> urlParams, InetSocketAddress address, InetSocketAddress local, String url, boolean xdomain) {
		super();
		this.headers = headers;
		this.urlParams = urlParams;
		this.address = address;
		this.local = local;
		this.url = url;
		this.xdomain = xdomain;
	}
	
	public InetSocketAddress getAddress() {
		return address;
	}
	
	public InetSocketAddress getLocal() {
		return local;
	}
	
	public HttpHeaders getHttpHeaders() {
		return headers;
	}
	
	public Date getTime( ) {
		return time;
	}
	
	public String getUrl() {
		return url;
	}
	
	public boolean isXdomain() {
		return xdomain;
	}
	
	public Map<String, List<String>> getUrlParams() {
		return urlParams;
	}
	
	public String getSingleUrlParam(String name) {
		List<String> values = urlParams.get(name);
		if (values != null && values.size() == 1) {
			return values.iterator().next();
		}
		return null;
	}
	
	public void setAuthToken(Object token) {
		this.authToken = token;
	}
	
	public Object getAuthToken() {
		return this.authToken;
	}
}

