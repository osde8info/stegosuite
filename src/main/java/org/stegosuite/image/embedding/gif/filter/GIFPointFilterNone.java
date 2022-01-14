package org.stegosuite.image.embedding.gif.filter;

import org.stegosuite.image.embedding.point.PointFilter;
import org.stegosuite.image.format.GIFImage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Allows embedding into all points of a GIF image
 */
public class GIFPointFilterNone
		extends PointFilter<GIFImage> {

	@Override
	public int maxLsbCount() {
		return 1;
	}

	@Override
	protected Collection<Point> filter(GIFImage image) {
		return new ArrayList<>();
	}

}
