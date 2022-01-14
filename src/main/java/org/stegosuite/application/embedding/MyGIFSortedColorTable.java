package org.stegosuite.application.embedding;

import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.gif.GIFSortedColorTable;
import org.stegosuite.image.embedding.gif.filter.GIFPointFilterHomogeneous;
import org.stegosuite.image.embedding.gif.filter.GIFPointFilterNone;
import org.stegosuite.image.format.GIFImage;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.payload.Payload;

/**
 * Abstraction layer between the GUI and the embed/extract functions.
 */
public class MyGIFSortedColorTable
		extends Embedding {

	private EmbeddingMethod<GIFImage> embeddable;
	private EmbeddingMethod<GIFImage> embeddablePoint;
	private EmbeddingMethod<GIFImage> embeddableNoPoint;

	public MyGIFSortedColorTable(ImageFormat image) {
		embeddableNoPoint = new GIFSortedColorTable((GIFImage) image, new GIFPointFilterNone());
		embeddablePoint = new GIFSortedColorTable((GIFImage) image, new GIFPointFilterHomogeneous());
		embeddable = embeddableNoPoint;
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
		if (a == 0) {
			embeddable = embeddableNoPoint;
		} else if (a == 1) {
			embeddable = embeddablePoint;
		}
	}

}
