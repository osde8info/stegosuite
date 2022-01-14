// Version 1.0a
// Copyright (C) 1998, James R. Weeks and BioElectroMech.
// Visit BioElectroMech at www.obrador.com. Email James@obrador.com.

// See license.txt for details about the allowed used of this software.
// This software is based in part on the work of the Independent JPEG Group.
// See IJGreadme.txt for details about the Independent JPEG Group's license.

// This encoder is inspired by the Java Jpeg encoder by Florian Raemy,
// studwww.eurecom.fr/~raemy.
// It borrows a great deal of code and structure from the Independent
// Jpeg Group's Jpeg 6a library, Copyright Thomas G. Lane.
// See license.txt for details.

package org.stegosuite.image.jpgtemp.james;

import java.awt.*;
import java.awt.image.PixelGrabber;

/**
 * JpegInfo - Given an image, sets default information about it and divides it into its constituant
 * components, downsizing those that need to be.
 */
class JpegInfo {

	String Comment;

	public Image imageobj;

	public int imageHeight;

	public int imageWidth;

	public int BlockWidth[];

	public int BlockHeight[];

	// the following are set as the default
	public int Precision = 8;

	public int NumberOfComponents = 3;

	public Object Components[];

	public int[] CompID = { 1, 2, 3 };

	// public int[] HsampFactor = {1, 1, 1};
	// public int[] VsampFactor = {1, 1, 1};
	public int[] HsampFactor = { 2, 1, 1 };

	public int[] VsampFactor = { 2, 1, 1 };

	public int[] QtableNumber = { 0, 1, 1 };

	public int[] DCtableNumber = { 0, 1, 1 };

	public int[] ACtableNumber = { 0, 1, 1 };

	public boolean[] lastColumnIsDummy = { false, false, false };

	public boolean[] lastRowIsDummy = { false, false, false };

	public int Ss = 0;

	public int Se = 63;

	public int Ah = 0;

	public int Al = 0;

	public int compWidth[], compHeight[];

	public int MaxHsampFactor;

	public int MaxVsampFactor;

	public JpegInfo(final Image image, final String comment) {
		this.Components = new Object[this.NumberOfComponents];
		this.compWidth = new int[this.NumberOfComponents];
		this.compHeight = new int[this.NumberOfComponents];
		this.BlockWidth = new int[this.NumberOfComponents];
		this.BlockHeight = new int[this.NumberOfComponents];
		this.imageobj = image;
		this.imageWidth = image.getWidth(null);
		this.imageHeight = image.getHeight(null);
		// Comment =
		// "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech. ";
		this.Comment = comment;
		getYCCArray();
	}

	float[][] DownSample(final float[][] C, final int comp) {
		int inrow, incol;
		int outrow, outcol;
		float output[][];
		float temp;
		int bias;
		inrow = 0;
		incol = 0;
		output = new float[this.compHeight[comp]][this.compWidth[comp]];
		for (outrow = 0; outrow < this.compHeight[comp]; outrow++) {
			bias = 1;
			for (outcol = 0; outcol < this.compWidth[comp]; outcol++) {
				// System.out.println("outcol="+outcol);
				temp = C[inrow][incol++]; // 00
				temp += C[inrow++][incol--]; // 01
				temp += C[inrow][incol++]; // 10
				temp += C[inrow--][incol++] + bias; // 11 -> 02
				output[outrow][outcol] = temp / (float) 4.0;
				bias ^= 3;
			}
			inrow += 2;
			incol = 0;
		}
		return output;
	}

	public String getComment() {
		return this.Comment;
	}

	/*
	 * This method creates and fills three arrays, Y, Cb, and Cr using the input image.
	 */

	private void getYCCArray() {
		final int values[] = new int[this.imageWidth * this.imageHeight];
		int r, g, b, y, x;
		// In order to minimize the chance that grabPixels will throw an
		// exception
		// it may be necessary to grab some pixels every few scanlines and
		// process
		// those before going for more. The time expense may be prohibitive.
		// However, for a situation where memory overhead is a concern, this may
		// be
		// the only choice.
		final PixelGrabber grabber = new PixelGrabber(this.imageobj.getSource(), 0, 0, this.imageWidth,
				this.imageHeight, values, 0, this.imageWidth);
		this.MaxHsampFactor = 1;
		this.MaxVsampFactor = 1;
		for (y = 0; y < this.NumberOfComponents; y++) {
			this.MaxHsampFactor = Math.max(this.MaxHsampFactor, this.HsampFactor[y]);
			this.MaxVsampFactor = Math.max(this.MaxVsampFactor, this.VsampFactor[y]);
		}
		for (y = 0; y < this.NumberOfComponents; y++) {
			this.compWidth[y] = (this.imageWidth % 8 != 0 ? (int) Math.ceil(this.imageWidth / 8.0) * 8
					: this.imageWidth) / this.MaxHsampFactor * this.HsampFactor[y];
			if (this.compWidth[y] != this.imageWidth / this.MaxHsampFactor * this.HsampFactor[y]) {
				this.lastColumnIsDummy[y] = true;
			}
			// results in a multiple of 8 for compWidth
			// this will make the rest of the program fail for the unlikely
			// event that someone tries to compress an 16 x 16 pixel image
			// which would of course be worse than pointless
			this.BlockWidth[y] = (int) Math.ceil(this.compWidth[y] / 8.0);
			this.compHeight[y] = (this.imageHeight % 8 != 0 ? (int) Math.ceil(this.imageHeight / 8.0) * 8
					: this.imageHeight) / this.MaxVsampFactor * this.VsampFactor[y];
			if (this.compHeight[y] != this.imageHeight / this.MaxVsampFactor * this.VsampFactor[y]) {
				this.lastRowIsDummy[y] = true;
			}
			this.BlockHeight[y] = (int) Math.ceil(this.compHeight[y] / 8.0);
		}
		try {
			if (grabber.grabPixels() != true) {
				try {
					throw new AWTException("Grabber returned false: " + grabber.getStatus());
				} catch (final Exception e) {}
				;
			}
		} catch (final InterruptedException e) {}
		;
		final float Y[][] = new float[this.compHeight[0]][this.compWidth[0]];
		final float Cr1[][] = new float[this.compHeight[0]][this.compWidth[0]];
		final float Cb1[][] = new float[this.compHeight[0]][this.compWidth[0]];
		float Cb2[][] = new float[this.compHeight[1]][this.compWidth[1]];
		float Cr2[][] = new float[this.compHeight[2]][this.compWidth[2]];
		int index = 0;
		for (y = 0; y < this.imageHeight; ++y) {
			for (x = 0; x < this.imageWidth; ++x) {
				r = values[index] >> 16 & 0xff;
				g = values[index] >> 8 & 0xff;
				b = values[index] & 0xff;

				// The following three lines are a more correct color conversion
				// but
				// the current conversion technique is sufficient and results in
				// a higher
				// compression rate.
				// Y[y][x] = 16 + (float)(0.8588*(0.299 * (float)r + 0.587 *
				// (float)g + 0.114 * (float)b ));
				// Cb1[y][x] = 128 + (float)(0.8784*(-0.16874 * (float)r -
				// 0.33126 * (float)g + 0.5 * (float)b));
				// Cr1[y][x] = 128 + (float)(0.8784*(0.5 * (float)r - 0.41869 *
				// (float)g - 0.08131 * (float)b));
				Y[y][x] = (float) (0.299 * r + 0.587 * g + 0.114 * b);
				Cb1[y][x] = 128 + (float) (-0.16874 * r - 0.33126 * g + 0.5 * b);
				Cr1[y][x] = 128 + (float) (0.5 * r - 0.41869 * g - 0.08131 * b);
				index++;
			}
		}

		// Need a way to set the H and V sample factors before allowing
		// downsampling.
		// For now (04/04/98) downsampling must be hard coded.
		// Until a better downsampler is implemented, this will not be done.
		// Downsampling is currently supported. The downsampling method here
		// is a simple box filter.

		this.Components[0] = Y;
		Cb2 = DownSample(Cb1, 1);
		this.Components[1] = Cb2;
		Cr2 = DownSample(Cr1, 2);
		this.Components[2] = Cr2;
	}

	public void setComment(final String comment) {
		this.Comment.concat(comment);
	}
}
