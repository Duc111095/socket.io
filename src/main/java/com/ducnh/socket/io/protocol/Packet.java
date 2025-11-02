package com.ducnh.socket.io.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ducnh.socket.io.namespace.Namespace;

import io.netty.buffer.ByteBuf;

public class Packet implements Serializable {

	private static final long serialVersionUID = 941638241065905869L;

	private PacketType type;
	private EngineIOVersion engineIOVersion;
	private PacketType subType;
	private Long ackId;
	private String name;
	private String nsp = Namespace.DEFAULT_NAME;
	private Object data;
	
	private ByteBuf dataSource;
	private int attachmentsCount;
	private List<ByteBuf> attachments = Collections.emptyList();
	
	protected Packet() {
		
	}
	
	// only for test
	public Packet(PacketType type) {
		super();
		this.type = type;
	}
	
	public Packet(PacketType type, EngineIOVersion engineIOVersion) {
		this(type);
		this.engineIOVersion = engineIOVersion;
	}
	
	public PacketType getSubType() {
		return subType;
	}
	
	public void setSubType(PacketType subType) {
		this.subType = subType;
	}
	
	public PacketType getType() {
		return type;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData() {
		return (T) data;
	}
	
	public Packet withNsp(String namespace, EngineIOVersion engineIOVersion) {
		if (this.nsp.equalsIgnoreCase(namespace)) {
			return this;
		} else {
			Packet newPacket = new Packet(this.type, engineIOVersion);
			newPacket.setAckId(this.ackId);
			newPacket.setData(this.data);
			newPacket.setDataSource(this.dataSource);
			newPacket.setName(this.name);
			newPacket.setSubType(this.subType);
			newPacket.setNsp(namespace);
			newPacket.attachments = this.attachments;
			newPacket.attachmentsCount = this.attachmentsCount;
			return newPacket;
		}
	}
	
	public void setNsp(String endpoint) {
		if(endpoint.equals("{}")) {
			endpoint = "";
		}
		this.nsp = endpoint;
	}
	
	public String getNsp() {
		return nsp;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Long getAckId() {
		return ackId;
	}
	
	public void setAckId(Long ackId) {
		this.ackId = ackId;
	}
	
	public boolean isAckRequested() {
		return getAckId() != null;
	}
	
	public void initAttachments(int attachmentsCount) {
		this.attachmentsCount = attachmentsCount;
		this.attachments = new ArrayList<>();
	}
	
	public void addAttachments(ByteBuf attachment) {
		if (this.attachments.size() < attachmentsCount) {
			this.attachments.add(attachment);
		}
	}
	public List<ByteBuf> getAttachments() {
		return attachments;
	}
	
	public boolean hasAttachments() {
		return attachmentsCount != 0;
	}
	public boolean isAttachmentsLoaded() {
		return this.attachments.size() == attachmentsCount;
	}
	
	public ByteBuf getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(ByteBuf dataSource) {
		this.dataSource = dataSource;
	}
	
	public EngineIOVersion getEngineIOVersion() {
		return engineIOVersion;
	}
	
	public void setEngineIOVersion(EngineIOVersion engineIOVersion) {
		this.engineIOVersion = engineIOVersion;
	}
	
	@Override
	public String toString() {
		return "Packet [type=" + type + ", ackId=" + ackId + "]";
	}
}

