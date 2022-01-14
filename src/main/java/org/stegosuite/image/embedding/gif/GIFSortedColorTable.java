package org.stegosuite.image.embedding.gif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.embedding.Visualizer.VisualizationMode;
import org.stegosuite.image.embedding.Visualizer.Visualize;
import org.stegosuite.image.embedding.gif.filter.GIFPointFilterHomogeneous;
import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.embedding.point.PointGenerator;
import org.stegosuite.image.format.GIFImage;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.model.exception.SteganoKeyException;
import org.stegosuite.model.payload.Payload;
import org.stegosuite.model.payload.PayloadEmbedder;
import org.stegosuite.model.payload.PayloadExtractor;
import org.stegosuite.model.payload.block.FileBlock;
import org.stegosuite.model.payload.block.MessageBlock;
import org.stegosuite.util.ColorDistance;
import org.stegosuite.util.ColorUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toList;

/**
 * "Normal" embedding and extracting procedures for GIF image, i.e. using a sorted color table.
 */
public class GIFSortedColorTable
		extends EmbeddingMethod<GIFImage> {

	private static final Logger LOG = LoggerFactory.getLogger(GIFSortedColorTable.class);

	private static final ColorDistance DISTANCE = ColorDistance.CIEDE_2000;

	/**
	 * Number of colors to skip when embedding / extracting to prevent replacing too distinct
	 * colors. The colors are trimmed from the end of the sorted color table as that's where the
	 * distance between successive colors is the largest.
	 */
	private static final int NUM_SKIP_TRAILING_COLORS = 6;

	private SimpleEntry<GIFImage, Set<Color>> skipColors = null;

	public GIFSortedColorTable(GIFImage image, PointFilter<GIFImage> pointFilter) {
		super(image, pointFilter);
	}

	@Override
	protected Visualizer createVisualizer(GIFImage image) {
		return new Visualizer(image, new Visualize(VisualizationMode.ALTERED, Color.RED),
				new Visualize(VisualizationMode.UNALTERED, Color.GREEN));
	}

	/**
	 * The SortedColorTable method can embed 1 bit in each pixel. From the total number of pixels,
	 * we subtract 1) the number of pixels that have a color we need to skip, and 2) the number of
	 * pixels that the point filter doesn't allow us to embed into.
	 */
	@Override
	protected int doCapacity(GIFImage image) {
		Map<Color, Integer> histogram = image.getHistogram();
		int skipPixelCount = getSkipColors(image).stream().mapToInt(c -> histogram.get(c)).sum();
		int filteredPixelCount = pointFilter.getFilteredPoints(image).size();
		return (image.getHeight() * image.getWidth() - filteredPixelCount - skipPixelCount) / 8;
	}

	@Override
	protected void doEmbed(GIFImage image, Payload payload, EmbeddingProgress progress)
			throws SteganoEmbedException {

		PointGenerator<GIFImage> pointGenerator = new PointGenerator<>(image, payload.getSteganoPassword(),
				pointFilter);
		PayloadEmbedder embedder = new PayloadEmbedder(payload, this.capacity());
		int payloadNumBytes = embedder.getPayloadBytes().length;

		int[] pixels = image.getPixels().clone();
		List<Color> table = image.getColorTable();
		List<Color> sortedTable = image.getSortedColorTable(DISTANCE);
		Set<Color> unreferencedColorsBefore = ColorUtils.getUnreferencedColors(table, pixels);

		int currentBit = 0;
		for (byte bit : embedder.iteratePayloadBits()) {
			Point point = null;
			int pixelIndex = 0;
			Color currentColor = null;

			do {
				point = pointGenerator.nextPoint();
				pixelIndex = (point.y * image.getWidth()) + point.x;
				currentColor = table.get(pixels[pixelIndex]);
			} while (getSkipColors(image).contains(currentColor));

			int sortedColorIndex = sortedTable.indexOf(currentColor);
			boolean pixelMatchesPayloadBit = sortedColorIndex % 2 == bit;

			// Write steganogram
			if (!pixelMatchesPayloadBit) {
				// Toggle LSB
				int newSortedColorIndex = Math.min(sortedColorIndex ^ 0b1, sortedTable.size() - 1);
				pixels[pixelIndex] = table.indexOf(sortedTable.get(newSortedColorIndex));
			}

			// Write visualization
			visualizer.visualize(point,
					pixelMatchesPayloadBit ? VisualizationMode.UNALTERED : VisualizationMode.ALTERED);

			// Update progress
			if (progress != null) {
				progress.progressUpdate(currentBit++ / 8, payloadNumBytes);
			}
		}

		// Check for colors that were referenced before embedding but are now
		// unreferenced
		Set<Color> referencedColorsAfter = ColorUtils.getUnreferencedColors(table, pixels);
		referencedColorsAfter.removeAll(unreferencedColorsBefore);
		if (!referencedColorsAfter.isEmpty()) {
			LOG.info("Reinserting {} unreferenced colors", referencedColorsAfter.size());
			Map<Color, Integer> histogram = ColorUtils.getHistogram(table, pixels);
			for (Color unreferencedColor : referencedColorsAfter) {
				// Sort table by similarity to unreferenced color
				List<Color> similarColors = new ArrayList<>(table);
				similarColors.remove(unreferencedColor);
				similarColors.sort((c1, c2) -> (int) Math
						.round(DISTANCE.distance(unreferencedColor, c1) - DISTANCE.distance(unreferencedColor, c2)));
				// Try to replace most similar color with unreferenced color
				boolean isFixed = false;
				for (int i = 0; i < similarColors.size() && !isFixed; i++) {
					Color similarColor = similarColors.get(i);
					// Only replace it if it's referenced at least twice, we
					// don't want another unreferenced color
					if (histogram.get(similarColor) > 1) {
						int similarColorIndex = table.indexOf(similarColor);
						// Find first pixel of that color
						for (int j = 0; j < pixels.length && !isFixed; j++) {
							if (pixels[j] == similarColorIndex) {
								// Only modify the pixel if it doesn't carry our
								// payload
								Point point = new Point(j % image.getWidth(), j / image.getWidth());
								if (!pointGenerator.wasGenerated(point)) {
									pixels[j] = table.indexOf(unreferencedColor);
									isFixed = true;
								}
							}
						}
					}
				}
			}
		}

		// Sort color table by new color frequencies
		List<Color> colorsSortedByFrequency = image.getHistogram().entrySet().stream()
				.sorted(Collections.reverseOrder(Entry.comparingByValue())).map(Entry::getKey).collect(toList());

		// Adjust pixels to new sorting of the palette
		pixels = Arrays.stream(pixels).map(i -> colorsSortedByFrequency.indexOf(table.get(i))).toArray();

		image.setColorTable(colorsSortedByFrequency);
		image.setPixels(pixels);
	}

	@Override
	protected void doExtract(GIFImage image, Payload payload, EmbeddingProgress progress)
			throws SteganoExtractException {

		int[] pixels = image.getPixels();
		List<Color> table = image.getColorTable();
		List<Color> sortedTable = image.getSortedColorTable(DISTANCE);

		PayloadExtractor extractor = new PayloadExtractor(payload);
		PointGenerator<GIFImage> pointGenerator = new PointGenerator<>(image, payload.getSteganoPassword(),
				pointFilter);

		try {
			while (!extractor.finished()) {
				Point point = pointGenerator.nextPoint();
				int pixelIndex = (point.y * image.getWidth()) + point.x;
				Color color = table.get(pixels[pixelIndex]);

				if (getSkipColors(image).contains(color)) {
					continue;
				}

				int sortedColorIndex = sortedTable.indexOf(color);
				byte bit = (byte) (sortedColorIndex % 2);
				extractor.processBit(bit);

				// Write visualization
				visualizer.visualize(point, VisualizationMode.ALTERED);

				// Update progress
				if (progress != null && extractor.getPayloadLength() != null) {
					progress.progressUpdate(extractor.getProcessedBytesCount(), extractor.getPayloadLength());
				}
			}
		} catch (NoSuchElementException e) {
			// The point generator hit its limit, corrupt payload size
			throw new SteganoKeyException();
		}
	}

	/**
	 * Returns the a list of colors to skip when embedding and extracting
	 * 
	 * @param image
	 * @return
	 */
	private Collection<Color> getSkipColors(GIFImage image) {
		if (skipColors == null || !skipColors.getKey().equals(image)) {
			skipColors = new SimpleEntry<>(image, new HashSet<>());
			List<Color> sortedTable = image.getSortedColorTable(DISTANCE);
			int sortedTableSize = sortedTable.size();

			// In addition to skipping trailing colors, we skip one more color
			// if the number of colors in the palette is uneven. Otherwise we
			// might switch the last color's LSB from 0 to 1 which increases the
			// index and therefore causes an ArrayIndexOutOfBoundsException
			for (int i = 0; i < NUM_SKIP_TRAILING_COLORS + (sortedTableSize % 2); i++) {
				skipColors.getValue().add(sortedTable.get(sortedTableSize - i - 1));
			}
		}
		return skipColors.getValue();
	}


	// TODO: Move this into tests
	public static void main(String[] args)
			throws SteganoImageException, SteganoEmbedException, SteganoExtractException, IOException {

		File carrierFile = new File("D:/test/fish.gif");
		File secretFileIn = new File("D:/test/secret_in3");
		File secretFileOut = new File("D:/test/secret_out");
		File steganogramFile = new File("D:/test/steganogram_" + GIFSortedColorTable.DISTANCE + ".gif");

		GIFImage gifImage = new GIFImage();
		gifImage.load(carrierFile);
		LOG.info("{} unreferenced colors", ColorUtils.getUnreferencedColors(gifImage.getHistogram()).size());

		EmbeddingMethod<GIFImage> embeddingMethod = new GIFSortedColorTable(gifImage, new GIFPointFilterHomogeneous());

		// Prepare payload to embed
		Payload payloadToEmbed = new Payload();

		payloadToEmbed.setSteganoPassword("123");

		// Disable payload encryption
		payloadToEmbed.setEncryptionPassword(null);

		// Add file to payload
		FileBlock fileBlock = new FileBlock(secretFileIn.getAbsolutePath());
		payloadToEmbed.addBlock(fileBlock);

		MessageBlock messageBlock = new MessageBlock("Some hidden message");
		payloadToEmbed.addBlock(messageBlock);

		// Embed
		embeddingMethod.embed(payloadToEmbed, null);
		gifImage.save(steganogramFile);

		// Prepare payload to extract
		Payload payloadExtracted = new Payload();

		payloadExtracted.setSteganoPassword("123");

		// Extract
		gifImage = new GIFImage();
		gifImage.load(steganogramFile);
		LOG.info("{} unreferenced colors", ColorUtils.getUnreferencedColors(gifImage.getHistogram()).size());
		embeddingMethod.extract(payloadExtracted, null);

		// Save extracted contents to file
		fileBlock = (FileBlock) payloadExtracted.getBlock(0);
		LOG.info("Original file name: " + fileBlock.getFileName());
		Files.write(secretFileOut.toPath(), fileBlock.getFileContent(), StandardOpenOption.CREATE);

		messageBlock = (MessageBlock) payloadExtracted.getBlock(1);
		LOG.info("Message: {}", messageBlock.getMessage());
	}

}
