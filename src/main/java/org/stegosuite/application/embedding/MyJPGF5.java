package org.stegosuite.application.embedding;

import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.jpg.JPGF5;
import org.stegosuite.image.embedding.jpg.filter.JPGPointFilterNone;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.image.format.JPGImage;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.payload.Payload;

/**
 * Abstraction layer between the GUI and the embed/extract functions.
 */
public class MyJPGF5
		extends Embedding {

	private EmbeddingMethod<JPGImage> embeddable;

	public MyJPGF5(ImageFormat image) {
		embeddable = new JPGF5((JPGImage) image, new JPGPointFilterNone());
	}

	@Override
	public void embed(Payload payload, EmbeddingProgress progress, EmbeddingDoneListener listener)
			throws SteganoEmbedException {
		JPGImage embeddedImage = embeddable.embed(payload, progress);
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
