/* Version 0.1 of F5 Steganography Software by Andreas Westfeld 1999 */
/***********************************************************/
/* JPEG Decoder */
/* Sean Breslin */
/* EE590 Directed Research */
/* Dr. Ortega */
/* Fall 1997 */
/*                                                         */
/* HuffmanDecode.class: */
/* This object performs entopy decoding on */
/* a JPEG image file. This object instanciates */
/* HuffTable.class which extracts the Huffman */
/* Tables from the file header information. */
/*                                                         */
/* Methods: */
/* HuffDecode(), returns array of 8x8 */
/* blocks of image data */
/* getX(), returns horizontal image size */
/* getY(), returns vertical image size */
/* getPrec(), returns sample precision */
/* getComp(), returns number of components */
/* rawDecode(), returns quantized */
/* coefficients */
/*                                                         */
/********************** 11/4/97 ****************************/
//
// changes by Andreas Westfeld
// <mailto:westfeld@inf.tu-dresden.de>
package org.stegosuite.image.jpgtemp.net.f5.ortega;

//
// Mar 15 1999
// constructor changed to byte array parameter
// added method rawDecode
//

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

@SuppressWarnings("unused")
public class HuffmanDecode {

	private final static int APP0 = 0xE0;

	private final static int APP1 = 0xE1;

	private final static int APP2 = 0xE2;

	private final static int APP3 = 0xE3;

	private final static int APP4 = 0xE4;

	private final static int APP5 = 0xE5;

	private final static int APP6 = 0xE6;

	private final static int APP7 = 0xE7;

	private final static int APP8 = 0xE8;

	private final static int APP9 = 0xE9;

	private final static int APP10 = 0xEA;

	private final static int APP11 = 0xEB;

	private final static int APP12 = 0xEC;

	private final static int APP13 = 0xED;

	private final static int APP14 = 0xEE;

	private final static int APP15 = 0xEF;

	private final static int DRI = 0xDD;

	private final static int DNL = 0xDC;

	private final static int EOI = 0xD9;

	// Instance variables
	// Declare header variables
	private int Lf, P, X, Y, Nf; // SOF0 parameters

	private int[] C, H, V, T; // SOF0 parameters

	private int Ls, Ns, Ss, Se, Ah, Al; // SOS parameters

	private int[] Cs, Td, Ta; // SOS parameters

	private int Lh, Tc, Th; // DHT parameters

	private int Lq, Pq, Tq; // DQT parameters

	private int Ld, Nl; // DNL parameters

	private int Lr, Ri; // DRI parameters

	// other variables
	private int B, CNT, DIFF, PRED;

	private final int size;

	private int K, SSSS, RS, R, J, CODE;

	private int lp, cnt, a, b, hftbl;

	private int[][][] Cr, Cb;

	private final int[][] HUFFVAL = new int[4][];

	private final int[][] VALPTR = new int[4][];

	private final int[][] MINCODE = new int[4][];

	private final int[][] MAXCODE = new int[4][];

	private final int[] ZZ = new int[64];

	private final int[][] QNT = new int[4][64];

	private int RI;

	private static byte[][] deZZ = { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 2, 0 }, { 1, 1 }, { 0, 2 }, { 0, 3 }, { 1, 2 },
			{ 2, 1 }, { 3, 0 }, { 4, 0 }, { 3, 1 }, { 2, 2 }, { 1, 3 }, { 0, 4 }, { 0, 5 }, { 1, 4 }, { 2, 3 },
			{ 3, 2 }, { 4, 1 }, { 5, 0 }, { 6, 0 }, { 5, 1 }, { 4, 2 }, { 3, 3 }, { 2, 4 }, { 1, 5 }, { 0, 6 },
			{ 0, 7 }, { 1, 6 }, { 2, 5 }, { 3, 4 }, { 4, 3 }, { 5, 2 }, { 6, 1 }, { 7, 0 }, { 7, 1 }, { 6, 2 },
			{ 5, 3 }, { 4, 4 }, { 3, 5 }, { 2, 6 }, { 1, 7 }, { 2, 7 }, { 3, 6 }, { 4, 5 }, { 5, 4 }, { 6, 3 },
			{ 7, 2 }, { 7, 3 }, { 6, 4 }, { 5, 5 }, { 4, 6 }, { 3, 7 }, { 4, 7 }, { 5, 6 }, { 6, 5 }, { 7, 4 },
			{ 7, 5 }, { 6, 6 }, { 5, 7 }, { 6, 7 }, { 7, 6 }, { 7, 7 } };

	// added for decode()
	private static byte[] deZigZag = { 0, 1, 5, 6, 14, 15, 27, 28, 2, 4, 7, 13, 16, 26, 29, 42, 3, 8, 12, 17, 25, 30,
			41, 43, 9, 11, 18, 24, 31, 40, 44, 53, 10, 19, 23, 32, 39, 45, 52, 54, 20, 22, 33, 38, 46, 51, 55, 60, 21,
			34, 37, 47, 50, 56, 59, 61, 35, 36, 48, 49, 57, 58, 62, 63 };

	// {{ Control Objects
	HuffTable htDC0, htDC1;

	HuffTable htAC0, htAC1;

	DataInputStream dis;

	TextArea ta;

	Date dt;

	// }}

	// Constructor Method
	public HuffmanDecode(final byte[] data) {
		this.size = (short) data.length;
		this.dis = new DataInputStream(new ByteArrayInputStream(data));
		// Parse out markers and header info
		boolean cont = true;
		while (cont) {
			if (255 == getByte()) {
				switch (getByte()) {
					case 192:
						sof0();
						break;
					case 196:
						dht();
						break;
					case 219:
						dqt();
						break;
					case 217:
						cont = false;
						break;
					case 218:
						cont = false;
						break;
					case APP0:
					case APP1:
					case APP2:
					case APP3:
					case APP4:
					case APP5:
					case APP6:
					case APP7:
					case APP8:
					case APP9:
					case APP10:
					case APP11:
					case APP12:
					case APP13:
					case APP14:
					case APP15:
						skipVariable();
						break;
					case DRI:
						dri();
						break;
				}
			}
		}
	}

	private int available() {
		try {
			return this.dis.available();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void closeStream() {
		// Close input stream
		try {
			this.dis.close(); // close io stream to file
		} catch (final IOException e) {}
	}

	// Return image data
	public int[] decode() {
		final int x, y, a, b, line;// , sz = X * Y;
		int /* col, */ tmp;
		final int blocks, MCU;// , scan=0;
		int[] Cs, Ta, Td;
		final int[] PRED = new int[this.Nf];
		for (int nComponent = 0; nComponent < this.Nf; nComponent++) {
			PRED[nComponent] = 0;
		}
		final long t;
		final double time;
		this.CNT = 0;
		// Read in Scan Header information
		this.Ls = getInt();
		this.Ns = getByte();
		// System.out.println("SOS - Components: "+Integer.toString(Ns));
		Cs = new int[this.Ns];
		Td = new int[this.Ns];
		Ta = new int[this.Ns];

		// get table information
		for (this.lp = 0; this.lp < this.Ns; this.lp++) {
			Cs[this.lp] = getByte();
			Td[this.lp] = getByte();
			Ta[this.lp] = Td[this.lp] & 0x0f;
			Td[this.lp] >>= 4;
			// System.out.println("DC-Table: "+Integer.toString(Td[lp])+"AC-Table:
			// "+Integer.toString(Ta[lp]));
		}

		this.Ss = getByte();
		this.Se = getByte();
		this.Ah = getByte();
		this.Al = this.Ah & 0x0f;
		this.Ah >>= 4;

		// Calculate the Number of blocks encoded
		// warum doppelt so viel?
		final int buff[] = new int[2 * 8 * 8 * getBlockCount()];
		int pos = 0;
		int MCUCount = 0;

		// System.out.println("BlockCount="+getBlockCount());
		final boolean bDoIt = true;
		while (bDoIt) {
			// Get component 1 of MCU
			for (int nComponent = 0; nComponent < this.Nf; nComponent++) {
				for (this.cnt = 0; this.cnt < this.H[nComponent] * this.V[nComponent]; this.cnt++) {
					// Get DC coefficient
					this.hftbl = Td[nComponent] * 2;
					tmp = DECODE();
					this.DIFF = RECEIVE(tmp);
					this.ZZ[0] = PRED[0] + EXTEND(this.DIFF, tmp);
					PRED[nComponent] = this.ZZ[0];

					// Get AC coefficients
					this.hftbl = Ta[nComponent] * 2 + 1;
					Decode_AC_coefficients();

					for (this.lp = 0; this.lp < 64; this.lp++) {
						// System.out.println("pos="+pos);
						// Zickzack???
						// buff[pos++]=ZZ[deZigZag[lp]];
						buff[pos++] = this.ZZ[this.lp];
					}
				}
			}

			MCUCount++;
			if (MCUCount == this.RI) {
				MCUCount = 0;
				this.CNT = 0;
				for (int nComponent = 0; nComponent < this.Nf; nComponent++) {
					PRED[nComponent] = 0;
				}
				// System.out.println("MCUCount");
				getByte();
				// System.out.println(Integer.toHexString(getByte()));
				final int tmpB = getByte();
				// System.out.println(Integer.toHexString(tmpB));
				if (tmpB == EOI) {
					break;
					// System.out.println("MCUCount-Ende");
				}
			}
			if (available() <= 2) {
				// System.out.println("expecting end of image");
				if (available() == 2) {
					getByte();
					if (getByte() != EOI) {
						System.out.println("file does not end with EOI");
					}
				} else {
					if (available() > 0) {
						System.out.println(Integer.toHexString(getByte()));
					}
					System.out.println("file does not end with EOI");
				}
				break;
			}
		}
		final int[] tmpBuff = new int[pos];
		System.arraycopy(buff, 0, tmpBuff, 0, pos);
		return tmpBuff;
	}

	private int DECODE() {
		int I, CD, VALUE;

		CD = NextBit();
		I = 1;

		while (true) {
			// System.out.println(hftbl+" "+I);
			if (CD > this.MAXCODE[this.hftbl][I]) {
				CD = (CD << 1) + NextBit();
				I++;
			} else {
				break;
			}
		}
		this.J = this.VALPTR[this.hftbl][I];
		this.J = this.J + CD - this.MINCODE[this.hftbl][I];
		VALUE = this.HUFFVAL[this.hftbl][this.J];
		return VALUE;
	}

	private void Decode_AC_coefficients() {
		this.K = 1;

		// Zero out array ZZ[]
		for (this.lp = 1; this.lp < 64; this.lp++) {
			this.ZZ[this.lp] = 0;
		}

		while (true) {
			// System.out.println(hftbl);
			this.RS = DECODE();
			this.SSSS = this.RS % 16;
			this.R = this.RS >> 4;
			if (this.SSSS == 0) {
				if (this.R == 15) {
					this.K += 16;
					continue;
				} else {
					return;
				}
			} else {
				this.K = this.K + this.R;
				Decode_ZZ(this.K);
				if (this.K == 63) {
					return;
				} else {
					this.K++;
				}
			}
		}
	}

	private void Decode_ZZ(final int k) {
		// Decoding a nonzero AC coefficient
		this.ZZ[k] = RECEIVE(this.SSSS);
		this.ZZ[k] = EXTEND(this.ZZ[k], this.SSSS);
	}

	private void dht() {
		// Read in Huffman tables
		// System.out.println("Read in Huffman tables");
		// Lh length
		// Th index
		// Tc AC?
		this.Lh = getInt();
		while (this.Lh > 0) {
			this.Tc = getByte();
			this.Th = this.Tc & 0x0f;
			this.Tc >>= 4;
			// System.out.println("______Lh="+Lh);
			if (this.Th == 0) {
				if (this.Tc == 0) {
					this.htDC0 = new HuffTable(this.dis, this.Lh);
					this.Lh -= this.htDC0.getLen();
					this.HUFFVAL[0] = this.htDC0.getHUFFVAL();
					this.VALPTR[0] = this.htDC0.getVALPTR();
					this.MAXCODE[0] = this.htDC0.getMAXCODE();
					// System.out.println("MAXCODE[0]="+MAXCODE[0]);
					this.MINCODE[0] = this.htDC0.getMINCODE();
					this.htDC0 = null;
					System.gc();
				} else {
					this.htAC0 = new HuffTable(this.dis, this.Lh);
					this.Lh -= this.htAC0.getLen();
					this.HUFFVAL[1] = this.htAC0.getHUFFVAL();
					this.VALPTR[1] = this.htAC0.getVALPTR();
					this.MAXCODE[1] = this.htAC0.getMAXCODE();
					// System.out.println("MAXCODE[1]="+MAXCODE[1]);
					this.MINCODE[1] = this.htAC0.getMINCODE();
					this.htAC0 = null;
					System.gc();
				}
			} else {
				if (this.Tc == 0) {
					this.htDC1 = new HuffTable(this.dis, this.Lh);
					this.Lh -= this.htDC1.getLen();
					this.HUFFVAL[2] = this.htDC1.getHUFFVAL();
					this.VALPTR[2] = this.htDC1.getVALPTR();
					this.MAXCODE[2] = this.htDC1.getMAXCODE();
					// System.out.println("MAXCODE[2]="+MAXCODE[2]);
					this.MINCODE[2] = this.htDC1.getMINCODE();
					this.htDC1 = null;
					System.gc();
				} else {
					this.htAC1 = new HuffTable(this.dis, this.Lh);
					this.Lh -= this.htAC1.getLen();
					this.HUFFVAL[3] = this.htAC1.getHUFFVAL();
					this.VALPTR[3] = this.htAC1.getVALPTR();
					this.MAXCODE[3] = this.htAC1.getMAXCODE();
					// System.out.println("MAXCODE[3]="+MAXCODE[3]);
					this.MINCODE[3] = this.htAC1.getMINCODE();
					this.htAC1 = null;
					System.gc();
				}
			}
		}
	}

	private void dqt() {

		// Read in quatization tables
		this.Lq = getInt();
		this.Pq = getByte();
		this.Tq = this.Pq & 0x0f;
		this.Pq >>= 4;

		switch (this.Tq) {
			case 0:
				for (this.lp = 0; this.lp < 64; this.lp++) {
					this.QNT[0][this.lp] = getByte();
				}
				break;

			case 1:
				for (this.lp = 0; this.lp < 64; this.lp++) {
					this.QNT[1][this.lp] = getByte();
				}
				break;

			case 2:
				for (this.lp = 0; this.lp < 64; this.lp++) {
					this.QNT[2][this.lp] = getByte();
				}
				break;

			case 3:
				for (this.lp = 0; this.lp < 64; this.lp++) {
					this.QNT[3][this.lp] = getByte();
				}
				break;
		}
	}

	private void dri() {
		getInt();
		this.RI = getInt();
	}

	private int EXTEND(int V, final int T) {
		int Vt;

		Vt = 0x01 << T - 1;
		if (V < Vt) {
			Vt = (-1 << T) + 1;
			V += Vt;
		}
		return V;
	}

	// Calculate the Number of blocks encoded
	public int getBlockCount() {
		switch (this.Nf) {
			case 1:
				return (this.X + 7) / 8 * ((this.Y + 7) / 8);
			case 3:
				return 6 * ((this.X + 15) / 16) * ((this.Y + 15) / 16);
			default:
				System.out.println("Nf weder 1 noch 3");
		}
		return 0;
	}

	public int getByte() {
		int b = 0;
		// Read Byte from DataInputStream
		try {
			b = this.dis.readUnsignedByte();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return b;
	}

	public int getComp() {
		return this.Nf;
	}

	public int getInt() {
		int b = 0;
		// Read Integer from DataInputStream
		try {
			b = this.dis.readUnsignedByte();
			b <<= 8;
			final int tmp = this.dis.readUnsignedByte();
			b ^= tmp;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return b;
	}

	public int getPrec() {
		return this.P;
	}

	// Public get methods
	public int getX() {
		return this.X;
	}

	public int getY() {
		return this.Y;
	}

	// Return image data
	public void HuffDecode(final int[][][] buffer) {
		int x, y, tmp;
		final int sz = this.X * this.Y, scan = 0;
		final int[][] Block = new int[8][8];
		int Cs, Ta, Td, blocks;
		final long t;
		final double time;

		// Read in Scan Header information
		this.Ls = getInt();
		this.Ns = getByte();
		Cs = getByte();
		Td = getByte();
		Ta = Td & 0x0f;
		Td >>= 4;

		this.Ss = getByte();
		this.Se = getByte();
		this.Ah = getByte();
		this.Al = this.Ah & 0x0f;
		this.Ah >>= 4;

		// Calculate the Number of blocks encoded
		// blocks = X * Y / 64;
		blocks = getBlockCount() / 6;

		// decode image data and return image data in array
		for (this.cnt = 0; this.cnt < blocks; this.cnt++) {
			// Get DC coefficient
			if (Td == 0) {
				this.hftbl = 0;
			} else {
				this.hftbl = 2;
			}
			tmp = DECODE();
			this.DIFF = RECEIVE(tmp);
			this.ZZ[0] = this.PRED + EXTEND(this.DIFF, tmp);
			this.PRED = this.ZZ[0];

			// Get AC coefficients
			if (Ta == 0) {
				this.hftbl = 1;
			} else {
				this.hftbl = 3;
			}
			Decode_AC_coefficients();

			// dezigzag and dequantize block
			for (this.lp = 0; this.lp < 64; this.lp++) {
				Block[deZZ[this.lp][0]][deZZ[this.lp][1]] = this.ZZ[this.lp] * this.QNT[0][this.lp];
			}

			// store blocks in buffer
			for (x = 0; x < 8; x++) {
				for (y = 0; y < 8; y++) {
					buffer[this.cnt][x][y] = Block[x][y];
				}
			}
		}
		closeStream();
	}

	private int NextBit() {
		// Get one bit from entropy coded data stream
		int b2;
		final int lns;
		int BIT;

		if (this.CNT == 0) {
			this.CNT = 8;
			this.B = getByte();
			if (255 == this.B) {
				b2 = getByte();
			}
		}
		BIT = this.B & 0X80; // get MSBit of B
		BIT >>= 7; // move MSB to LSB
		this.CNT--; // Decrement counter
		this.B <<= 1; // Shift left one bit
		return BIT;
	}

	// Return quantized coefficients
	public void rawDecode(final int[][][] buffer) {
		int x, y, tmp;
		final int[][] Block = new int[8][8];
		int Cs, Ta, Td, blocks;
		final long t;
		final double time;

		// Read in Scan Header information
		this.Ls = getInt();
		this.Ns = getByte();
		Cs = getByte();
		Td = getByte();
		Ta = Td & 0x0f;
		Td >>= 4;

		this.Ss = getByte();
		this.Se = getByte();
		this.Ah = getByte();
		this.Al = this.Ah & 0x0f;
		this.Ah >>= 4;

		// Calculate the Number of blocks encoded
		blocks = getBlockCount() / 6;

		// decode image data and return image data in array
		for (this.cnt = 0; this.cnt < blocks; this.cnt++) {
			// Get DC coefficient
			if (Td == 0) {
				this.hftbl = 0;
			} else {
				this.hftbl = 2;
			}
			tmp = DECODE();
			this.DIFF = RECEIVE(tmp);
			this.ZZ[0] = this.PRED + EXTEND(this.DIFF, tmp);
			this.PRED = this.ZZ[0];

			// Get AC coefficients
			if (Ta == 0) {
				this.hftbl = 1;
			} else {
				this.hftbl = 3;
			}
			Decode_AC_coefficients();

			// dezigzag
			for (this.lp = 0; this.lp < 64; this.lp++) {
				Block[deZZ[this.lp][0]][deZZ[this.lp][1]] = this.ZZ[this.lp];
			}

			// store blocks in buffer
			System.out.print(this.cnt + " ");
			for (x = 0; x < 8; x++) {
				for (y = 0; y < 8; y++) {
					buffer[this.cnt][x][y] = Block[x][y];
				}
			}
		}
		closeStream();
	}

	private int RECEIVE(final int SSS) {
		int V = 0, I = 0;

		while (true) {
			if (I == SSS) {
				return V;
			}
			I++;
			V = (V << 1) + NextBit();
		}
	}

	// Return image data for RGB images
	public void RGBdecode(final int[][][] Lum) {
		int x, y, a, b, line, col, tmp;
		final int sz = this.X * this.Y;
		int blocks;
		final int MCU, scan = 0;
		final int[][] Block = new int[8][8];
		int[] Cs, Ta, Td;
		final int[] PRED = { 0, 0, 0 };
		final long t;
		final double time;

		// Read in Scan Header information
		this.Ls = getInt();
		this.Ns = getByte();
		Cs = new int[this.Ns];
		Td = new int[this.Ns];
		Ta = new int[this.Ns];

		// get table information
		for (this.lp = 0; this.lp < this.Ns; this.lp++) {
			Cs[this.lp] = getByte();
			Td[this.lp] = getByte();
			Ta[this.lp] = Td[this.lp] & 0x0f;
			Td[this.lp] >>= 4;
		}

		this.Ss = getByte();
		this.Se = getByte();
		this.Ah = getByte();
		this.Al = this.Ah & 0x0f;
		this.Ah >>= 4;

		// Calculate the Number of blocks encoded
		// blocks = X * Y / 64;
		blocks = getBlockCount() / 6;
		col = 2;

		// decode image data and return image data in array
		for (a = 0; a < 32; a++) {
			for (b = 0; b < 32; b++) {
				// Get component 1 of MCU
				for (this.cnt = 0; this.cnt < 4; this.cnt++) {
					// Get DC coefficient
					this.hftbl = 0;
					tmp = DECODE();
					this.DIFF = RECEIVE(tmp);
					this.ZZ[0] = PRED[0] + EXTEND(this.DIFF, tmp);
					PRED[0] = this.ZZ[0];

					// Get AC coefficients
					this.hftbl = 1;
					Decode_AC_coefficients();

					// dezigzag and dequantize block
					for (this.lp = 0; this.lp < 64; this.lp++) {
						Block[deZZ[this.lp][0]][deZZ[this.lp][1]] = this.ZZ[this.lp] * this.QNT[0][this.lp];
					}

					if (this.cnt < 2) {
						line = 0;
					} else {
						line = 62;
					}

					// store blocks in buffer
					for (x = 0; x < 8; x++) {
						for (y = 0; y < 8; y++) {
							Lum[b * 2 + this.cnt + line + a * 128][x][y] = Block[x][y];
						}
					}
				}

				// getComponent 2 and 3 of image
				for (this.cnt = 0; this.cnt < 2; this.cnt++) {
					// Get DC coefficient
					this.hftbl = 2;
					tmp = DECODE();
					this.DIFF = RECEIVE(tmp);
					this.ZZ[0] = PRED[this.cnt + 1] + EXTEND(this.DIFF, tmp);
					PRED[this.cnt + 1] = this.ZZ[0];

					// Get AC coefficients
					this.hftbl = 3;
					Decode_AC_coefficients();

					// dezigzag and dequantize block
					for (this.lp = 0; this.lp < 64; this.lp++) {
						Block[deZZ[this.lp][0]][deZZ[this.lp][1]] = this.ZZ[this.lp] * this.QNT[1][this.lp];
					}

					// store blocks in buffer
					if (this.cnt == 0) {
						for (x = 0; x < 8; x++) {
							for (y = 0; y < 8; y++) {
								this.Cb[a * 32 + b][x][y] = Block[x][y];
							}
						}
					} else {
						for (x = 0; x < 8; x++) {
							for (y = 0; y < 8; y++) {
								this.Cr[a * 32 + b][x][y] = Block[x][y];
							}
						}
					}
				}
			}
		}
		closeStream();
	}

	public void setCb(final int[][][] chrome) {
		this.Cb = chrome;
	}

	public void setCr(final int[][][] chrome) {
		this.Cr = chrome;
	}

	private void skipVariable() {
		try {
			this.dis.skipBytes(getInt() - 2);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void sof0() {
		// Read in start of frame header data
		this.Lf = getInt();
		this.P = getByte();
		this.Y = getInt();
		this.X = getInt();
		this.Nf = getByte();

		this.C = new int[this.Nf];
		this.H = new int[this.Nf];
		this.V = new int[this.Nf];
		this.T = new int[this.Nf];

		// Read in quatization table identifiers
		for (this.lp = 0; this.lp < this.Nf; this.lp++) {
			this.C[this.lp] = getByte();
			this.H[this.lp] = getByte();
			this.V[this.lp] = this.H[this.lp] & 0x0f;
			this.H[this.lp] >>= 4;
			this.T[this.lp] = getByte();
		}
	}
}
