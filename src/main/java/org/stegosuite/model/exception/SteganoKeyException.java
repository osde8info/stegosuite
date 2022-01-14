package org.stegosuite.model.exception;

public class SteganoKeyException
		extends SteganoExtractException {

	private static final long serialVersionUID = 1L;

	public SteganoKeyException() {
		super("Wrong stego-password.");
	}

	public SteganoKeyException(String message) {
		super(message);
	}

}
