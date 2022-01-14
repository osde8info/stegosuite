package org.stegosuite.image.embedding.png.filter;

import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.PNGImage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Allows embedding into all points of a PNG image
 */
public class PNGPointFilterNone
		extends PointFilter<PNGImage> {

	@Override
	public int maxLsbCount() {
		return 8;
	}

	@Override
	protected Collection<Point> filter(PNGImage image) {
		return new ArrayList<>();
	}

}
