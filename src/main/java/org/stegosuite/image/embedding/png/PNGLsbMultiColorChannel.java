package org.stegosuite.image.embedding.png;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.embedding.Visualizer.VisualizationMode;
import org.stegosuite.image.embedding.Visualizer.Visualize;
import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.embedding.point.PointGenerator;
import org.stegosuite.image.format.PNGImage;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoKeyException;
import org.stegosuite.model.payload.Payload;
import org.stegosuite.model.payload.PayloadEmbedder;
import org.stegosuite.model.payload.PayloadExtractor;
import org.stegosuite.util.ByteUtils;
import org.stegosuite.util.RgbChannel;

import java.awt.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Embed/Extract on PNG-images using data-spreading method. This method will utilise all {@code 3}
 * color channels, sequentially, meaning the payload will first be embedded into the
 * <b>{@code red}</b>-byte, then the <b>{@code green} </b>-byte, and finally the <b>{@code blue}</b>
 * -byte.
 *
 * @author alwin
 *
 */
public class PNGLsbMultiColorChannel
		extends EmbeddingMethod<PNGImage> {

	private static final Logger LOG = LoggerFactory.getLogger(PNGLsbMultiColorChannel.class);

	/**
	 * Constructor
	 *
	 * @param image the {@link PNGImage} to be embeded/extracted
	 * @param pointFilter the {@link PointFilter} to filter out undesired areas of the given
	 *        {@link PNGImage}
	 */
	public PNGLsbMultiColorChannel(PNGImage image, PointFilter<PNGImage> pointFilter) {
		super(image, pointFilter);
	}

	@Override
	public Visualizer createVisualizer(PNGImage image) {
		return new Visualizer(image, new Visualize(VisualizationMode.ALTERED, Color.RED),
				new Visualize(VisualizationMode.UNALTERED, Color.GREEN));
	}

	@Override
	public int doCapacity(PNGImage image) {
		LOG.debug("Embedding into {} LSBs", pointFilter.maxLsbCount());

		// Embedding will be done on all 3 color channels
		// Get the maximum number of LSBs from the point filter
		int pixelCount = image.getWidth() * image.getHeight();
		int filteredPixelCount = pointFilter.getFilteredPoints(image).size();
		int embeddableBits = (pixelCount - filteredPixelCount) * 3 * pointFilter.maxLsbCount();
		int embeddableBytes = embeddableBits / 8;

		return embeddableBytes;
	}

	@Override
	protected void doEmbed(PNGImage image, Payload payload, EmbeddingProgress progress)
			throws SteganoEmbedException {

		LOG.debug("Performing PNG LSB embedding");

		PayloadEmbedder payloadEmbedder = new PayloadEmbedder(payload, this.capacity());
		int numPayloadBytes = payloadEmbedder.getPayloadBytes().length;

		// Initialize the data spreader
		PointGenerator<PNGImage> pointGenerator = new PointGenerator<>(image, payload.getSteganoPassword(),
				pointFilter);

		int processedBits = 0;
		Iterator<Byte> payloadBits = payloadEmbedder.iteratePayloadBits().iterator();

		try {
			while (payloadBits.hasNext()) {
				// Get next point and its color
				Point point = pointGenerator.nextPoint();
				Color oldColor = new Color(image.getBufferedImage().getRGB(point.x, point.y));
				Color newColor = new Color(oldColor.getRGB());

				// Embed into all 3 color channels
				for (RgbChannel channel : RgbChannel.RGB()) {
					byte bit = payloadBits.next();
					int channelValue = channel.getValue(newColor);
					channelValue = ByteUtils.setBitAt(channelValue, pointGenerator.getIterationCount() - 1, bit);
					newColor = channel.setValue(newColor, channelValue);

					processedBits++;
					if (!payloadBits.hasNext()) {
						break;
					}
				}

				// Update visualization
				visualizer.visualize(point,
						newColor.equals(oldColor) ? VisualizationMode.UNALTERED : VisualizationMode.ALTERED);

				// Update pixel color
				image.getBufferedImage().setRGB(point.x, point.y, newColor.getRGB());

				// Update progress
				if (progress != null) {
					progress.progressUpdate(processedBits / 8, numPayloadBytes);
				}
			}
		} catch (NoSuchElementException e) {
			throw new SteganoEmbedException(e.getMessage());
		}
	}

	@Override
	protected void doExtract(PNGImage image, Payload payload, EmbeddingProgress progress)
			throws SteganoExtractException {

		LOG.debug("Performing PNG LSB extraction");

		PayloadExtractor payloadExtractor = new PayloadExtractor(payload);
		PointGenerator<PNGImage> pointGenerator = new PointGenerator<>(image, payload.getSteganoPassword(),
				pointFilter);

		try {
			while (!payloadExtractor.finished()) {
				// Get next point and its color
				Point point = pointGenerator.nextPoint();
				Color color = new Color(image.getBufferedImage().getRGB(point.x, point.y));

				// Extract from all 3 color channels
				for (RgbChannel channel : RgbChannel.RGB()) {
					int value = channel.getValue(color);
					byte bit = ByteUtils.getBitAt(value, pointGenerator.getIterationCount() - 1);
					payloadExtractor.processBit(bit);

					if (payloadExtractor.finished()) {
						break;
					}
				}

				// Update visualization
				visualizer.visualize(point, VisualizationMode.ALTERED);

				// Update progress
				if (progress != null && payloadExtractor.getPayloadLength() != null) {
					progress.progressUpdate(payloadExtractor.getProcessedBytesCount(),
							payloadExtractor.getPayloadLength());
				}
			}
		} catch (NoSuchElementException e) {
			LOG.error("The iteration count of PointGenerator exceeds its maximum iteration count ({})",
					pointFilter.maxLsbCount());
			throw new SteganoKeyException();
		}
	}
}
