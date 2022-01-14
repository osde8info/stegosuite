package org.stegosuite.image.embedding;

import org.eclipse.swt.graphics.ImageData;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.util.ColorUtils;
import org.stegosuite.util.ImageSwtAwtConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class Visualizer {

	/**
	 * The different modes of visualization
	 */
	public enum VisualizationMode {
		ALTERED, UNALTERED
	}

	/**
	 * Ties a mode to a specific color
	 */
	public static class Visualize {

		private final VisualizationMode mode;

		private final Color color;

		public Visualize(VisualizationMode mode, Color color) {
			this.mode = mode;
			this.color = color;
		}

		public VisualizationMode getMode() {
			return mode;
		}

		public Color getColor() {
			return color;
		}

	}

	private Map<VisualizationMode, Color> visualizations = null;

	private BufferedImage visualizationBufferedImage = null;

	public Visualizer(ImageFormat image, Visualize... visualizations) {
		visualizationBufferedImage = ColorUtils.cloneBufferedImage(image.getBufferedImage());
		this.visualizations = Arrays.stream(visualizations).collect(toMap(Visualize::getMode, Visualize::getColor));
	}

	/**
	 * Changes the color of the visualization image at the specified point according to the
	 * visualization mode
	 * 
	 * @param point
	 * @param mode
	 */
	public void visualize(Point point, VisualizationMode mode) {
		visualizationBufferedImage.setRGB(point.x, point.y, visualizations.get(mode).getRGB());
	}

	/**
	 * Returns a copy of the visualization image in ImageData format
	 * 
	 * @return
	 */
	public ImageData getImageData() {
		return ImageSwtAwtConverter.convertToSWT(visualizationBufferedImage);
	}

}
