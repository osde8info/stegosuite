package org.stegosuite.model.payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.model.exception.SteganoEncryptionException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoKeyException;
import org.stegosuite.util.ByteUtils;
import org.stegosuite.util.CompressionUtils;
import org.stegosuite.util.CryptoUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PayloadExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(PayloadExtractor.class);

	/**
	 * The payload instance that should be populated with the extracted data
	 */
	private Payload payload = null;

	/**
	 * Array of 4 bytes containing the total message length
	 */
	private byte[] dataSizeBytes = new byte[] { 0, 0, 0, 0 };

	/**
	 *
	 */
	private Integer payloadLength = null;

	/**
	 * Buffer that holds the bytes that are extracted after reading the message length
	 */
	private byte[] data = new byte[0];

	/**
	 * Current position of the dataSize array
	 */
	private int dataSizePos = 4 - Payload.LENGTH_NUM_BYTES;

	/**
	 * Buffer that holds the bits
	 */
	private byte[] bits = new byte[8];

	/**
	 * Current position of the bit
	 */
	private int dataSizeBitPos = 0;

	/**
	 * Current position of the data array
	 */
	private int dataPos = 0;

	/**
	 * Constructor
	 *
	 * @param payload The payload instance that should be populated with the extracted data
	 */
	public PayloadExtractor(Payload payload) {
		this.payload = payload;
	}

	/**
	 * Signals whether the reader has processed all the data. If finished() returns true, (optional)
	 * decryption and decompression is applied and the payload instance is fed with the extracted
	 * data
	 *
	 * @return true if the payload instance is successfully populated with the extracted data
	 * @throws SteganoExtractException
	 */
	public boolean finished()
			throws SteganoExtractException {

		boolean finished = dataPos > 0 && dataPos == data.length;

		if (finished && dataSizeBytes != null) {
			LOG.debug("Unpacking payload from {} extracted bytes", ByteBuffer.wrap(dataSizeBytes).getInt());

			byte[] payloadBytes = Arrays.copyOfRange(data, 0, data.length);

			if (payload.getEncryptionPassword() == null) {
				payload.setEncryptionPassword("");
			}

			try {
				payloadBytes = CryptoUtils.decrypt(payloadBytes, payload.getEncryptionPassword());
			} catch (Exception e) {
				LOG.debug(e.getMessage());
				throw new SteganoEncryptionException("Wrong decryption password.");
			}

			try {
				payloadBytes = CompressionUtils.decompress(payloadBytes);
			} catch (IOException e) {
				throw new SteganoKeyException();
			}

			LOG.debug("Unpacked {} bytes of payload", payloadBytes.length);

			payload.unpack(payloadBytes);

			dataSizeBytes = null;
		}

		return finished;
	}

	/**
	 * Processes the bytes extracted from a steganogram and stores them in the internal buffer
	 *
	 * @param b
	 * @throws SteganoExtractException
	 */
	public void processByte(byte b)
			throws SteganoExtractException {

		if (finished()) {
			return;
		}

		if (dataSizeBitPos != 0) {
			throw new SteganoExtractException("Cannot process byte while processing bits");
		}

		if (dataSizePos < 4) {
			dataSizeBytes[dataSizePos] = b;
			if (dataSizePos == 3) {
				// The bytes containing the payload length have been processed,
				// initialize main buffer for the encryption flag and the whole
				// payload.
				payloadLength = ByteBuffer.wrap(dataSizeBytes).getInt() - Payload.LENGTH_NUM_BYTES;

				// If (dataSize minus encryption flag byte) is negative it's a
				// strong indicator that the stegano password was wrong
				if (payloadLength - 1 < 0) {
					throw new SteganoKeyException();
				}

				LOG.debug("Payload of {} bytes to be extracted", payloadLength + Payload.LENGTH_NUM_BYTES);

				data = new byte[payloadLength];
				dataPos = 0;
			}
			dataSizePos++;
		} else {
			data[dataPos++] = b;
		}
	}

	/**
	 * Processes the bits extracted from a steganogram and stores them in the internal buffer
	 *
	 * @param bit
	 * @throws SteganoExtractException
	 */
	public void processBit(byte bit)
			throws SteganoExtractException {

		if (finished()) {
			return;
		}

		bits[dataSizeBitPos] = bit;

		if ((dataSizeBitPos = ++dataSizeBitPos % 8) == 0) {
			processByte(ByteUtils.bitsToByte(bits, Payload.BYTE_ORDER));
		}
	}

	/**
	 * Returns the total number of bytes that the extractor needs to process. Note that this method
	 * returns {@link null} if called until the extractor has processed at least
	 * Payload.LENGTH_NUM_BYTES bytes.
	 *
	 * @return
	 */
	public Integer getPayloadLength() {
		return payloadLength;
	}

	/**
	 * Returns the number of currently processed bytes
	 *
	 * @return
	 */
	public int getProcessedBytesCount() {
		return dataPos + dataSizePos - Payload.LENGTH_NUM_BYTES;
	}
}
