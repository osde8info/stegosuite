package org.stegosuite.application.embedding;

import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.format.ImageFormat;

public interface EmbeddingDoneListener {

	/**
	 * Is fired by the abstraction layer to let the Gui process the embedding result
	 * 
	 * @param embeddingMethod
	 * @param embeddedImage
	 */
	void onEmbeddingDone(EmbeddingMethod<? extends ImageFormat> embeddingMethod, ImageFormat embeddedImage);

}
