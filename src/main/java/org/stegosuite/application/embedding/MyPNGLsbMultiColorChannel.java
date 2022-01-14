package org.stegosuite.application.embedding;

import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.png.PNGLsbMultiColorChannel;
import org.stegosuite.image.embedding.png.filter.PNGPointFilterHomogeneous;
import org.stegosuite.image.embedding.png.filter.PNGPointFilterNone;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.image.format.PNGImage;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.payload.Payload;

/**
 * Abstraction layer between the GUI and the embed/extract functions.
 */
public class MyPNGLsbMultiColorChannel
		extends Embedding {

	private EmbeddingMethod<PNGImage> embeddable;
	private EmbeddingMethod<PNGImage> embeddablePoint;
	private EmbeddingMethod<PNGImage> embeddableNoPoint;

	public MyPNGLsbMultiColorChannel(ImageFormat image) {
		embeddableNoPoint = new PNGLsbMultiColorChannel((PNGImage) image, new PNGPointFilterNone());
		embeddablePoint = new PNGLsbMultiColorChannel((PNGImage) image, new PNGPointFilterHomogeneous());
		embeddable = embeddableNoPoint;
	}

	@Override
	public void embed(Payload payload, EmbeddingProgress progress, EmbeddingDoneListener listener)
			throws SteganoEmbedException {
		PNGImage embeddedImage = embeddable.embed(payload, progress);
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
		if (a == 0) {
			embeddable = embeddableNoPoint;
		} else if (a == 1) {
			embeddable = embeddablePoint;
		}
	}

}
