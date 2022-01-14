package org.stegosuite.application.embedding;

import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.bmp.BMPLsbMultiColorChannel;
import org.stegosuite.image.embedding.bmp.filter.BMPPointFilterHomogeneous;
import org.stegosuite.image.embedding.bmp.filter.BMPPointFilterNone;
import org.stegosuite.image.format.BMPImage;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.payload.Payload;

/**
 * Abstraction layer between the GUI and the embed/extract functions.
 */
public class MyBMPLsbMultiColorChannel
		extends Embedding {

	private EmbeddingMethod<BMPImage> embeddable;
	private EmbeddingMethod<BMPImage> embeddablePoint;
	private EmbeddingMethod<BMPImage> embeddableNoPoint;

	public MyBMPLsbMultiColorChannel(ImageFormat image) {
		embeddableNoPoint = new BMPLsbMultiColorChannel((BMPImage) image, new BMPPointFilterNone());
		embeddablePoint = new BMPLsbMultiColorChannel((BMPImage) image, new BMPPointFilterHomogeneous());
		embeddable = embeddableNoPoint;
	}

	@Override
	public void embed(Payload payload, EmbeddingProgress progress, EmbeddingDoneListener listener)
			throws SteganoEmbedException {
		BMPImage embeddedImage = embeddable.embed(payload, progress);
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
