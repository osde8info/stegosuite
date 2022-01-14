package org.stegosuite.image.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.util.ColorUtils;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

/**
 * Represent BMP-images and provides methods to manipulate BMP pixels
 *
 * @author alwin
 */
public class BMPImage
		extends ImageFormat {

	private static final Logger LOG = LoggerFactory.getLogger(BMPImage.class);

	public static final String FILE_EXTENSION = "bmp";

	private static final List<Integer> SUPPORTED_BMP_TYPES = Arrays.asList(BufferedImage.TYPE_3BYTE_BGR,
			BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE);

	@Override
	public String getFileExtension() {
		return FILE_EXTENSION;
	}

	@Override
	public void setBufferedImage(BufferedImage image) {
		if (!SUPPORTED_BMP_TYPES.contains(image.getType())) {
			LOG.debug("Converting BMP image to 24 bit");
			image = ColorUtils.cloneBufferedImage(image, BufferedImage.TYPE_3BYTE_BGR);
		}

		super.setBufferedImage(image);

		LOG.debug("Width, height, type: {}*{}*{}", getWidth(), getHeight(), this.image.getType());
	}
}
