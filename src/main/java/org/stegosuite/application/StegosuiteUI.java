package org.stegosuite.application;

import java.util.List;

import org.eclipse.swt.graphics.ImageData;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;

public interface StegosuiteUI {
	void showEmbeddingError(SteganoEmbedException e);

	void showExtractingError(SteganoExtractException e);

	void extractingCompleted(String extractedMessage, List<String> filePaths, Visualizer visualizer, ImageData imageData);

	void embeddingCompleted(ImageFormat embeddedImage, String outputPath, Visualizer visualizer);

	void addPayloadFile(String filename, String extension, long fileSize);
}
