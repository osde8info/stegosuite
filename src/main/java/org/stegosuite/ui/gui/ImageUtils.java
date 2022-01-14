package org.stegosuite.ui.gui;

public class ImageUtils {

	// taken from http://stackoverflow.com/a/24805871
	public static String formatSize(long v) {
		if (v < 1024) {
			return v + " B";
		}
		int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
		return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
	}
}
