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

/**
 * DCT - A Java implementation of the Discreet Cosine Transform
 */
@SuppressWarnings("unused")
class DCT {

	/**
	 * DCT Block Size - default 8
	 */
	public int N = 8;

	/**
	 * Image Quality (0-100) - default 80 (good image / good compression)
	 */
	public int QUALITY = 80;

	public Object quantum[] = new Object[2];

	public Object Divisors[] = new Object[2];

	/**
	 * Quantitization Matrix for luminace.
	 */
	public int quantum_luminance[] = new int[this.N * this.N];

	public double DivisorsLuminance[] = new double[this.N * this.N];

	/**
	 * Quantitization Matrix for chrominance.
	 */
	public int quantum_chrominance[] = new int[this.N * this.N];

	public double DivisorsChrominance[] = new double[this.N * this.N];

	/**
	 * Constructs a new DCT object. Initializes the cosine transform matrix these are used when
	 * computing the DCT and it's inverse. This also initializes the run length counters and the
	 * ZigZag sequence. Note that the image quality can be worse than 25 however the image will be
	 * extemely pixelated, usually to a block size of N.
	 * 
	 * @param QUALITY The quality of the image (0 worst - 100 best)
	 */
	public DCT(final int QUALITY) {
		initMatrix(QUALITY);
	}

	/*
	 * This method preforms a DCT on a block of image data using the AAN method as implemented in
	 * the IJG Jpeg-6a library.
	 */
	public double[][] forwardDCT(final float input[][]) {
		final double output[][] = new double[this.N][this.N];
		double tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
		double tmp10, tmp11, tmp12, tmp13;
		double z1, z2, z3, z4, z5, z11, z13;
		int i;
		int j;

		// Subtracts 128 from the input values
		for (i = 0; i < 8; i++) {
			for (j = 0; j < 8; j++) {
				output[i][j] = input[i][j] - 128.0;
				// input[i][j] -= 128;

			}
		}

		for (i = 0; i < 8; i++) {
			tmp0 = output[i][0] + output[i][7];
			tmp7 = output[i][0] - output[i][7];
			tmp1 = output[i][1] + output[i][6];
			tmp6 = output[i][1] - output[i][6];
			tmp2 = output[i][2] + output[i][5];
			tmp5 = output[i][2] - output[i][5];
			tmp3 = output[i][3] + output[i][4];
			tmp4 = output[i][3] - output[i][4];

			tmp10 = tmp0 + tmp3;
			tmp13 = tmp0 - tmp3;
			tmp11 = tmp1 + tmp2;
			tmp12 = tmp1 - tmp2;

			output[i][0] = tmp10 + tmp11;
			output[i][4] = tmp10 - tmp11;

			z1 = (tmp12 + tmp13) * 0.707106781;
			output[i][2] = tmp13 + z1;
			output[i][6] = tmp13 - z1;

			tmp10 = tmp4 + tmp5;
			tmp11 = tmp5 + tmp6;
			tmp12 = tmp6 + tmp7;

			z5 = (tmp10 - tmp12) * 0.382683433;
			z2 = 0.541196100 * tmp10 + z5;
			z4 = 1.306562965 * tmp12 + z5;
			z3 = tmp11 * 0.707106781;

			z11 = tmp7 + z3;
			z13 = tmp7 - z3;

			output[i][5] = z13 + z2;
			output[i][3] = z13 - z2;
			output[i][1] = z11 + z4;
			output[i][7] = z11 - z4;
		}

		for (i = 0; i < 8; i++) {
			tmp0 = output[0][i] + output[7][i];
			tmp7 = output[0][i] - output[7][i];
			tmp1 = output[1][i] + output[6][i];
			tmp6 = output[1][i] - output[6][i];
			tmp2 = output[2][i] + output[5][i];
			tmp5 = output[2][i] - output[5][i];
			tmp3 = output[3][i] + output[4][i];
			tmp4 = output[3][i] - output[4][i];

			tmp10 = tmp0 + tmp3;
			tmp13 = tmp0 - tmp3;
			tmp11 = tmp1 + tmp2;
			tmp12 = tmp1 - tmp2;

			output[0][i] = tmp10 + tmp11;
			output[4][i] = tmp10 - tmp11;

			z1 = (tmp12 + tmp13) * 0.707106781;
			output[2][i] = tmp13 + z1;
			output[6][i] = tmp13 - z1;

			tmp10 = tmp4 + tmp5;
			tmp11 = tmp5 + tmp6;
			tmp12 = tmp6 + tmp7;

			z5 = (tmp10 - tmp12) * 0.382683433;
			z2 = 0.541196100 * tmp10 + z5;
			z4 = 1.306562965 * tmp12 + z5;
			z3 = tmp11 * 0.707106781;

			z11 = tmp7 + z3;
			z13 = tmp7 - z3;

			output[5][i] = z13 + z2;
			output[3][i] = z13 - z2;
			output[1][i] = z11 + z4;
			output[7][i] = z11 - z4;
		}

		return output;
	}

	/*
	 * This method preforms forward DCT on a block of image data using the literal method specified
	 * for a 2-D Discrete Cosine Transform. It is included as a curiosity and can give you an idea
	 * of the difference in the compression result (the resulting image quality) by comparing its
	 * output to the output of the AAN method below. It is ridiculously inefficient.
	 */

	// For now the final output is unusable. The associated quantization step
	// needs some tweaking. If you get this part working, please let me know.

	public double[][] forwardDCTExtreme(final float input[][]) {
		final double output[][] = new double[this.N][this.N];
		final double tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
		final double tmp10, tmp11, tmp12, tmp13;
		final double z1, z2, z3, z4, z5, z11, z13;
		final int i;
		final int j;
		int v, u, x, y;
		for (v = 0; v < 8; v++) {
			for (u = 0; u < 8; u++) {
				for (x = 0; x < 8; x++) {
					for (y = 0; y < 8; y++) {
						output[v][u] += input[x][y] * Math.cos((double) (2 * x + 1) * (double) u * Math.PI / 16)
								* Math.cos((double) (2 * y + 1) * (double) v * Math.PI / 16);
					}
				}
				output[v][u] *= 0.25 * (u == 0 ? 1.0 / Math.sqrt(2) : (double) 1.0)
						* (v == 0 ? 1.0 / Math.sqrt(2) : (double) 1.0);
			}
		}
		return output;
	}

	/*
	 * This method sets up the quantization matrix for luminance and chrominance using the Quality
	 * parameter.
	 */
	private void initMatrix(final int quality) {
		final double[] AANscaleFactor = { 1.0, 1.387039845, 1.306562965, 1.175875602, 1.0, 0.785694958, 0.541196100,
				0.275899379 };
		int i;
		int j;
		int index;
		int Quality;
		int temp;

		// converting quality setting to that specified in the
		// jpeg_quality_scaling
		// method in the IJG Jpeg-6a C libraries

		Quality = quality;
		if (Quality <= 0) {
			Quality = 1;
		}
		if (Quality > 100) {
			Quality = 100;
		}
		if (Quality < 50) {
			Quality = 5000 / Quality;
		} else {
			Quality = 200 - Quality * 2;
		}

		// Creating the luminance matrix

		this.quantum_luminance[0] = 16;
		this.quantum_luminance[1] = 11;
		this.quantum_luminance[2] = 10;
		this.quantum_luminance[3] = 16;
		this.quantum_luminance[4] = 24;
		this.quantum_luminance[5] = 40;
		this.quantum_luminance[6] = 51;
		this.quantum_luminance[7] = 61;
		this.quantum_luminance[8] = 12;
		this.quantum_luminance[9] = 12;
		this.quantum_luminance[10] = 14;
		this.quantum_luminance[11] = 19;
		this.quantum_luminance[12] = 26;
		this.quantum_luminance[13] = 58;
		this.quantum_luminance[14] = 60;
		this.quantum_luminance[15] = 55;
		this.quantum_luminance[16] = 14;
		this.quantum_luminance[17] = 13;
		this.quantum_luminance[18] = 16;
		this.quantum_luminance[19] = 24;
		this.quantum_luminance[20] = 40;
		this.quantum_luminance[21] = 57;
		this.quantum_luminance[22] = 69;
		this.quantum_luminance[23] = 56;
		this.quantum_luminance[24] = 14;
		this.quantum_luminance[25] = 17;
		this.quantum_luminance[26] = 22;
		this.quantum_luminance[27] = 29;
		this.quantum_luminance[28] = 51;
		this.quantum_luminance[29] = 87;
		this.quantum_luminance[30] = 80;
		this.quantum_luminance[31] = 62;
		this.quantum_luminance[32] = 18;
		this.quantum_luminance[33] = 22;
		this.quantum_luminance[34] = 37;
		this.quantum_luminance[35] = 56;
		this.quantum_luminance[36] = 68;
		this.quantum_luminance[37] = 109;
		this.quantum_luminance[38] = 103;
		this.quantum_luminance[39] = 77;
		this.quantum_luminance[40] = 24;
		this.quantum_luminance[41] = 35;
		this.quantum_luminance[42] = 55;
		this.quantum_luminance[43] = 64;
		this.quantum_luminance[44] = 81;
		this.quantum_luminance[45] = 104;
		this.quantum_luminance[46] = 113;
		this.quantum_luminance[47] = 92;
		this.quantum_luminance[48] = 49;
		this.quantum_luminance[49] = 64;
		this.quantum_luminance[50] = 78;
		this.quantum_luminance[51] = 87;
		this.quantum_luminance[52] = 103;
		this.quantum_luminance[53] = 121;
		this.quantum_luminance[54] = 120;
		this.quantum_luminance[55] = 101;
		this.quantum_luminance[56] = 72;
		this.quantum_luminance[57] = 92;
		this.quantum_luminance[58] = 95;
		this.quantum_luminance[59] = 98;
		this.quantum_luminance[60] = 112;
		this.quantum_luminance[61] = 100;
		this.quantum_luminance[62] = 103;
		this.quantum_luminance[63] = 99;

		for (j = 0; j < 64; j++) {
			temp = (this.quantum_luminance[j] * Quality + 50) / 100;
			if (temp <= 0) {
				temp = 1;
			}
			if (temp > 255) {
				temp = 255;
			}
			this.quantum_luminance[j] = temp;
		}
		index = 0;
		for (i = 0; i < 8; i++) {
			for (j = 0; j < 8; j++) {
				// The divisors for the LL&M method (the slow integer method
				// used in
				// jpeg 6a library). This method is currently (04/04/98)
				// incompletely
				// implemented.
				// DivisorsLuminance[index] = ((double)
				// quantum_luminance[index]) << 3;
				// The divisors for the AAN method (the float method used in
				// jpeg 6a library.
				this.DivisorsLuminance[index] = 1.0
						/ (this.quantum_luminance[index] * AANscaleFactor[i] * AANscaleFactor[j] * 8.0);
				index++;
			}
		}

		// Creating the chrominance matrix

		this.quantum_chrominance[0] = 17;
		this.quantum_chrominance[1] = 18;
		this.quantum_chrominance[2] = 24;
		this.quantum_chrominance[3] = 47;
		this.quantum_chrominance[4] = 99;
		this.quantum_chrominance[5] = 99;
		this.quantum_chrominance[6] = 99;
		this.quantum_chrominance[7] = 99;
		this.quantum_chrominance[8] = 18;
		this.quantum_chrominance[9] = 21;
		this.quantum_chrominance[10] = 26;
		this.quantum_chrominance[11] = 66;
		this.quantum_chrominance[12] = 99;
		this.quantum_chrominance[13] = 99;
		this.quantum_chrominance[14] = 99;
		this.quantum_chrominance[15] = 99;
		this.quantum_chrominance[16] = 24;
		this.quantum_chrominance[17] = 26;
		this.quantum_chrominance[18] = 56;
		this.quantum_chrominance[19] = 99;
		this.quantum_chrominance[20] = 99;
		this.quantum_chrominance[21] = 99;
		this.quantum_chrominance[22] = 99;
		this.quantum_chrominance[23] = 99;
		this.quantum_chrominance[24] = 47;
		this.quantum_chrominance[25] = 66;
		this.quantum_chrominance[26] = 99;
		this.quantum_chrominance[27] = 99;
		this.quantum_chrominance[28] = 99;
		this.quantum_chrominance[29] = 99;
		this.quantum_chrominance[30] = 99;
		this.quantum_chrominance[31] = 99;
		this.quantum_chrominance[32] = 99;
		this.quantum_chrominance[33] = 99;
		this.quantum_chrominance[34] = 99;
		this.quantum_chrominance[35] = 99;
		this.quantum_chrominance[36] = 99;
		this.quantum_chrominance[37] = 99;
		this.quantum_chrominance[38] = 99;
		this.quantum_chrominance[39] = 99;
		this.quantum_chrominance[40] = 99;
		this.quantum_chrominance[41] = 99;
		this.quantum_chrominance[42] = 99;
		this.quantum_chrominance[43] = 99;
		this.quantum_chrominance[44] = 99;
		this.quantum_chrominance[45] = 99;
		this.quantum_chrominance[46] = 99;
		this.quantum_chrominance[47] = 99;
		this.quantum_chrominance[48] = 99;
		this.quantum_chrominance[49] = 99;
		this.quantum_chrominance[50] = 99;
		this.quantum_chrominance[51] = 99;
		this.quantum_chrominance[52] = 99;
		this.quantum_chrominance[53] = 99;
		this.quantum_chrominance[54] = 99;
		this.quantum_chrominance[55] = 99;
		this.quantum_chrominance[56] = 99;
		this.quantum_chrominance[57] = 99;
		this.quantum_chrominance[58] = 99;
		this.quantum_chrominance[59] = 99;
		this.quantum_chrominance[60] = 99;
		this.quantum_chrominance[61] = 99;
		this.quantum_chrominance[62] = 99;
		this.quantum_chrominance[63] = 99;

		for (j = 0; j < 64; j++) {
			temp = (this.quantum_chrominance[j] * Quality + 50) / 100;
			if (temp <= 0) {
				temp = 1;
			}
			if (temp >= 255) {
				temp = 255;
			}
			this.quantum_chrominance[j] = temp;
		}
		index = 0;
		for (i = 0; i < 8; i++) {
			for (j = 0; j < 8; j++) {
				// The divisors for the LL&M method (the slow integer method
				// used in
				// jpeg 6a library). This method is currently (04/04/98)
				// incompletely
				// implemented.
				// DivisorsChrominance[index] = ((double)
				// quantum_chrominance[index]) << 3;
				// The divisors for the AAN method (the float method used in
				// jpeg 6a library.
				this.DivisorsChrominance[index] = 1.0
						/ (this.quantum_chrominance[index] * AANscaleFactor[i] * AANscaleFactor[j] * 8.0);
				index++;
			}
		}

		// quantum and Divisors are objects used to hold the appropriate matices

		this.quantum[0] = this.quantum_luminance;
		this.Divisors[0] = this.DivisorsLuminance;
		this.quantum[1] = this.quantum_chrominance;
		this.Divisors[1] = this.DivisorsChrominance;

	}

	/*
	 * This method quantitizes data and rounds it to the nearest integer.
	 */
	public int[] quantizeBlock(final double inputData[][], final int code) {
		final int outputData[] = new int[this.N * this.N];
		int i, j;
		int index;
		index = 0;
		for (i = 0; i < 8; i++) {
			for (j = 0; j < 8; j++) {
				// The second line results in significantly better compression.
				outputData[index] = (int) Math.round(inputData[i][j] * ((double[]) this.Divisors[code])[index]);
				// outputData[index] = (int)(((inputData[i][j] * (((double[])
				// (Divisors[code]))[index])) + 16384.5) -16384);
				index++;
			}
		}

		return outputData;
	}

	/*
	 * This is the method for quantizing a block DCT'ed with forwardDCTExtreme This method
	 * quantitizes data and rounds it to the nearest integer.
	 */
	public int[] quantizeBlockExtreme(final double inputData[][], final int code) {
		final int outputData[] = new int[this.N * this.N];
		int i, j;
		int index;
		index = 0;
		for (i = 0; i < 8; i++) {
			for (j = 0; j < 8; j++) {
				outputData[index] = (int) Math.round(inputData[i][j] / ((int[]) this.quantum[code])[index]);
				index++;
			}
		}

		return outputData;
	}
}
