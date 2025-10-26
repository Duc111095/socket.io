package com.ducnh.socket.io.protocol;

import java.util.Map;
import java.util.HashMap;

/**
 * Engine.IO protocol version
 */
public enum EngineIOVersion {

	V2("2"),
	V3("3"),
	V4("4"),
	UNKNOWN(""),
	;
	
	public static final String EIO = "EIO";
	
	private static final Map<String, EngineIOVersion> VERSIONS = new HashMap<>();
	
	static {
		for (EngineIOVersion value : values()) {
			VERSIONS.put(value.getValue(), value);
		}
	}
	
	private final String value;
	
	private EngineIOVersion(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static EngineIOVersion fromValue(String value) {
		EngineIOVersion engineIOVersion = VERSIONS.get(value);
		if (engineIOVersion != null) {
			return engineIOVersion;
		}
		return UNKNOWN;
	}
}
