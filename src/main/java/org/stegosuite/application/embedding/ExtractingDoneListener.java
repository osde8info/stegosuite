package org.stegosuite.application.embedding;

import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.format.ImageFormat;

public interface ExtractingDoneListener {

	/**
	 * Is fired by the abstraction layer to let the Gui process the extraction result
	 * 
	 * @param embeddingMethod
	 */
	void onExtractingDone(EmbeddingMethod<? extends ImageFormat> embeddingMethod);

}
