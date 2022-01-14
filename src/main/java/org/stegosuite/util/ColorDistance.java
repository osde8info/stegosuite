package org.stegosuite.util;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 */
public enum ColorDistance {

	/**
	 * Basic Euclidean distance
	 */
	RGB_EUCLID {

		@Override
		protected Double getDistance(Color rgb1, Color rgb2) {
			return Math.sqrt(Math.pow(rgb1.getRed() - rgb2.getRed(), 2) + Math.pow(rgb1.getGreen() - rgb2.getGreen(), 2)
					+ Math.pow(rgb1.getBlue() - rgb2.getBlue(), 2));
		}
	},

	/**
	 * CIEDE2000 distance using the Lab color space
	 */
	CIEDE_2000 {

		@Override
		protected Double getDistance(Color rgb1, Color rgb2) {
			double[] lab1 = rgb2lab(rgb1.getRed(), rgb1.getGreen(), rgb1.getBlue());
			double[] lab2 = rgb2lab(rgb2.getRed(), rgb2.getGreen(), rgb2.getBlue());
			return ciede2000(lab1, lab2);
		}
	};

	protected abstract Double getDistance(Color rgb1, Color rgb2);

	private static Map<ColorDistance, Map<InterchangeablePair<Color, Color>, Double>> cache = null;

	static {
		cache = new HashMap<>(ColorDistance.values().length);
		for (ColorDistance distance : ColorDistance.values()) {
			cache.put(distance, new ConcurrentHashMap<>());
		}
	}

	/**
	 * Compares two RGB colors according to the current distance algorithm.
	 * 
	 * @param rgb1 First color for the comparaison.
	 * @param rgb2 Second color for the comparaison.
	 * @return The distance between the two colors according to the current distance algorithm.
	 */
	public Double distance(Color rgb1, Color rgb2) {
		if (rgb1 == null || rgb2 == null) {
			return null;
		}

		if (rgb1.equals(rgb2)) {
			return 0.0;
		}

		InterchangeablePair<Color, Color> pair = new InterchangeablePair<>(rgb1, rgb2);
		Double distance = cache.get(this).computeIfAbsent(pair, k -> getDistance(rgb1, rgb2));

		return distance;
	}

	/**
	 * Converts a color from the RGB color space the L*a*b color space
	 *
	 * Taken from https://github.com/StanfordHCI/c3/blob/master/java/src/edu/stanford
	 * /vis/color/LAB.java
	 *
	 * Maps an RGB triple to binned LAB space (D65). Binning is done by <i>flooring</i> LAB values.
	 *
	 * @param ri Red component of the RGB color.
	 * @param gi Green component of the RGB color.
	 * @param bi Blue component of the RGB color.
	 * @return The color in the L*a*b color space
	 */
	private static double[] rgb2lab(int ri, int gi, int bi) {
		// first, normalize RGB values
		double r = ri / 255.0;
		double g = gi / 255.0;
		double b = bi / 255.0;

		// D65 standard referent
		double X = 0.950470, Y = 1.0, Z = 1.088830;

		// second, map sRGB to CIE XYZ
		r = r <= 0.04045 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
		g = g <= 0.04045 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
		b = b <= 0.04045 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
		double x = ((0.4124564 * r) + (0.3575761 * g) + (0.1804375 * b)) / X,
				y = ((0.2126729 * r) + (0.7151522 * g) + (0.0721750 * b)) / Y,
				z = ((0.0193339 * r) + (0.1191920 * g) + (0.9503041 * b)) / Z;

		// third, map CIE XYZ to CIE L*a*b* and return
		x = x > 0.008856 ? Math.pow(x, 1.0 / 3) : (7.787037 * x) + (4.0 / 29);
		y = y > 0.008856 ? Math.pow(y, 1.0 / 3) : (7.787037 * y) + (4.0 / 29);
		z = z > 0.008856 ? Math.pow(z, 1.0 / 3) : (7.787037 * z) + (4.0 / 29);

		double L = (116 * y) - 16, A = 500 * (x - y), B = 200 * (y - z);

		return new double[] { L, A, B };
	}

	/**
	 * Compares to L*a*b colors and returns the degree of their similarity. The lower the result the
	 * more similar are the colors.
	 *
	 * Taken from https://github.com/StanfordHCI/c3/blob/master/java/src/edu/stanford
	 * /vis/color/LAB.java
	 *
	 * @param lab1 First color represented in L*a*b color space.
	 * @param lab2 Second color represented in L*a*b color space.
	 * @return The degree of similarity between the two input colors according to the CIEDE2000
	 *         color-difference formula.
	 */
	private static Double ciede2000(double[] lab1, double[] lab2) {
		// adapted from Sharma et al's MATLAB implementation at
		// http://www.ece.rochester.edu/~gsharma/ciede2000/

		// parametric factors, use defaults
		double kl = 1, kc = 1, kh = 1;

		// compute terms
		double pi = Math.PI, L1 = lab1[0], a1 = lab1[1], b1 = lab1[2], Cab1 = Math.sqrt((a1 * a1) + (b1 * b1)),
				L2 = lab2[0], a2 = lab2[1], b2 = lab2[2], Cab2 = Math.sqrt((a2 * a2) + (b2 * b2)),
				Cab = 0.5 * (Cab1 + Cab2),
				G = 0.5 * (1 - Math.sqrt(Math.pow(Cab, 7) / (Math.pow(Cab, 7) + Math.pow(25, 7)))), ap1 = (1 + G) * a1,
				ap2 = (1 + G) * a2, Cp1 = Math.sqrt((ap1 * ap1) + (b1 * b1)), Cp2 = Math.sqrt((ap2 * ap2) + (b2 * b2)),
				Cpp = Cp1 * Cp2;

		// ensure hue is between 0 and 2pi
		double hp1 = Math.atan2(b1, ap1);
		if (hp1 < 0) {
			hp1 += 2 * pi;
		}
		double hp2 = Math.atan2(b2, ap2);
		if (hp2 < 0) {
			hp2 += 2 * pi;
		}

		double dL = L2 - L1, dC = Cp2 - Cp1, dhp = hp2 - hp1;

		if (dhp > +pi) {
			dhp -= 2 * pi;
		}
		if (dhp < -pi) {
			dhp += 2 * pi;
		}
		if (Cpp == 0) {
			dhp = 0;
		}

		// Note that the defining equations actually need signed Hue and chroma
		// differences which is different from prior color difference formulae
		double dH = 2 * Math.sqrt(Cpp) * Math.sin(dhp / 2);

		// Weighting functions
		double Lp = 0.5 * (L1 + L2), Cp = 0.5 * (Cp1 + Cp2);

		// Average Hue Computation. This is equivalent to that in the paper but
		// simpler programmatically. Average hue is computed in radians and
		// converted to degrees where needed
		double hp = 0.5 * (hp1 + hp2);
		// Identify positions for which abs hue diff exceeds 180 degrees
		if (Math.abs(hp1 - hp2) > pi) {
			hp -= pi;
		}
		if (hp < 0) {
			hp += 2 * pi;
		}

		// Check if one of the chroma values is zero, in which case set mean hue
		// to the sum which is equivalent to other value
		if (Cpp == 0) {
			hp = hp1 + hp2;
		}

		double Lpm502 = (Lp - 50) * (Lp - 50), Sl = 1 + ((0.015 * Lpm502) / Math.sqrt(20 + Lpm502)),
				Sc = 1 + (0.045 * Cp),
				T = ((1 - (0.17 * Math.cos(hp - (pi / 6)))) + (0.24 * Math.cos(2 * hp))
						+ (0.32 * Math.cos((3 * hp) + (pi / 30)))) - (0.20 * Math.cos((4 * hp) - ((63 * pi) / 180))),
				Sh = 1 + (0.015 * Cp * T), ex = (((180 / pi) * hp) - 275) / 25,
				delthetarad = ((30 * pi) / 180) * Math.exp(-1 * (ex * ex)),
				Rc = 2 * Math.sqrt(Math.pow(Cp, 7) / (Math.pow(Cp, 7) + Math.pow(25, 7))),
				RT = -1 * Math.sin(2 * delthetarad) * Rc;

		dL = dL / (kl * Sl);
		dC = dC / (kc * Sc);
		dH = dH / (kh * Sh);

		// The CIED200 color difference
		return Math.sqrt((dL * dL) + (dC * dC) + (dH * dH) + (RT * dC * dH));
	}
}
