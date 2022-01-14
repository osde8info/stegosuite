package org.stegosuite.image.embedding.bmp.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.BMPImage;
import org.stegosuite.model.exception.SteganoImageException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Removes all points that are part of homogeneous areas of a BMP image
 */
public class BMPPointFilterHomogeneous
		extends PointFilter<BMPImage> {

	/**
	 * Sets the LSB of all 3 color channels of a 32bit RGB value to 0
	 */
	private static final int MASK_LSBS_TO_ZERO = ~0b10000000100000001;

	private static final Logger LOG = LoggerFactory.getLogger(BMPPointFilterHomogeneous.class);

	@Override
	public int maxLsbCount() {
		return 1;
	}

	/**
	 * Returns a list of all homogeneous areas
	 */
	@Override
	protected Collection<Point> filter(BMPImage image) {
		int[][] normalizedRgbValues = getNormalizedRgbValues(image.getBufferedImage());
		// long startTime = System.nanoTime();
		Collection<Point> filteredPoints = new HashSet<>();
		for (int x = 1; x < normalizedRgbValues.length - 1; x++) {
			for (int y = 1; y < normalizedRgbValues[x].length - 1; y++) {
				Collection<Point> homogenousPoints = getHomogeneousPoints(normalizedRgbValues, x, y);
				filteredPoints.addAll(homogenousPoints);
			}
		}
		// System.out.println("filter : " + (System.nanoTime() - startTime) / 1000000 + " ms");
		return filteredPoints;
	}

	/**
	 * Returns a matrix of all RGB values that are present in the image with each value having set
	 * its LSBs for the 3 channels to zero.
	 * 
	 * @param image
	 * @return
	 */
	private int[][] getNormalizedRgbValues(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		int[][] rgbValues = new int[width][];
		for (int x = 0; x < width; x++) {
			rgbValues[x] = new int[height];
			for (int y = 0; y < height; y++) {
				rgbValues[x][y] = image.getRGB(x, y) & MASK_LSBS_TO_ZERO;
			}
		}

		return rgbValues;
	}

	/**
	 * Returns a list of points that are part of a homogeneous area around the x and y coordinate.
	 * Returns an empty list if the area is not homogeneous.
	 * 
	 * @param normalizedRgbValues
	 * @param x
	 * @param y
	 * @return
	 */
	private Collection<Point> getHomogeneousPoints(int[][] normalizedRgbValues, int x, int y) {
		Collection<Point> homogeneousPoints = new ArrayList<>();
		int referenceRgbValue = normalizedRgbValues[x][y];
		// System.out.println(Integer.toBinaryString(referenceRgbValue));
		for (int dX = x - 1; dX <= x + 1; dX++) {
			for (int dY = y - 1; dY <= y + 1; dY++) {
				// As soon as 1 pixel in the area is different than the pixel in
				// the center, the area is considered NOT to be homogeneous.
				if (dX != x && dY != y && normalizedRgbValues[dX][dY] != referenceRgbValue) {
					return Collections.emptyList();
				}

				homogeneousPoints.add(new Point(dX, dY));
			}
		}

		return homogeneousPoints;
	}

	/**
	 * Debug only
	 *
	 * @param args
	 * @throws IOException
	 * @throws SteganoImageException
	 */
	public static void main(String[] args)
			throws SteganoImageException {

		BMPImage bmpImage = new BMPImage();
		bmpImage.load(new File("resources/Snow.bmp"));

		LOG.debug("Width: {}", bmpImage.getWidth());
		LOG.debug("Height: {}", bmpImage.getHeight());
		LOG.debug("Total pixel: {}", (bmpImage.getWidth() * bmpImage.getHeight()));

		List<Point> allPoints = new ArrayList<>();
		for (int x = 0; x < bmpImage.getWidth(); x++) {
			for (int y = 0; y < bmpImage.getHeight(); y++) {
				allPoints.add(new Point(x, y));
			}
		}

		PointFilter<BMPImage> filter = new BMPPointFilterHomogeneous();
		Collection<Point> filteredPoints = filter.getFilteredPoints(bmpImage);
		LOG.debug("Count of non-noise pixels: {}", filteredPoints.size());

		BufferedImage bufferedImage = bmpImage.getBufferedImage();
		for (Point p : filteredPoints) {
			bufferedImage.setRGB(p.x, p.y, Color.RED.getRGB());
		}
		bmpImage.setBufferedImage(bufferedImage);

		bmpImage.save(new File("resources/sunflower_Noise2.bmp"));
	}
}
