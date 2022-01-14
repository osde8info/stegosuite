package org.stegosuite.image.embedding.jpg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.image.embedding.EmbeddingMethod;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.JPGImage;
import org.stegosuite.image.jpgtemp.james.JpegEncoder;
import org.stegosuite.image.jpgtemp.net.f5.Extract;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.model.payload.Payload;
import org.stegosuite.model.payload.PayloadEmbedder;
import org.stegosuite.model.payload.PayloadExtractor;
import org.stegosuite.model.payload.block.FileBlock;
import org.stegosuite.model.payload.block.MessageBlock;
import org.stegosuite.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class JPGF5
		extends EmbeddingMethod<JPGImage> {

	private static final Logger LOG = LoggerFactory.getLogger(JPGF5.class);

	public JPGF5(JPGImage image, PointFilter<JPGImage> pointFilter) {
		super(image, pointFilter);
	}

	@Override
	protected int doCapacity(JPGImage image) {
		JpegEncoder jpg = new JpegEncoder(image.getBufferedImage(), 80, null, null);

		return jpg.getCapacity();
	}

	@Override
	protected void doEmbed(JPGImage image, Payload payload, EmbeddingProgress progress)
			throws SteganoEmbedException {

		PayloadEmbedder embedder = new PayloadEmbedder(payload, this.capacity());

		final String comment = "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ";
		final int quality = 80; // 0 is worst, 100 is best

		ByteArrayOutputStream dataOut = new ByteArrayOutputStream();

		JpegEncoder jpg = new JpegEncoder(image.getBufferedImage(), quality, dataOut, comment);
		jpg.Compress(new ByteArrayInputStream(embedder.getPayloadBytes()), payload.getSteganoPassword());
		String outputPath = FileUtils.addFileNameSuffix(image.getFile().getAbsolutePath(), "_embed");

		FileOutputStream outputStream = null;

		//TODO: Move this saving functionality to ImageFormat.save()
		try {
			LOG.info("Saving jpg image to {}", outputPath);
			outputStream = new FileOutputStream(outputPath);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			dataOut.writeTo(outputStream);
			dataOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void doExtract(JPGImage image, Payload payload, EmbeddingProgress progress)
			throws SteganoExtractException {

		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		try {
			fis = new FileInputStream(image.getFile());
			baos = new ByteArrayOutputStream();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Extract.extract(fis, (int) image.getFile().length(), baos, payload.getSteganoPassword());
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] payloadBytes = null;
		try {
			baos.flush();

			payloadBytes = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PayloadExtractor extractor = new PayloadExtractor(payload);
		int i = 0;
		while (!extractor.finished() && i < payloadBytes.length) {
			extractor.processByte(payloadBytes[i++]);
		}
	}

	// TODO: Move this into tests
	public static void main(String[] args)
			throws SteganoImageException, SteganoEmbedException, SteganoExtractException, IOException {

		File carrierFile = new File("/home/tobi/stego/f5-steganography/sample/enc.jpg");
		File secretFileIn = new File("/home/tobi/stego/f5-steganography/sample/msg.txt");
		File secretFileOut = new File("/home/tobi/stego/f5-steganography/sample/msg_out.txt");
		File steganogramFile = new File("/home/tobi/stego/f5-steganography/sample/enc_embed.jpg");

		JPGImage jpgImage = new JPGImage();
		jpgImage.load(carrierFile);

		// EmbeddingMethod<JPGImage> embeddingMethod = new F5(jpgImage, null);
		JPGF5 embeddingMethod = new JPGF5(jpgImage, null);

		// Prepare payload to embed
		Payload payloadToEmbed = new Payload();

		payloadToEmbed.setSteganoPassword("123");

		// Disable payload encryption
		payloadToEmbed.setEncryptionPassword(null);

		// Add file to payload
		FileBlock fileBlock = new FileBlock(secretFileIn.getAbsolutePath());
		payloadToEmbed.addBlock(fileBlock);

		MessageBlock messageBlock = new MessageBlock("Some hidden message");
		payloadToEmbed.addBlock(messageBlock);

		// Embed
		embeddingMethod.embed(payloadToEmbed, null);

		// jpgImage.save(steganogramFile);

		Payload payloadExtracted = new Payload();
		payloadExtracted.setSteganoPassword("123");
		payloadExtracted.setEncryptionPassword(null);

		// Extract
		jpgImage = new JPGImage();
		jpgImage.load(steganogramFile);
		embeddingMethod = new JPGF5(jpgImage, null);

		embeddingMethod.extract(payloadExtracted, null);
		// Save extracted contents to file
		fileBlock = (FileBlock) payloadExtracted.getBlock(0);
		LOG.info("Original file name: " + fileBlock.getFileName());
		Files.write(secretFileOut.toPath(), fileBlock.getFileContent(), StandardOpenOption.CREATE);

		messageBlock = (MessageBlock) payloadExtracted.getBlock(1);
		LOG.info("Message: {}", messageBlock.getMessage());

	}
}
