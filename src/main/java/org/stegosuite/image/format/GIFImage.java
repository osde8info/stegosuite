package org.stegosuite.image.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.util.ColorDistance;
import org.stegosuite.util.ColorUtils;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static java.util.stream.Collectors.toSet;

public class GIFImage
		extends ImageFormat {

	private static final Logger LOG = LoggerFactory.getLogger(GIFImage.class);

	public static final String FILE_EXTENSION = "gif";

	private int[] pixels = null;

	private List<Color> colorTable = null;

	private Map<ColorDistance, List<Color>> sortedColorTables = new HashMap<>();

	/**
	 * Custom parameters for encoding GIF images
	 */
	private class GIFImageWriteParam
			extends ImageWriteParam {

		GIFImageWriteParam() {
			super(null);
			canWriteCompressed = true;
			canWriteProgressive = true;
			compressionTypes = new String[] { "LZW", "lzw" };
			compressionType = compressionTypes[0];
			// Disable interlacing to decrease file size
			progressiveMode = ImageWriteParam.MODE_DISABLED;
		}

		@Override
		public void setCompressionMode(int mode) {
			if (mode == MODE_DISABLED) {
				throw new UnsupportedOperationException("MODE_DISABLED is not supported.");
			}
			super.setCompressionMode(mode);
		}
	}

	/**
	 * Saves the modified GIF to file
	 */
	@Override
	public void save(File gifFile)
			throws SteganoImageException {

		LOG.info("Saving GIF image to {}", gifFile.getAbsolutePath());

		ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
		ImageWriter writer = ImageIO.getImageWriters(type, FILE_EXTENSION).next();

		try (ImageOutputStream stream = ImageIO.createImageOutputStream(gifFile)) {
			// gifFile.delete();
			writer.setOutput(stream);
			try {
				writer.write(null, new IIOImage(image, null, null), new GIFImageWriteParam());
			} finally {
				writer.dispose();
				stream.flush();
			}
		} catch (IOException e) {
			throw new SteganoImageException(e.getMessage());
		}
	}

	/**
	 *
	 */
	@Override
	public String getFileExtension() {
		return FILE_EXTENSION;
	}

	/**
	 * Sets the internal BufferedImage instance
	 */
	@Override
	public void setBufferedImage(BufferedImage image) {
		super.setBufferedImage(image);
		pixels = null;
		colorTable = null;
		sortedColorTables.clear();
	}

	/**
	 * Returns up to 256 colors from the GIF color table. The black padding colors at the end of the
	 * table are skipped.
	 *
	 * @return
	 */
	public List<Color> getColorTable() {
		if (colorTable == null) {
			long startTime = System.nanoTime();

			IndexColorModel colorModel = (IndexColorModel) image.getColorModel();
			int colorCount = colorModel.getMapSize();

			byte[] reds = new byte[colorCount];
			byte[] greens = new byte[colorCount];
			byte[] blues = new byte[colorCount];

			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);

			colorTable = new ArrayList<>(colorCount);
			for (int i = 0; i < reds.length; i++) {
				colorTable.add(new Color(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF));
			}

			// Remove trailing blacks if they are unreferenced
			Set<Integer> referencedColors = Arrays.stream(getPixels()).boxed().collect(toSet());
			while (colorTable.size() > 1 && colorTable.get(colorTable.size() - 1).equals(Color.BLACK)
					&& referencedColors.contains(colorTable.get(colorTable.size() - 1))) {
				colorTable.remove(colorTable.size() - 1);
			}

			LOG.debug("{} colors extracted from GIF color table in {} ms", colorTable.size(),
					(System.nanoTime() - startTime) / 1000000);
		}

		return colorTable;
	}

	/**
	 * Returns the current color table sorted by the specified color distance
	 *
	 * @param colorDistance
	 * @return
	 */
	public List<Color> getSortedColorTable(ColorDistance colorDistance) {
		if (!sortedColorTables.containsKey(colorDistance)) {
			long startTime = System.nanoTime();
			sortedColorTables.put(colorDistance, ColorUtils.sortColors(getColorTable(), colorDistance));
			LOG.debug("Sorted color table in {} ms", (System.nanoTime() - startTime) / 1000000);
		}
		return new ArrayList<>(sortedColorTables.get(colorDistance));
	}

	/**
	 * Overwrites the existing color table with a new color table.
	 *
	 * @param table
	 */
	public void setColorTable(List<Color> table) {
		LOG.debug("Writing {} colors to GIF color table", table.size());

		byte[] reds = new byte[table.size()];
		byte[] greens = new byte[table.size()];
		byte[] blues = new byte[table.size()];

		for (int i = 0; i < table.size(); i++) {
			Color color = table.get(i);
			reds[i] = (byte) color.getRed();
			greens[i] = (byte) color.getGreen();
			blues[i] = (byte) color.getBlue();
		}

		IndexColorModel colorModel = new IndexColorModel(8, table.size(), reds, greens, blues);
		WritableRaster raster = colorModel.createCompatibleWritableRaster(getWidth(), getHeight());
		raster.setPixels(0, 0, getWidth(), getHeight(), getPixels());
		image = new BufferedImage(colorModel, raster, image.isAlphaPremultiplied(), null);

		colorTable = null;
		sortedColorTables.clear();
	}

	/**
	 * Returns an array of pixel references.
	 *
	 * @return
	 */
	public int[] getPixels() {
		if (pixels == null) {
			int w = getWidth();
			int h = getHeight();

			LOG.debug("Reading {} pixels from GIF file ({}x{})", w * h, w, h);

			pixels = image.getRaster().getPixels(0, 0, w, h, (int[]) null);
		}

		return pixels;
	}

	/**
	 * Overwrites the existing pixel references.
	 *
	 * @param newPixels
	 */
	public void setPixels(int[] newPixels) {
		int w = getWidth();
		int h = getHeight();

		LOG.debug("Writing {} pixels to GIF file ({}x{})", w * h, w, h);

		image.getRaster().setPixels(0, 0, w, h, newPixels);

		pixels = null;
	}

	/**
	 * Returns the histogram of the GIF image.
	 *
	 * @return
	 */
	public Map<Color, Integer> getHistogram() {
		return ColorUtils.getHistogram(getColorTable(), getPixels());
	}
}
