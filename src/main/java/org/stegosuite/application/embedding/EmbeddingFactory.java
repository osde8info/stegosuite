package org.stegosuite.application.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.image.format.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates instances of implementations of Embedding.java
 */
public enum EmbeddingFactory {

	GIF_SORTED(MyGIFSortedColorTable.class), //
	GIFSHUFFLE(MyGIFShuffle.class), //
	BMP(MyBMPLsbMultiColorChannel.class), //
	JPG(MyJPGF5.class), //
	PNG(MyPNGLsbMultiColorChannel.class); //

	private static final Logger LOG = LoggerFactory.getLogger(EmbeddingFactory.class);

	/**
	 * Defines the default embedding method for each supported image type
	 */
	private static final Map<Class<? extends ImageFormat>, EmbeddingFactory> embeddingDefaults = new HashMap<>();

	static {
		embeddingDefaults.put(GIFImage.class, EmbeddingFactory.GIF_SORTED);
		embeddingDefaults.put(BMPImage.class, EmbeddingFactory.BMP);
		embeddingDefaults.put(JPGImage.class, EmbeddingFactory.JPG);
		embeddingDefaults.put(PNGImage.class, EmbeddingFactory.PNG);
	}

	private Class<? extends Embedding> embeddingClass = null;

	EmbeddingFactory(Class<? extends Embedding> embeddingClass) {
		this.embeddingClass = embeddingClass;
	}

	public Embedding newEmbedding(ImageFormat image) {
		try {
			Constructor<? extends Embedding> constructor = embeddingClass.getConstructor(ImageFormat.class);
			return constructor.newInstance(image);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error(e.getMessage());
		}
		return null;
	}

	public static Embedding getEmbedding(ImageFormat image) {
		// TODO: disabled until GIFShuffle works
		//if (image.getClass().equals(GIFImage.class)) {
		//	return EmbeddingFactory.GIFSHUFFLE.newEmbedding(image);
		//}
		EmbeddingFactory factory = embeddingDefaults.get(image.getClass());
		if (factory == null) {
			LOG.error("No embedding method found for {}", image.getClass().getName());
			return null;
		}
		return factory.newEmbedding(image);
	}

}
