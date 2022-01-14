package org.stegosuite.image.jpgtemp.net.f5;

import org.stegosuite.image.jpgtemp.james.JpegEncoder;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Embed {

	public static void main(final String args[]) {
		Image image = null;
		FileOutputStream dataOut = null;
		File file, outFile;
		JpegEncoder jpg;
		int Quality = 80;
		// Check to see if the input file name has one of the extensions:
		// .tif, .gif, .jpg
		// If not, print the standard use info.
		String embFileName = null;
		String comment = "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ";
		String password = "abc123";
		String inFileName = null;
		String outFileName = null;

		outFile = new File(outFileName);
		while (outFile.exists()) {
			outFile = new File(outFileName);

		}
		file = new File(inFileName);
		if (file.exists()) {
			try {
				dataOut = new FileOutputStream(outFile);
			} catch (final IOException e) {}

			image = Toolkit.getDefaultToolkit().getImage(inFileName);

			jpg = new JpegEncoder(image, Quality, dataOut, comment);
			try {
				jpg.Compress(new FileInputStream(embFileName), password);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			try {
				dataOut.close();
			} catch (final IOException e) {}
		} else {
			System.out.println("I couldn't find " + inFileName + ". Is it in another directory?");
		}
	}
}
