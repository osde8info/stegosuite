package org.stegosuite.model.payload.block;

import org.stegosuite.util.ByteUtils;
import org.stegosuite.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.nio.file.StandardOpenOption.CREATE;

/**
 * This block contains the contents of a file as well as its file name
 */
public final class FileBlock
		extends Block {

	/**
	 * Mandatory empty constructor
	 * @see org.stegosuite.model.payload.Payload#unpack(byte[])
	 */
	@SuppressWarnings("unused")
	public FileBlock() {}

	/**
	 * Unique number among all Block implementations
	 */
	public static final byte IDENTIFIER = 1;

	/**
	 * Contains the file name without path
	 */
	private String fileName = null;

	/**
	 * Contains the bytes of the file
	 */
	private byte[] fileContent = null;

	public FileBlock(String fileName) {
		this.fileName = fileName;
		try {
			fileContent = Files.readAllBytes(new File(fileName).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public byte[] pack() {
		byte[] fileNameWithoutPath = new File(fileName).getName().getBytes(StandardCharsets.UTF_8);
		byte[] fileNameLength = ByteUtils.intToBytes(fileNameWithoutPath.length);
		return ByteUtils.concat(fileNameLength, fileNameWithoutPath, fileContent);
	}

	@Override
	public void unpack(byte[] data) {
		int fileNameLength = ByteBuffer.wrap(data).getInt();
		fileName = new String(Arrays.copyOfRange(data, 4, 4 + fileNameLength));
		fileContent = Arrays.copyOfRange(data, 4 + fileNameLength, data.length);
	}

	public String getFileName() {
		return fileName;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public long getSize() {
		return FileUtils.getFileSize(fileName);
	}

	public boolean hasPath(String filename) {
		String blockFilename = Paths.get(fileName).getFileName().toString();
		return filename.equals(blockFilename);
	}

	public void saveFileTo(String extractionPath) {
		try {
			writeContentToFileIn(extractionPath);
			// Update the filename, so that we can compute the file size later
			fileName = extractionPath;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeContentToFileIn(String extractionPath) throws IOException {
		Path path = Paths.get(extractionPath);
		Files.write(path, fileContent, CREATE);
	}
}
