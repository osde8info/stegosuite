package org.stegosuite.application.embedding;

import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.gif.GIFShuffle;
import org.stegosuite.image.embedding.gif.filter.GIFPointFilterNone;
import org.stegosuite.image.format.GIFImage;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.payload.Payload;

/**
 * Abstraction layer between the GUI and the embed/extract functions.
 */
public class MyGIFShuffle
		extends Embedding {

	private EmbeddingMethod<GIFImage> embeddable;

	public MyGIFShuffle(ImageFormat image) {
		embeddable = new GIFShuffle((GIFImage) image, new GIFPointFilterNone());
	}

	@Override
	public void embed(Payload payload, EmbeddingProgress progress, EmbeddingDoneListener listener)
			throws SteganoEmbedException {
		GIFImage embeddedImage = embeddable.embed(payload, progress);
		listener.onEmbeddingDone(embeddable, embeddedImage);
	}

	@Override
	public void extract(Payload payload, EmbeddingProgress progress, ExtractingDoneListener listener)
			throws SteganoExtractException {
		embeddable.extract(payload, progress);
		listener.onExtractingDone(embeddable);
	}

	@Override
	public int getCapacity() {
		return embeddable.capacity();
	}

	@Override
	public void setPointFilter(int a) {
		// no point filter used
	}

}
