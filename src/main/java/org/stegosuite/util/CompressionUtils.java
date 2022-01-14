package org.stegosuite.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class CompressionUtils {

	/**
	 * Private constructor to hide the publicly implicit one
	 */
	private CompressionUtils() {}

	/**
	 * Compresses a byte array using deflate
	 *
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(byte[] data)
			throws IOException {
		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

		try (ByteArrayInputStream in = new ByteArrayInputStream(data);
				DeflaterOutputStream outDeflate = new DeflaterOutputStream(outBytes)) {
			ByteUtils.copy(in, outDeflate);
			outBytes.close();
		}

		return outBytes.toByteArray();
	}

	/**
	 * Decompresses a byte array using inflate
	 *
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static byte[] decompress(byte[] data)
			throws IOException {
		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

		try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data))) {
			ByteUtils.copy(in, outBytes);
			outBytes.close();
		}

		return outBytes.toByteArray();
	}

}
