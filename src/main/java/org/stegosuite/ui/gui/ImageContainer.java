package org.stegosuite.ui.gui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;

import java.util.HashMap;
import java.util.Map;

public class ImageContainer {

	public enum ImageState {
		CARRIER, CARRIER_SCALED, STEG, STEG_VISUALIZED, STEG_LSB, STEG_LSB2
	}

	private Image image = null;
	private ImageState state = ImageState.CARRIER;
	private Map<ImageState, ImageData> images = new HashMap<>();
	private Composite composite = null;

	public ImageContainer(Composite composite) {
		this.composite = composite;
	}

	public Image scaleImage() {
		return scaleImage(state);
	}

	public Image scaleImage(ImageState state) {
		this.state = state;
		return loadImage(images.get(state));
	}

	public Image loadImage(ImageData imageData) {
		if (composite.getChildren().length > 0) {
			if (image != null) {
				image.dispose();
				image = null;
			} else {
				images.put(ImageState.CARRIER, imageData);
			}
		}

		float ih = imageData.height;
		float iw = imageData.width;

		int iw_scaled = (int) iw;
		int ih_scaled = (int) ih;

		float scaleFactor = getImageScaleFactor(imageData);
		if (scaleFactor < 1) {
			iw_scaled = Math.round(iw * scaleFactor);
			ih_scaled = Math.round(ih * scaleFactor);
		}

		ImageData imageDataScaled = imageData.scaledTo(iw_scaled, ih_scaled);
		image = new Image(composite.getDisplay(), imageDataScaled);
		return image;
	}

	private float getImageScaleFactor(ImageData imageData) {
		float h = imageData.height;
		float w = imageData.width;
		float ch = composite.getBounds().height - 54;
		float cw = composite.getBounds().width - 36;

		float scaleHeight = ch / h;
		float scaleWidth = cw / w;

		if (scaleHeight < scaleWidth) {
			return scaleHeight;
		} else {
			return scaleWidth;
		}
	}

	public void setImageData(ImageState state, ImageData imageData) {
		images.put(state, imageData);
	}

	public ImageData getImageData(ImageState state) {
		return images.get(state);
	}
}
