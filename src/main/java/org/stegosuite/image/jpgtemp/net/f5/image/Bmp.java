package org.stegosuite.image.jpgtemp.net.f5.image;

import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Bmp {

	int iDataOffset;

	int pixel[] = null;

	BufferedInputStream imageFile;

	int bfSize;

	int bfOffBits;

	int biSize;

	int biWidth;

	int biHeight;

	int biPlanes;

	int biBitCount;

	int biCompression;

	int biSizeImage;

	int biXPelsPerMeter;

	int biYPelsPerMeter;

	int biClrUsed;

	int biClrImportant;

	public Bmp(final String fileName) {
		try {
			this.imageFile = new BufferedInputStream(new FileInputStream(fileName));
			readBitmapFileHeader();
			readBitmapInfoHeader();
			this.pixel = new int[this.biWidth * this.biHeight];
			int padding = 3 * this.biWidth % 4;
			if (padding > 0) {
				padding = 4 - padding;
			}
			int offset;
			for (int y = 1; y <= this.biHeight; y++) {
				offset = (this.biHeight - y) * this.biWidth;
				for (int x = 0; x < this.biWidth; x++) {
					this.pixel[offset + x] = readPixel();
				}
				for (int x = 0; x < padding; x++) {
					this.imageFile.read();
				}
			}
		} catch (final Exception e) {
			System.out.println(fileName + " is not a true colour file.");
			System.exit(1);
		}
	}

	public Image getImage() {
		MemoryImageSource mis;
		mis = new MemoryImageSource(this.biWidth, this.biHeight, this.pixel, 0, this.biWidth);
		return Toolkit.getDefaultToolkit().createImage(mis);
	}

	void readBitmapFileHeader()
			throws Exception {
		if (this.imageFile.read() != 'B') {
			throw new Exception();
		}
		if (this.imageFile.read() != 'M') {
			throw new Exception();
		}
		this.bfSize = readInt();
		readInt(); // ignore 4 bytes reserved
		this.bfOffBits = readInt();
	}

	void readBitmapInfoHeader()
			throws Exception {
		this.biSize = readInt();
		this.biWidth = readInt();
		this.biHeight = readInt();
		this.biPlanes = readShort();
		this.biBitCount = readShort();
		if (this.biBitCount != 24) {
			throw new Exception();
		}
		this.biCompression = readInt();
		this.biSizeImage = readInt();
		this.biXPelsPerMeter = readInt();
		this.biYPelsPerMeter = readInt();
		this.biClrUsed = readInt();
		this.biClrImportant = readInt();
	}

	int readInt()
			throws IOException {
		int retVal = 0;

		for (int i = 0; i < 4; i++) {
			retVal += (this.imageFile.read() & 0xff) << 8 * i;
		}
		return retVal;
	}

	int readPixel()
			throws IOException {
		int retVal = 0;

		for (int i = 0; i < 3; i++) {
			retVal += (this.imageFile.read() & 0xff) << 8 * i;
		}
		return retVal | 0xff000000;
	}

	int readShort()
			throws IOException {
		int retVal;

		retVal = this.imageFile.read() & 0xff;
		retVal += (this.imageFile.read() & 0xff) << 8;
		return retVal;
	}
}
