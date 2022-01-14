package org.stegosuite.application.block_processing;

import org.stegosuite.model.payload.Payload;
import org.stegosuite.model.payload.block.Block;
import org.stegosuite.model.payload.block.FileBlock;
import org.stegosuite.model.payload.block.MessageBlock;

import java.util.ArrayList;
import java.util.List;

public class BlockProcessor {

	private final Payload payload;
	private final List<String> messages = new ArrayList<>();
	private final List<String> filePaths = new ArrayList<>();
	private final String baseFilePath;

	public BlockProcessor(Payload payload, String baseFilePath) {
		this.baseFilePath = baseFilePath;
		this.payload = payload;
	}

	public BlockProcessor processBlocks() {
		payload.getBlocks().stream().map(block -> toBlockContainer(block)).forEach(BlockContainer::processBlock);

		return this;
	}

	private BlockContainer toBlockContainer(Block aBlock) {
		switch (aBlock.getIdentifier()) {
			case FileBlock.IDENTIFIER:
				return new FileBlockContainer((FileBlock) aBlock, filePaths, baseFilePath);
			case MessageBlock.IDENTIFIER:
				return new MessageBlockContainer((MessageBlock) aBlock, messages);
			default:
				return null;
		}
	}

	public List<String> getFilePaths() {
		return filePaths;
	}

	public String getExtractedMessage() {
		if (messages.isEmpty()) {
			return null;
		} else {
			return messages.get(0);
		}

	}

	public String getStatusText() {
		String status = "Extracting completed.";
		if (thereWereProcessedFiles()) {
			status += " Extracted file saved to " + lastFilePath();
		}
		return status;
	}

	private boolean thereWereProcessedFiles() {
		return !filePaths.isEmpty();
	}

	private String lastFilePath() {
		return filePaths.get(filePaths.size() - 1);
	}

}
