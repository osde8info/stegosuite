package org.stegosuite;

import org.eclipse.swt.graphics.ImageData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.stegosuite.application.StegosuitePresenter;
import org.stegosuite.application.StegosuiteUI;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.model.payload.block.FileBlock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static org.junit.Assert.*;
import static org.stegosuite.Resources.pathOf;

public class PresenterTest implements StegosuiteUI, Observer {
	private static final String IMAGE_NAME = "snow.bmp";
	private static final String EMBEDDED_IMAGE = "snow_embed.bmp";
	private static final String FILE_CONTENT = "The content of the file";
	private static final String FILE_EXTENSION = "txt";
	private static final String FILE_NAME = "file.txt";
	private final String message = "a message";
	private final String password = "a password";

	private int currentProgress;
	private EmbeddingProgress progressListener;
	private StegosuitePresenter presenter;

	private String outputPath;
	private ImageFormat embeddedImage;
	private Visualizer outputVisualizer;

	private String extractedMessage;
	private String statusMessage;
	private ImageData imageData;

	private String addedFileName;
	private String addedFileExtension;
	private long addedFileSize;

	@Before
	public void setUp() throws Exception {
		progressListener = new EmbeddingProgress();
		progressListener.addObserver(this);
	}

	@After
	public void tearDown() throws Exception {
		Resources.delete(EMBEDDED_IMAGE);
	}

	@Test
	public void embeddingTest() throws Exception {
		presenter = presenterWithImage(IMAGE_NAME);
		presenter.addMessageToPayload(message);
		presenter.embedNotifying(progressListener, password);

		assertEquals(100, currentProgress);
		assertNotNull(outputVisualizer);
		assertEquals(pathOf(IMAGE_NAME), embeddedImage.getFilePath());
		assertEquals(pathOf(EMBEDDED_IMAGE), outputPath);
		assertTrue(new File(outputPath).exists());
	}

	@Test
	public void extractTest() throws Exception {
		presenter = presenterWithImage(IMAGE_NAME);
		presenter.addMessageToPayload(message);
		presenter.embedNotifying(progressListener, password);
		resetCurrentProgress();

		presenter = presenterWithImage(EMBEDDED_IMAGE);
		presenter.extractNotifying(progressListener, password);

		assertEquals(message, extractedMessage);
		//assertEquals("Extracting completed.", statusMessage); TODO: use filePaths instead
		assertNotNull(outputVisualizer);
		assertArrayEquals(imageDataOf(EMBEDDED_IMAGE).data, imageData.data);
	}

	@Test
	public void extractFileTest() throws Exception {
		presenter = presenterWithImage(IMAGE_NAME);
		presenter.addFileToPayload(pathOf(FILE_NAME));
		presenter.addMessageToPayload(message);
		presenter.embedNotifying(progressListener, password);
		resetCurrentProgress();
		presenter = presenterWithImage(EMBEDDED_IMAGE);

		presenter.extractNotifying(progressListener, password);

		assertEquals(message, extractedMessage);
		//assertEquals("Extracting completed. Extracted file saved to "
		//		+ pathOf(FILE_NAME), statusMessage); TODO: use filePaths instead
		assertNotNull(outputVisualizer);
		assertArrayEquals(imageDataOf(EMBEDDED_IMAGE).data, imageData.data);
		assertEquals(FILE_NAME, addedFileName);
		assertEquals(FILE_EXTENSION, addedFileExtension);
		assertEquals(FILE_CONTENT.length(), fileBlockSize());
		assertEquals(FILE_CONTENT.length(), addedFileSize);
		assertEquals(FILE_CONTENT, readContentFromFile(FILE_NAME));
	}

	private long fileBlockSize() {
		return presenter.payloadFileBlocksWithFilename(FILE_NAME).stream()
				.findFirst().get().getSize();
	}

	private FileBlock getFileBlockFromPresenter() {
		return presenter.payloadFileBlocksWithFilename(FILE_NAME)
				.stream().findAny().get();
	}

	private StegosuitePresenter presenterWithImage(String imageName) throws SteganoImageException {
		ImageFormat image = getImage(imageName);
		return new StegosuitePresenter(image, this);
	}

	private ImageFormat getImage(String imageName) throws SteganoImageException {
		return ImageFormat.getImageFormat(pathOf(imageName));
	}

	private String readContentFromFile(String fileName) throws IOException {
		return Files.readAllLines(Paths.get(pathOf(fileName)))
					.stream()
					.reduce(String::concat)
					.get();
	}

	private void resetCurrentProgress() {
		currentProgress = 0;
	}

	private ImageData imageDataOf(String image) throws SteganoImageException {
		return getImage(image).getImageData();
	}

	@Override
	public void showEmbeddingError(SteganoEmbedException e) {
		throw new RuntimeException(e);
	}

	@Override
	public void showExtractingError(SteganoExtractException e) {
		throw new RuntimeException(e);
	}

	@Override
	public void extractingCompleted(String extractedMessage, List<String> filePaths, Visualizer visualizer, ImageData imageData) {
		this.extractedMessage = extractedMessage;
		//this.statusMessage = statusMessage; //TODO: Use filePaths instead of statusMessage
		this.outputVisualizer = visualizer;
		this.imageData = imageData;
	}

	@Override
	public void embeddingCompleted(ImageFormat embeddedImage, String outputPath, Visualizer visualizer) {
		this.outputPath = outputPath;
		this.embeddedImage = embeddedImage;
		this.outputVisualizer = visualizer;
	}

	@Override
	public void addPayloadFile(String filename, String extension, long fileSize) {
		addedFileName = filename;
		addedFileExtension = extension;
		addedFileSize = fileSize;
	}

	@Override
	public void update(Observable o, Object arg) {
		int notifiedProgress = (int) arg;
		assertTrue(notifiedProgress >= currentProgress);
		currentProgress = notifiedProgress;
	}
}
