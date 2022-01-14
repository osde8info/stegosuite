package org.stegosuite.ui.cli;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.eclipse.swt.graphics.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.application.StegosuitePresenter;
import org.stegosuite.application.StegosuiteUI;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.ui.gui.ImageUtils;

public class Cli
		implements
		StegosuiteUI {

	private static final Logger LOG = LoggerFactory.getLogger(Cli.class);
	private ImageFormat image;
	private StegosuitePresenter presenter;

	private String getSteganogramPath(CommandLine cmd) {
		if (cmd.getArgs().length > 0) {
			return cmd.getArgs()[0];
		} else {
			return null;
		}
	}

	public void embed(CommandLine cmd) {
		String steganogramPath = getSteganogramPath(cmd);
		if (steganogramPath == null)
			return;
		if (!validImageFormat(steganogramPath))
			return;

		pointFilter(cmd);

		String message = cmd.getOptionValue("m");
		if (message != null) {
			presenter.addMessageToPayload(message);
		}

		String[] files = cmd.getOptionValues("f");
		if (files != null) {
			for (String string : files) {
				presenter.addFileToPayload(string);
			}
		}

		String key = cmd.getOptionValue("k");

		embed(key);
	}

	private void embed(String key) {
		LOG.info("Embedding data...");
		presenter.embed(key);
	}

	public void extract(CommandLine cmd) {
		String steganogramPath = getSteganogramPath(cmd);
		if (steganogramPath == null)
			return;
		if (!validImageFormat(steganogramPath))
			return;

		pointFilter(cmd);
		String key = cmd.getOptionValue("k");
		extract(key);
	}

	private void extract(String key) {
		LOG.info("Extracting data...");
		presenter.extractUsing(key);
	}

	public void capacity(CommandLine cmd) {
		String steganogramPath = getSteganogramPath(cmd);
		if (steganogramPath == null)
			return;
		if (!validImageFormat(steganogramPath))
			return;
		pointFilter(cmd);
		int capacity = presenter.getEmbeddingCapacity();
		LOG.info("Capacity: {}", ImageUtils.formatSize(capacity));
	}

	private void pointFilter(CommandLine cmd) {
		if (cmd.hasOption("disable-noise-detection")) {
			presenter.setPointFilter(0);
		} else {
			presenter.setPointFilter(1);
		}
	}

	private boolean validImageFormat(String steganogramPath) {
		image = getImageFormat(steganogramPath);
		if (image == null) {
			showFormatNotSupportedError();
			return false;
		}
		presenter = new StegosuitePresenter(image, this);
		return true;
	}

	private ImageFormat getImageFormat(String steganogramPath) {
		try {
			return ImageFormat.getImageFormat(steganogramPath);
		} catch (SteganoImageException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void showFormatNotSupportedError() {
		LOG.error("Error: Currently only these file types are supported: {}", supportedFormats());
	}

	private String supportedFormats() {
		return String.join(", ", ImageFormat.getSupportedFormats());
	}

	@Override
	public void showEmbeddingError(SteganoEmbedException e) {
		LOG.info(e.getMessage());
	}

	@Override
	public void showExtractingError(SteganoExtractException e) {
		e.printStackTrace();
	}

	@Override
	public void extractingCompleted(String extractedMessage, List<String> filePaths, Visualizer visualizer,
			ImageData imageData) {
		LOG.info("Extracting completed");
		if (extractedMessage != null) {
			LOG.info("Extracted message: {}", extractedMessage);
		}
		if (!filePaths.isEmpty()) {
			for (String string : filePaths) {
				LOG.info("Extracted file saved to {}", string);
			}
		}
	}

	@Override
	public void embeddingCompleted(ImageFormat embeddedImage, String outputPath, Visualizer visualizer) {}

	@Override
	public void addPayloadFile(String filename, String extension, long fileSize) {}
}
