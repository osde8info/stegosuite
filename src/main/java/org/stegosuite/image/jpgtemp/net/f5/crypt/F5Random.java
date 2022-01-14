package org.stegosuite.image.jpgtemp.net.f5.crypt;

import sun.security.provider.SecureRandom;

public class F5Random {

	private SecureRandom random = null;

	private byte[] b = null;

	public F5Random(final byte[] password) {
		this.random = new SecureRandom();
		this.random.engineSetSeed(password);
		this.b = new byte[1];
	}

	// get a random byte
	public int getNextByte() {
		this.random.engineNextBytes(this.b);
		return this.b[0];
	}

	// get a random integer 0 ... (maxValue-1)
	public int getNextValue(final int maxValue) {
		int retVal = getNextByte() | getNextByte() << 8 | getNextByte() << 16 | getNextByte() << 24;
		retVal %= maxValue;
		if (retVal < 0) {
			retVal += maxValue;
		}
		return retVal;
	}
}
