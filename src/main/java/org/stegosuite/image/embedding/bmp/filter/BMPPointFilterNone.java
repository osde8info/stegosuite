package org.stegosuite.image.embedding.bmp.filter;

import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.BMPImage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Allows embedding into all points of a BMP image
 */
public class BMPPointFilterNone
		extends PointFilter<BMPImage> {

	@Override
	public int maxLsbCount() {
		return 8;
	}

	@Override
	protected Collection<Point> filter(BMPImage image) {
		return new ArrayList<>();
	}

}
