package org.stegosuite.application.embedding;

import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.payload.Payload;

/**
 * Abstraction layer between the GUI and the embed/extract functions.
 */
public abstract class Embedding {

	public abstract void embed(Payload payload, EmbeddingProgress progress, EmbeddingDoneListener listener)
			throws SteganoEmbedException;

	public void embed(Payload payload, EmbeddingDoneListener event)
			throws SteganoEmbedException {
		this.embed(payload, null, event);
	}

	public abstract void extract(Payload payload, EmbeddingProgress progress, ExtractingDoneListener listener)
			throws SteganoExtractException;

	public void extract(Payload payload, ExtractingDoneListener listener)
			throws SteganoExtractException {
		this.extract(payload, null, listener);
	}

	public abstract int getCapacity();

	public abstract void setPointFilter(int a);

}
