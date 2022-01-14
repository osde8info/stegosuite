package org.stegosuite.model.payload.block;

import java.nio.charset.StandardCharsets;

/**
 * This block contains a simple text message
 */
public final class MessageBlock
		extends Block {

	/**
	 * Unique number among all Block implementations
	 */
	public static final byte IDENTIFIER = 2;

	private String message = null;

	/**
	 * Mandatory empty constructor
	 * @see org.stegosuite.model.payload.Payload#unpack(byte[])
	 */
	@SuppressWarnings("unused")
	public MessageBlock() {}

	public MessageBlock(String message) {
		this.message = message;
	}

	@Override
	public byte getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public byte[] pack() {
		return message.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void unpack(byte[] data) {
		message = new String(data);
	}

	public String getMessage() {
		return message;
	}

}
