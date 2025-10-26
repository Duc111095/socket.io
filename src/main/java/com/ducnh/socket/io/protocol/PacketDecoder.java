package com.ducnh.socket.io.protocol;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import java.net.URLDecoder;

public class PacketDecoder {

	private final UTF8CharsScanner utf8scanner = new UTF8CharsScanner();
	
	private final ByteBuf QUOTES = Unpooled.copiedBuffer("\"", CharsetUtil.UTF_8);

	private final JsonSupport jsonSupport;
	private final AckManager ackManager;
	
	public PacketDecoder(JsonSupport jsonSupport, AckManager ackManager) {
		this.jsonSupport = jsonSupport;
		this.ackManager = ackManager;
	}
	
	private boolean isStringPacket(ByteBuf content) {
		return content.getByte(content.readerIndex()) == 0x0;
	} 
	
	public ByteBuf preprocessJson(Integer jsonIndex, ByteBuf content) throws IOException {
		String packet = URLDecoder.decode(content.toString(CharsetUtil.UTF_8), CharsetUtil.UTF_8.name());
		
		if (jsonIndex != null) {
			/**
			 * double escaping is required for escaped new lines because unescaping of new lines can be done safely on server-side. 
			 */
			packet = packet.replace("\\\\n", "\\n");
			
			// skip "d="
			packet = packet.substring(2);
		}
		
		return Unpooled.wrappedBuffer(packet.getBytes(CharsetUtil.UTF_8));
	}
	
	// fastest way to parse chars to int
	private long readLong(ByteBuf chars, int length) {
		long result = 0;
		for (int i = chars.readerIndex(); i < chars.readerIndex() + length; i++) {
			int digit = ((int) chars.getByte(i) & 0xF);
			for (int j = 0; j < chars.readerIndex() + length-1-i; j++) {
				digit*=10;
			}
			result += digit;
		}
		chars.readerIndex(chars.readerIndex() + length);
		return result;
	}
	
	private PacketType readType(ByteBuf buffer) {
		int typeId = buffer.readByte() & 0xF;
		return PacketType.valueOf(typeId);
	}
	
	private PacketType readInnerType(ByteBuf buffer) {
		int typeId = buffer.readByte() & 0xF;
		return PacketType.valueOfInner(typeId);
	}
	
	private boolean hasLengthHeader(ByteBuf buffer) {
		for (int i = 0; i < Math.min(buffer.readableBytes(), 10); i++) {
			byte b = buffer.getByte(buffer.readableBytes() + i);
			if (b == (byte)':' && i > 0) {
				return true;
			}
			if (b > 57 || b < 48) {
				return false;
			}
		}
		return false;
	}
	
	public Packet decodePackets(ByteBuf buffer, ClientHead client) throws IOException {
		if (isStringPacket(buffer)) {
			// TODO refactor
			int maxLength = Math.min(buffer.readableBytes(), 10);
			int headEndIndex = buffer.bytesBefore(maxLength, (byte)-1);
			if (headEndIndex == -1) {
				headEndIndex = buffer.bytesBefore(maxLength, (byte)0x3f);
			}
			int len = (int) readLong(buffer, headEndIndex);
			
			ByteBuf frame = buffer.slice(buffer.readerIndex() + 1, len);
			// skip this frame
			buffer.readerIndex(buffer.readerIndex() + 1 + len);
			return decode(client, frame);
		} else if (hasLengthHeader(buffer)) {
			// TODO refactor
			int lengthEndIndex = buffer.bytesBefore((byte) ':');
			int lenHeader = (int) readLong(buffer, lengthEndIndex);
			int len = utf8scanner.getActualLength(buffer, lenHeader);
			
			ByteBuf frame = buffer.slice(buffer.readerIndex() + 1, len);
			// skip this frame
			buffer.readerIndex(buffer.readerIndex() + 1 + len);
			return decode(client, frame);
		}
		
		return decode(client, buffer);
	}
	
	private String readString(ByteBuf frame) {
		return readString(frame, frame.readableBytes());
	}
	
	private String readString(ByteBuf frame, int size) {
		byte[] bytes = new byte[size];
		frame.readBytes(bytes);
		return new String(bytes, CharsetUtil.UTF_8);
	}
	
	private Packet decode(ClientHeader head, ByteBuf frame) throws IOException {
		Packet lastPacket = head.getLastBinaryPacket();
		// Assume attachments follow.
		if (lastPacket != null) {
			if (lastPacket.hasAttachments() && !lastPacket.isAttachmentsLoaded()) {
				return addAttachment(head, frame, lastPacket);
			}
		}
		
		final int separatorPos = frame.bytesBefore((byte) 0x1E);
		final ByteBuf packetBuf;
		if (separatorPos > 0) {
			// Multiple packets in one, copy out the next packet to parse
			packetBuf = frame.copy(frame.readerIndex(), separatorPos);
			frame.skipBytes(separatorPos + 1);
		} else {
			packetBuf = frame;
		}
		
		PacketType type = readType(packetBuf);
		Packet packet = new Packet(type, head.getEngineIOVersion());
		
		if (type == PacketType.PING) {
			packet.setData(readString(packetBuf));
			return packet;
		}
		
		if (!packetBuf.isReadable()) {
			return packet;
		}
		
		PacketType innerType = readInnerType(packetBuf);
		packet.setSubType(innerType);
		
		parseHeader(packetBuf, packet, innerType);
		parseBody(head, packetBuf, packet);
		return packet;
	}
	
	private void parseHeader(ByteBuf frame, Packet packet, PacketType innerType) {
		int endIndex = frame.bytesBefore((byte)'[');
		if (endIndex <= 0) {
			return;
		}
		
		int attachmentsDividerIndex = frame.bytesBefore(endIndex, (byte)'-');
		boolean hasAttachments = attachmentsDividerIndex != -1;
		if (hasAttachments && (PacketType.BINARY_EVENT.equals(innerType)
				|| PacketType.BINARY_ACK.equals(innerType))) {
			int attachments = (int) readLong(frame, attachmentsDividerIndex);
			packet.initAttachments(attachments);
			frame.readerIndex(frame.readerIndex() + 1);
			
			endIndex -= attachmentsDividerIndex + 1;
		} 
		if (endIndex == 0) {
			return;
		}
		
		// TODO optimize
		boolean hasNsp = frame.bytesBefore(endIndex, (byte)',') != -1;
		if (hasNsp) {
			String nspAckId = readString(frame, endIndex);
			String[] parts = nspAckId.split(",");
			String nsp = parts[0];
			packet.setNsp(nsp);
			if (parts.length > 1) {
				String ackId = parts[1];
				packet.setAckId(Long.valueOf(ackId));
			}
		} else {
			long ackId = readLong(frame, endIndex);
			packet.setAckId(ackId);
		}
	}
	
}
