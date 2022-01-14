package org.stegosuite.model.payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.util.ByteUtils;
import org.stegosuite.util.CompressionUtils;
import org.stegosuite.util.CryptoUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Arrays;

public class PayloadEmbedder {

	private static final Logger LOG = LoggerFactory.getLogger(PayloadEmbedder.class);

	private static final DecimalFormat DECIMAL = new DecimalFormat("#0.000");

	/**
	 * The raw data stream that should be embedded into the image
	 */
	private byte[] data = null;

	/**
	 * Constructor
	 * @param payload The payload instance that hold the data to be embedded
	 * @param capacity capacity according to the embeddingMethod
	 * @throws SteganoEmbedException
	 */
	public PayloadEmbedder(Payload payload, int capacity)
			throws SteganoEmbedException {

		if (payload.hasNoBlocks()) {
			throw new SteganoEmbedException("No data to embed");
		}

		byte[] packed = payload.pack();

		LOG.debug("Packing {} bytes of payload", packed.length);

		try {
			packed = CompressionUtils.compress(packed);
		} catch (IOException e) {
			throw new SteganoEmbedException("Error while compressing data");
		}

		if (payload.getEncryptionPassword() == null) {
			payload.setEncryptionPassword("");
		}

		try {
			packed = CryptoUtils.encrypt(packed, payload.getEncryptionPassword());
		} catch (Exception e) {
			throw new SteganoEmbedException(e.getMessage());
		}

		// Prepend header
		data = ByteUtils.concat(new byte[Payload.LENGTH_NUM_BYTES], packed);

		// Check if we crossed the maximum supported payload size
		int maxPayloadLength = (int) Math.pow(2, Payload.LENGTH_NUM_BYTES * 8);
		if (data.length > maxPayloadLength) {
			throw new SteganoEmbedException(String.format("Payload is too large. Limit is %d bytes but got %d bytes.",
					maxPayloadLength, data.length));
		}

		if (data.length > capacity) {
			throw new SteganoEmbedException(String.format(
					"Payload is too large. Maximum capacity for this carrier file is %d bytes but payload is %d bytes.",
					capacity, data.length));
		}

		// Fill header with length and encryption flag
		byte[] lengthBytes = Arrays.copyOfRange(ByteUtils.intToBytes(data.length), 4 - Payload.LENGTH_NUM_BYTES, 4);
		data = ByteBuffer.wrap(data).put(lengthBytes).array();

		Double percentage = data.length * 100.0 / capacity;
		LOG.debug("Packed payload to {} bytes, {}% of total capacity", data.length, DECIMAL.format(percentage));
	}

	/**
	 * Returns in Iterable that yields all bits of the serialized payload
	 *
	 * @return
	 */
	public Iterable<Byte> iteratePayloadBits() {
		return ByteUtils.iterateBits(data, Payload.BYTE_ORDER);
	}

	/**
	 * Retuns the compiled payload bytes that should be embedded
	 *
	 * @return
	 */
	public byte[] getPayloadBytes() {
		return data;
	}
}
