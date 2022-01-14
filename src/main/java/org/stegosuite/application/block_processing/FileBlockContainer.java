package org.stegosuite.application.block_processing;

import org.stegosuite.model.payload.block.FileBlock;
import org.stegosuite.util.FileUtils;

import java.util.List;

class FileBlockContainer implements BlockContainer {
	private final FileBlock block;
	private final List<String> processedFiles;
	private final String baseFilePath;

	public FileBlockContainer(FileBlock block, List<String> processedFiles, String baseFilePath) {
		this.block = block;
		this.processedFiles = processedFiles;
		this.baseFilePath = baseFilePath;
	}

	@Override
	public void processBlock() {
		String extractionPath = getExtractionPath();
		block.saveFileTo(extractionPath);
		processedFiles.add(extractionPath);
	}

	private String getExtractionPath() {
		return FileUtils.changeFileName(
				baseFilePath,
				block.getFileName()
		);
	}
}
