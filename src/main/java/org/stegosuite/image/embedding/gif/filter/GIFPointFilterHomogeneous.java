package org.stegosuite.image.embedding.gif.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.GIFImage;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.util.ColorDistance;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Removes all points that are part of homogeneous areas of a GIF image
 */
public class GIFPointFilterHomogeneous
		extends PointFilter<GIFImage> {

	private static final Logger LOG = LoggerFactory.getLogger(GIFPointFilterHomogeneous.class);

	private static final ColorDistance DISTANCE = ColorDistance.CIEDE_2000;

	private Collection<Point> filteredPoints = null;
	private int[] indices = null;
	private Map<Integer, Integer> indexToSortedIndex = null;

	@Override
	public int maxLsbCount() {
		return 1;
	}

	@Override
	protected Collection<Point> filter(GIFImage image) {
		filteredPoints = Collections.newSetFromMap(new ConcurrentHashMap<>());
		indices = image.getPixels();

		// Compute a mapping from original color indices to the indices in the sorted table,
		// such that for all colors: table(i) == sortedTable(indexToSortedIndex(i))
		List<Color> colorTable = image.getColorTable();
		List<Color> sortedTable = image.getSortedColorTable(DISTANCE);
		indexToSortedIndex = IntStream.range(0, colorTable.size()).boxed()
				.collect(toMap(identity(), i -> sortedTable.indexOf(colorTable.get(i))));

		long startTime = System.nanoTime();

		int width = image.getWidth();
		int height = image.getHeight();

		// Partition the image into as many slices as CPU cores we have available
		int numSlices = Runtime.getRuntime().availableProcessors();
		int rowsForEachSlice = height / numSlices;

		IntStream.range(0, numSlices).boxed().parallel().forEach(numSlice -> {
			long startTimeSlice = System.nanoTime();

			// Calculate start & end row of current slice
			int sliceStartRow = numSlice * rowsForEachSlice;
			int sliceEndRow = numSlice + 1 < numSlices ? sliceStartRow + rowsForEachSlice : height - 2;

			int[][] normalizedRows = getNormalizedRows(width, sliceStartRow, sliceEndRow + 2);
			Collection<Point> homogeneousPoints = collectHomogeneousPointsOfRows(normalizedRows, sliceStartRow,
					sliceEndRow, width);
			filteredPoints.addAll(homogeneousPoints);

			LOG.debug("Filtered slice {} of {} rows in {} ms", numSlice, sliceEndRow - sliceStartRow,
					(System.nanoTime() - startTimeSlice) / 1000000);
		});

		LOG.debug("Filtered homogeneous points in {} ms", (System.nanoTime() - startTime) / 1000000);

		return filteredPoints;
	}

	/**
	 * Returns a sub-matrix of all indices with each index having set its LSBs to zero
	 *
	 * @param width The width of the matrix
	 * @param sliceStartIncl The start row of the pixels, inclusive
	 * @param sliceEndExcl The end row of the pixels, exclusive
	 * @return
	 */
	private int[][] getNormalizedRows(int width, int sliceStartIncl, int sliceEndExcl) {
		int height = sliceEndExcl - sliceStartIncl;
		int[][] normalizedRows = new int[width][height];

		for (int y = sliceStartIncl; y < sliceEndExcl; y++) {
			for (int x = 0; x < width; x++) {
				int index = indices[y * width + x];
				int sortedIndex = indexToSortedIndex.get(index);

				// Set last bit of the sorted index to 0 because it will be changed after embedding
				normalizedRows[x][y - sliceStartIncl] = sortedIndex & ~0b1;
			}
		}

		return normalizedRows;
	}

	/**
	 * Returns a list with points of homogeneous areas in the normalizedRows matrix. Each returned
	 * point has the row offset (sliceStartIncl) added so it is an absolute position in the image.
	 *
	 * @param normalizedRows
	 * @param sliceStartIncl
	 * @param sliceEndExcl
	 * @param width
	 */
	private Collection<Point> collectHomogeneousPointsOfRows(int[][] normalizedRows, int sliceStartIncl,
			int sliceEndExcl, int width) {

		Collection<Point> filteredPoints = new ArrayList<>();

		for (int y = 0; y < sliceEndExcl - sliceStartIncl; y++) {
			// Indicates the left-most column of the current homogeneous area. -1 means we are not
			// in a homogeneous area.
			int homogeneousColumnStart = -1;

			for (int x = 0; x < width; x++) {
				int thisColumnColor = normalizedRows[x][y];

				// True if all 3 pixels of the current column are the same
				boolean isHomogeneousColumn = normalizedRows[x][y + 1] == thisColumnColor
						&& normalizedRows[x][y + 2] == thisColumnColor;

				// True if current column is part of a previously started homogeneous area
				if (isHomogeneousColumn && homogeneousColumnStart != -1
						&& thisColumnColor == normalizedRows[homogeneousColumnStart][y]) {
					// Jump to next column if the current column belongs the current homogeneous
					// field. In the last column we need to pretend we are 1 column ahead so that we
					// can correctly collect all points.
					if (x + 1 == width) {
						x++;
					} else {
						continue;
					}
				}

				// Collect all points of the homogeneous area if it's at 3 pixels wide
				if (homogeneousColumnStart != -1 && x - homogeneousColumnStart >= 3) {
					for (int dX = homogeneousColumnStart; dX < x; dX++) {
						for (int dY = 0; dY < 3; dY++) {
							filteredPoints.add(new Point(dX, sliceStartIncl + y + dY));
						}
					}
				}

				// If this column is homogeneous we are at the beginning of a new homogeneous area
				homogeneousColumnStart = isHomogeneousColumn ? x : -1;
			}
		}

		return filteredPoints;
	}

	/**
	 * Debug only
	 *
	 * @param args
	 * @throws SteganoImageException
	 */
	public static void main(String[] args)
			throws SteganoImageException {

		GIFImage image = new GIFImage();
		image.load(new File("src/test/resources/sunflower.gif"));

		PointFilter<GIFImage> filter = new GIFPointFilterHomogeneous();
		Collection<Point> filteredPoints = filter.getFilteredPoints(image);

		LOG.debug("Total pixels: {} * {} = {}", image.getWidth(), image.getHeight(),
				image.getWidth() * image.getHeight());
		LOG.debug("Count of non-noise pixels: {}", filteredPoints.size());

		for (Point p : filteredPoints) {
			image.getBufferedImage().setRGB(p.x, p.y, Color.RED.getRGB());
		}
		image.save(new File("src/test/resources/sunflower_noise.gif"));
	}
}
