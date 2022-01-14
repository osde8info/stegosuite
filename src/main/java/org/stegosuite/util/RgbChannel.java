package org.stegosuite.util;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public enum RgbChannel {

	RED {

		@Override
		public int getValue(Color color) {
			return color.getRed();
		}

		@Override
		public Color setValue(Color color, int value) {
			return new Color(value, color.getGreen(), color.getBlue(), color.getAlpha());
		}

	},

	GREEN {

		@Override
		public int getValue(Color color) {
			return color.getGreen();
		}

		@Override
		public Color setValue(Color color, int value) {
			return new Color(color.getRed(), value, color.getBlue(), color.getAlpha());
		}

	},

	BLUE {

		@Override
		public int getValue(Color color) {
			return color.getBlue();
		}

		@Override
		public Color setValue(Color color, int value) {
			return new Color(color.getRed(), color.getGreen(), value, color.getAlpha());
		}

	},

	ALPHA {

		@Override
		public int getValue(Color color) {
			return color.getAlpha();
		}

		@Override
		public Color setValue(Color color, int value) {
			return new Color(color.getRed(), color.getGreen(), color.getBlue(), value);
		}

	};

	/**
	 * Gets the value (0..255) of the color for the current channel
	 * 
	 * @param color
	 * @return
	 */
	public abstract int getValue(Color color);

	/**
	 * Sets the value (0..255) of the color for the current channel
	 * 
	 * @param color
	 * @param value
	 * @return
	 */
	public abstract Color setValue(Color color, int value);

	public static List<RgbChannel> RGB() {
		return Arrays.asList(RED, GREEN, BLUE);
	}
}
