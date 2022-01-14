package org.stegosuite.util;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import java.awt.image.*;
import java.util.stream.IntStream;

public class ImageSwtAwtConverter {

	/** Private constructor to hide the implicit public one */
	private ImageSwtAwtConverter() {}

	/**
	 * Convert a given AWT's {@link BufferedImage} into SWT's {@link ImageData}
	 *
	 * @param bufferedImage the {@link BufferedImage} to be converted
	 *
	 * @return the associated {@link ImageData}
	 */
	public static ImageData convertToSWT(BufferedImage bufferedImage) {
		int w = bufferedImage.getWidth();
		int h = bufferedImage.getHeight();
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel cm = (DirectColorModel) bufferedImage.getColorModel();
			PaletteData paletteData = new PaletteData(cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask());
			ImageData imageData = new ImageData(w, h, cm.getPixelSize(), paletteData);

			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[bufferedImage.getColorModel().getComponentSize().length];

			IntStream.range(0, h).forEach(y -> IntStream.range(0, w).forEach(x -> {
				raster.getPixel(x, y, pixelArray);
				int pixel = paletteData.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
				imageData.setPixel(x, y, pixel);
				if (cm.hasAlpha()) {
					imageData.setAlpha(x, y, pixelArray[3]);
				}
			}));

			return imageData;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel cm = (IndexColorModel) bufferedImage.getColorModel();
			int arrayLength = cm.getMapSize();
			byte[] reds = new byte[arrayLength];
			byte[] greens = new byte[arrayLength];
			byte[] blues = new byte[arrayLength];
			cm.getReds(reds);
			cm.getGreens(greens);
			cm.getBlues(blues);
			RGB[] rgbs = new RGB[arrayLength];
			IntStream.range(0, rgbs.length)
					.forEach(i -> rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF));
			PaletteData paletteData = new PaletteData(rgbs);
			ImageData imageData = new ImageData(w, h, cm.getPixelSize(), paletteData);
			imageData.transparentPixel = cm.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];

			IntStream.range(0, h).forEach(y -> IntStream.range(0, w).forEach(x -> {
				raster.getPixel(x, y, pixelArray);
				imageData.setPixel(x, y, pixelArray[0]);
			}));

			return imageData;
		} else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
			ComponentColorModel cm = (ComponentColorModel) bufferedImage.getColorModel();
			PaletteData paletteData = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
			ImageData imageData = new ImageData(w, h, cm.getPixelSize(), paletteData);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[bufferedImage.getColorModel().getComponentSize().length];

			IntStream.range(0, h).forEach(y -> IntStream.range(0, w).forEach(x -> {
				raster.getPixel(x, y, pixelArray);
				int pixel = paletteData.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
				imageData.setPixel(x, y, pixel);
				if (cm.hasAlpha()) {
					imageData.setAlpha(x, y, pixelArray[3]);
				}
			}));
			return imageData;
		}
		return null;
	}
}
