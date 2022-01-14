package org.stegosuite.model.payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.model.payload.block.Block;
import org.stegosuite.util.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is the outer-most wrapper of the data that the user wants to embed or extract.
 */
public class Payload {

	private static final Logger LOG = LoggerFactory.getLogger(Payload.class);

	/**
	 * The way processed bits are embedded and extracted, should be either ByteOrder.LITTLE_ENDIAN
	 * if LSB is at index 0, or ByteOrder.BIG_ENDIAN if LSB is at index 7
	 */
	public static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

	/**
	 * The number bytes that contain the payload size. Valid values: 1 = 256 bytes total payload
	 * length; 2 = 64 KB total payload length; 3 = 16MB total payload length; 4 = 4 GB total payload
	 * length;
	 */
	public static final int LENGTH_NUM_BYTES = 3;

	/**
	 * Default value if no stegano key is provided by user
	 */
	private String steganoPassword = "";

	/**
	 * List of all blocks that are embedded/extracted
	 */
	private List<Block> blocks = new ArrayList<>();

	/**
	 * User-provided password for encryption and decryption. If it's null at embedding time, the
	 * payload will not be encrypted. If it is null at extraction time, it signals that the payload
	 * is assumed to be not encrypted. If the payload turns out to be encrypted but no encryption
	 * password is set, an exception is thrown.
	 */
	private String encryptionPassword = null;

	/**
	 * Packs all blocks into a continuous byte stream (as in serialize)
	 *
	 * @return
	 */
	public byte[] pack() {
		byte[][] blocksData = new byte[blocks.size()][];
		for (int i = 0; i < blocks.size(); i++) {
			byte[] blockData = blocks.get(i).pack();
			byte[] blockSize = ByteUtils.intToBytes(blockData.length);
			byte[] blockTypeFlag = { blocks.get(i).getIdentifier() };
			blocksData[i] = ByteUtils.concat(blockTypeFlag, blockSize, blockData);
		}
		return ByteUtils.concat(blocksData);
	}

	/**
	 * Parses the byte stream and creates the original block instances (as in unserialize)
	 * Requires the block classes to have an empty constructor
	 *
	 * @param blocksData
	 */
	public void unpack(byte[] blocksData) {
		int i = 0;
		while (i < blocksData.length) {
			byte blockTypeFlag = blocksData[i];
			int blockSize = ByteBuffer.wrap(blocksData, i + 1, 4).getInt();
			byte[] blockData = Arrays.copyOfRange(blocksData, i + 5, i + 5 + blockSize);

			Class<? extends Block> blockClass = Block.getBlockClass(blockTypeFlag);
			try {
				Block block = blockClass.getConstructor().newInstance();
				block.unpack(blockData);
				blocks.add(block);
			} catch (Exception e) {
				LOG.error("Error unpacking data.", e);
			}

			i += 5 + blockSize;
		}
	}

	public String getSteganoPassword() {
		return steganoPassword;
	}

	public void setSteganoPassword(String steganoPassword) {
		this.steganoPassword = steganoPassword;
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public String getEncryptionPassword() {
		return encryptionPassword;
	}

	public void setEncryptionPassword(String encryptionPassword) {
		this.encryptionPassword = encryptionPassword;
	}

	public void removeBlock(Block aBlock) {
		blocks.remove(aBlock);
	}

	public void setPassword(String password) {
		setSteganoPassword(password);
		setEncryptionPassword(password);
	}

	public void addBlock(Block block) {
		getBlocks().add(block);
	}

	public Block getBlock(int blockIndex) {
		return getBlocks().get(blockIndex);
	}

	public boolean hasNoBlocks() {
		return getBlocks().isEmpty();
	}
}
