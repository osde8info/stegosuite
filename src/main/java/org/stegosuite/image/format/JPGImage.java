package org.stegosuite.image.format;

import org.stegosuite.model.exception.SteganoImageException;

import java.io.File;

public class JPGImage
		extends ImageFormat {

	public static final String FILE_EXTENSION = "jpg";

	@Override
	public String getFileExtension() {
		return FILE_EXTENSION;
	}

	@Override
	public void save(File file)
			throws SteganoImageException {
		// temporally nothing because it's already done in embedding
	}

}
