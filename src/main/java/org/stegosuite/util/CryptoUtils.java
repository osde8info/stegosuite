package org.stegosuite.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * Some code in this class taken from: http://stackoverflow.com/a/992413/4862922
 *
 */
public class CryptoUtils {

	private static final int SALT_LENGTH = 16;

	private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";

	/**
	 * Key lengths greater than 128 bit require JCE Unlimited Strength Jurisdiction Policy files at
	 * runtime which are a hassle to install. Available measures to circumvent are quite possible
	 * illegal.
	 */
	private static final int KEY_LENGTH = 128;

	private static final String CIPHER_ALGORITHM = "AES";

	private static final String CIPHER_MODE_PADDING_ALGORITHM = CIPHER_ALGORITHM + "/CTR/NoPadding";

	/**
	 * Can be constant because we use CTR mode and we salt the key
	 */
	private static final byte[] IV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	private static final String RANDOM_MESSAGE_DIGEST_ALGORITHM = "MD5";

	/**
	 * Encrypts a byte array
	 *
	 * @param dataToEncrypt
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(final byte[] dataToEncrypt, final String password)
			throws Exception {
		final byte[] salt = SecureRandom.getSeed(SALT_LENGTH);
		final Cipher cipher = Cipher.getInstance(CIPHER_MODE_PADDING_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, generateKey(password, salt), new IvParameterSpec(IV));
		final byte[] cipherText = cipher.doFinal(dataToEncrypt);
		return ByteUtils.concat(salt, cipherText);
	}

	/**
	 * Decrypts a byte array
	 *
	 * @param dataToDecrypt
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(final byte[] dataToDecrypt, final String password)
			throws Exception {
		final byte[] salt = Arrays.copyOfRange(dataToDecrypt, 0, SALT_LENGTH);
		final byte[] payload = Arrays.copyOfRange(dataToDecrypt, SALT_LENGTH, dataToDecrypt.length);
		final Cipher cipher = Cipher.getInstance(CIPHER_MODE_PADDING_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, generateKey(password, salt), new IvParameterSpec(IV));
		return cipher.doFinal(payload);
	}

	/**
	 *
	 * @param password
	 * @param salt
	 * @return
	 * @throws Exception
	 */
	private static SecretKey generateKey(final String password, byte[] salt)
			throws Exception {
		final int iterationCount = 65536;
		final SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, KEY_LENGTH);
		return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), CIPHER_ALGORITHM);
	}

	/**
	 * Creates a Random instance seeded with the specified key
	 * 
	 * @param key
	 */
	public static Random seededRandom(String key) {
		try {
			MessageDigest digest = MessageDigest.getInstance(RANDOM_MESSAGE_DIGEST_ALGORITHM);
			byte[] output = digest.digest(key.getBytes(StandardCharsets.UTF_8));
			long rndSeed = ByteBuffer.wrap(output).getLong();
			return new Random(rndSeed);
		} catch (NoSuchAlgorithmException e) {
			// Should never happen
		}
		return null;
	}
}
