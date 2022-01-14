package org.stegosuite.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Utility class that provides various color-related methods such as color transformations, color
 * comparisons, color sorting, etc.
 */
public class ColorUtils {

	/**
	 * Sorts a list of RGB colors with a specific color distance algorithm
	 *
	 * @param colors
	 * @param colorDistance
	 * @return
	 */
	public static List<Color> sortColors(List<Color> colors, ColorDistance colorDistance) {
		if (colors.size() < 2) {
			return colors;
		}

		Set<Color> unsortedBase = new HashSet<>(colors);
		Map<Double, List<Color>> sortedLists = new ConcurrentHashMap<>();

		unsortedBase.parallelStream().forEach(base -> {
			List<Color> unsorted = new ArrayList<>(unsortedBase);
			unsorted.remove(base);

			List<Color> sorted = new ArrayList<>(unsortedBase.size());
			sorted.add(base);

			Double sumDistance = 0.0;
			while (!unsorted.isEmpty()) {
				List<Double> distances = unsorted.stream()
						.map(c -> colorDistance.distance(c, sorted.get(sorted.size() - 1))).collect(toList());
				Double minDistance = Collections.min(distances);
				sumDistance += minDistance;
				sorted.add(unsorted.remove(distances.indexOf(minDistance)));
			}

			sortedLists.put(sumDistance, sorted);
		});

		return sortedLists.get(Collections.min(sortedLists.keySet()));
	}

	/**
	 * Returns a histogram (or frequency map) of all colors referenced in the pixels array
	 *
	 * @param colors
	 * @param pixels
	 * @return
	 */
	public static Map<Color, Integer> getHistogram(List<Color> colors, int[] pixels) {
		Map<Color, Integer> histogram = new HashSet<>(colors).stream().collect(toMap(identity(), c -> 0));
		Arrays.stream(pixels).mapToObj(colors::get).forEach(color -> histogram.put(color, histogram.get(color) + 1));
		return histogram;
	}

	/**
	 * Returns the colors that are present in the color table but not referenced by any pixel
	 *
	 * @param colorTable
	 * @param pixels
	 * @return
	 */
	public static Set<Color> getUnreferencedColors(List<Color> colorTable, int[] pixels) {
		return getUnreferencedColors(getHistogram(colorTable, pixels));
	}

	/**
	 * Returns the colors that are present in the histogram that have zero occurrences
	 *
	 * @param histogram
	 * @return
	 */
	public static Set<Color> getUnreferencedColors(Map<Color, Integer> histogram) {
		return histogram.entrySet().stream().filter(e -> e.getValue() == 0).map(Entry::getKey).collect(toSet());
	}

	/**
	 * Writes a list of colors onto a BufferedImage for easy visualization
	 *
	 * @param colors
	 * @return
	 */
	public static BufferedImage colorsToImage(List<Color> colors) {
		if (colors.isEmpty()) {
			return null;
		}

		int width = 400;
		int height = 40;

		BufferedImage bufferedImage = new BufferedImage(width, height * colors.size(), BufferedImage.TYPE_INT_RGB);
		Graphics graphics = bufferedImage.getGraphics();
		graphics.setFont(new Font("Arial", Font.BOLD, 16));

		for (int i = 0; i < colors.size(); i++) {
			Color color = colors.get(i);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					bufferedImage.setRGB(x, (i * height) + y, color.getRGB());
				}
			}

			String rgb = String.format("[%d,%d,%d]", color.getRed(), color.getGreen(), color.getBlue());
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, i * height, 150, height);
			graphics.setColor(Color.WHITE);
			graphics.drawString(rgb, 25, (i * height) + 25);
		}

		return bufferedImage;
	}

	/**
	 * Creates an exact copy of the passed buffered image instance
	 *
	 * @param source
	 * @return
	 */
	public static BufferedImage cloneBufferedImage(BufferedImage source) {
		return cloneBufferedImage(source, source.getType());
	}

	/**
	 * Creates a copy of the passed buffered image instance and optionally does type conversion
	 *
	 * @param source
	 * @param imageType
	 * @return
	 */
	public static BufferedImage cloneBufferedImage(BufferedImage source, int imageType) {
		if (source.getType() == imageType) {
			boolean isAlphaPremultiplied = source.getColorModel().isAlphaPremultiplied();
			WritableRaster raster = source.copyData(source.getRaster().createCompatibleWritableRaster());
			return new BufferedImage(source.getColorModel(), raster, isAlphaPremultiplied, null);
		} else {
			BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), imageType);
			Graphics2D g2d = copy.createGraphics();
			g2d.drawImage(source, 0, 0, null);
			g2d.dispose();
			return copy;
		}
	}
}
