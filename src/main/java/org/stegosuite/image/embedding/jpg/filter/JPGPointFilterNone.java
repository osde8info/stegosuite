package org.stegosuite.image.embedding.jpg.filter;

import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.JPGImage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Allows embedding into all points of a GIF image
 */
public class JPGPointFilterNone
		extends PointFilter<JPGImage> {

	@Override
	public int maxLsbCount() {
		return 1;
	}

	@Override
	protected Collection<Point> filter(JPGImage image) {
		return new ArrayList<>();
	}
}
