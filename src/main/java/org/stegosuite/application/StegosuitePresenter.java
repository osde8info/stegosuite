package org.stegosuite.application;

import org.eclipse.swt.graphics.ImageData;
import org.stegosuite.application.block_processing.BlockProcessor;
import org.stegosuite.application.embedding.Embedding;
import org.stegosuite.application.embedding.EmbeddingDoneListener;
import org.stegosuite.application.embedding.EmbeddingFactory;
import org.stegosuite.application.embedding.ExtractingDoneListener;
import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.model.payload.Payload;
import org.stegosuite.model.payload.block.FileBlock;
import org.stegosuite.model.payload.block.MessageBlock;
import org.stegosuite.util.FileUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class StegosuitePresenter implements EmbeddingDoneListener, ExtractingDoneListener {
	private ImageFormat image;
	private Payload payload;
	private Embedding embedding;
	private StegosuiteUI ui;
	private EmbeddingProgress progressListener = new EmbeddingProgress();

	public StegosuitePresenter(ImageFormat image, StegosuiteUI ui) {
		this.image = image;
		this.ui = ui;
		this.payload = new Payload();
		this.embedding = embeddingFor(image);
	}

	public void setPointFilter(int value) {
		this.embedding.setPointFilter(value);
	}
	
	private Embedding embeddingFor(ImageFormat image) {
		Embedding embedding = EmbeddingFactory.getEmbedding(image);
		embedding.setPointFilter(1);
		return embedding;
	}

	public void embedNotifying(EmbeddingProgress progressListener, String password) {
		this.progressListener = progressListener;

		embed(password);
	}

	public void embed(String password) {
		try {
			setPassword(password);
			embedData();
		} catch (SteganoEmbedException e) {
			ui.showEmbeddingError(e);
		}
	}

	public void addMessageToPayload(String message) {
		payload.addBlock(new MessageBlock(message));
	}
	
	public void addFileToPayload(String filename) {
		payload.addBlock(new FileBlock(filename));
		notifyAddedFile(filename);
	}
	
	private void setPassword(String password) {
		payload.setPassword(password);
	}
	
	private void embedData() throws SteganoEmbedException {
		embedding.embed(payload, progressListener, this);
	}

	@Override
	public void onEmbeddingDone(EmbeddingMethod<? extends ImageFormat> embeddingMethod, ImageFormat embeddedImage) {
		save(embeddedImage);
		notifyEmbeddingCompleted(embeddingMethod, embeddedImage);
	}

	private void save(ImageFormat embeddedImage) {
		try {
			String outputPath = getOutputPathFor(embeddedImage);
			embeddedImage.save(new File(outputPath));
		} catch (SteganoImageException e) {
			e.printStackTrace();
		}
	}

	private void notifyEmbeddingCompleted(EmbeddingMethod<? extends ImageFormat> embeddingMethod, ImageFormat embeddedImage) {
		Visualizer visualizer = embeddingMethod.getVisualizer();
		String outputPath = getOutputPathFor(embeddedImage);

		ui.embeddingCompleted(embeddedImage, outputPath, visualizer);
	}

	private String getOutputPathFor(ImageFormat embeddedImage) {
		return FileUtils.addFileNameSuffix(embeddedImage.getFilePath(), "_embed");
	}

	public void extractNotifying(EmbeddingProgress progressListener, String password) {
		this.progressListener = progressListener;

		extractUsing(password);
	}

	public void extractUsing(String password) {
		payload.setPassword(password);
		try {
			embedding.extract(payload, progressListener, this);
		} catch (SteganoExtractException e) {
			ui.showExtractingError(e);
		}
	}

	@Override
	public void onExtractingDone(EmbeddingMethod<? extends ImageFormat> embeddingMethod) {
		BlockProcessor blockProcessor = new BlockProcessor(payload, image.getFilePath()).processBlocks();
		notifyAddedFiles(blockProcessor.getFilePaths());
		notifyExtractingCompleted(embeddingMethod,
				blockProcessor.getExtractedMessage(),
				blockProcessor.getFilePaths()
		);
	}

	private void notifyAddedFiles(List<String> addedFilePaths) {
		addedFilePaths.forEach(path -> notifyAddedFile(path));
	}

	private void notifyExtractingCompleted(EmbeddingMethod<? extends ImageFormat> embeddingMethod, String extractedMessage, List<String> filePaths) {
		Visualizer visualizer = embeddingMethod.getVisualizer();
		ImageData imageData = image.getImageData();
		ui.extractingCompleted(extractedMessage, filePaths, visualizer, imageData);
	}

	private void notifyAddedFile(String filePath) {
		String filename = FileUtils.getFileName(filePath);
		String extension = FileUtils.getFileExtension(filePath);
		long fileSize = FileUtils.getFileSize(filePath);

		ui.addPayloadFile(filename, extension, fileSize);
	}

	public List<FileBlock> payloadFileBlocksWithFilename(String filename) {
		return payload.getBlocks().stream()
                .filter(block -> block.hasIdentifier(FileBlock.IDENTIFIER))
                .map(block -> (FileBlock) block)
                .filter(fileBlock -> fileBlock.hasPath(filename))
                .collect(Collectors.toList());
	}

	public void removeBlock(FileBlock fileBlock) {
		payload.removeBlock(fileBlock);
	}

	public void clearPayload() {
		this.payload = new Payload();
	}

	public int getEmbeddingCapacity() {
		return embedding.getCapacity();
	}

}
