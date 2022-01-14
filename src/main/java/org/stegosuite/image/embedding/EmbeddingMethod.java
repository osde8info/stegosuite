package org.stegosuite.image.embedding;

import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.payload.Payload;

/**
 * Each embedding technique we support (SortedColorTable, Shuffle, LSB for bitmaps) needs to
 * implement this interface
 *
 * @param <T> The image format which the embedding method supports
 */
public abstract class EmbeddingMethod<T extends ImageFormat> {

	/**
	 * The image to embed to or extract from. The actual image to embed into or extract from is
	 * passed to the doEmbed() and doExtract() methods to make sure this image is not modified
	 */
	private final T image;

	/**
	 * The filter to apply before embedding or extraction to skip certain points
	 */
	protected final PointFilter<T> pointFilter;

	/**
	 * Keeps track of the processed pixels in the image. Needs to be properly initialized in the
	 * implementing class and can then be used in the embed() and extract() methods.
	 */
	protected Visualizer visualizer = null;

	/**
	 * Constructor
	 * 
	 * @param image The image to embed to or extract from
	 * @param pointFilter The filter to apply before embedding or extraction to skip certain points
	 */
	public EmbeddingMethod(T image, PointFilter<T> pointFilter) {
		this.image = image;
		this.pointFilter = pointFilter;
	}

	/**
	 * Returns the number of bytes that the current embedding technique is able to embed into the
	 * passed image
	 * 
	 * @return
	 */
	public final int capacity() {
		return this.doCapacity(this.image);
	}

	/**
	 * Internal capacity method implemented by subclasses
	 * 
	 * @param image
	 * @return
	 */
	protected abstract int doCapacity(T image);

	/**
	 * Embeds the payload into the carrier image
	 * 
	 * @param payload
	 * @param progress
	 * @return
	 * @throws SteganoEmbedException
	 */
	public final T embed(Payload payload, EmbeddingProgress progress)
			throws SteganoEmbedException {
		@SuppressWarnings("unchecked")
		T clonedImage = (T) this.image.clone();
		this.visualizer = this.createVisualizer(clonedImage);
		this.doEmbed(clonedImage, payload, progress);
		return clonedImage;
	}

	/**
	 * Internal embedding method implemented by subclasses
	 * 
	 * @param payload
	 * @param progress
	 * @throws SteganoEmbedException
	 */
	protected abstract void doEmbed(T image, Payload payload, EmbeddingProgress progress)
			throws SteganoEmbedException;

	/**
	 * Extracts the payload from the steganogram
	 * 
	 * @param payload
	 * @param progress
	 * @throws SteganoExtractException
	 */
	public final void extract(Payload payload, EmbeddingProgress progress)
			throws SteganoExtractException {
		this.visualizer = this.createVisualizer(this.image);
		this.doExtract(this.image, payload, progress);
	}

	/**
	 * Internal extraction method implemented by subclasses
	 * 
	 * @param image
	 * @param payload
	 * @param progress
	 * @throws SteganoExtractException
	 */
	protected abstract void doExtract(T image, Payload payload, EmbeddingProgress progress)
			throws SteganoExtractException;

	/**
	 * Gets the current visualizer for this embedding method
	 * 
	 * @return
	 */
	public Visualizer getVisualizer() {
		return this.visualizer;
	}

	/**
	 * If the subclass supports visualizing the embedding/extraction process it needs to override
	 * this method and return a new Visualizer instance
	 * 
	 * @return
	 */
	protected Visualizer createVisualizer(T image) {
		return null;
	}
}
