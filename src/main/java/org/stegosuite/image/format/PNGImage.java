package org.stegosuite.image.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

/**
 * Represent PNG-images and provides methods to manipulate PNG pixels
 *
 */
public class PNGImage
		extends ImageFormat {

	private static final Logger LOG = LoggerFactory.getLogger(PNGImage.class);

	public static final String FILE_EXTENSION = "png";

	@Override
	public String getFileExtension() {
		return FILE_EXTENSION;
	}

	@Override
	public void setBufferedImage(BufferedImage image) {
		super.setBufferedImage(image);
		LOG.debug("Width, height, type: {}*{}*{}", getWidth(), getHeight(), this.image.getType());
	}
}
