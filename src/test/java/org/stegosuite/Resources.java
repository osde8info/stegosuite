package org.stegosuite;

import java.io.File;
import java.net.URL;

public class Resources {
	public static String pathOf(String fileName) {
		URL resourceURL = Resources.class.getClassLoader().getResource(fileName);
		if (resourceURL == null)
			throw new RuntimeException("The resource " + fileName + " does not exist");

		return resourceURL.getPath();
	}

	static void delete(String filename) {
		try {
			new File(pathOf(filename)).delete();
		} catch (Exception ignored) {}
	}
}
