package org.stegosuite.model.payload.block;

import java.util.HashMap;
import java.util.Map;

/**
 * The payload is comprised of blocks. It can contain an arbitrary number of blocks. Each concrete
 * type of block must extend this class, and in addition must provide its own unique identifier.
 */
public abstract class Block {

	/**
	 * This map keeps track of all available block types
	 */
	private static final Map<Byte, Class<? extends Block>> identifiers = new HashMap<>();

	static {
		identifiers.put(FileBlock.IDENTIFIER, FileBlock.class);
		identifiers.put(MessageBlock.IDENTIFIER, MessageBlock.class);
	}

	public static Class<? extends Block> getBlockClass(byte identifier) {
		return identifiers.get(identifier);
	}

	/**
	 * Returns the individual identifier for the block type
	 * 
	 * @return
	 */
	public abstract byte getIdentifier();

	/**
	 * Packs the Block's data into a byte stream
	 * 
	 * @return
	 */
	public abstract byte[] pack();

	/**
	 * Unpacks a byte stream and populates the Block implementation's data fields
	 * 
	 * @param data
	 */
	public abstract void unpack(byte[] data);

	public boolean hasIdentifier(byte identifier) {
		return getIdentifier() == identifier;
	}
}
