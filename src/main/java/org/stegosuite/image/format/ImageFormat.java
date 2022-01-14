package org.stegosuite.image.format;

import org.eclipse.swt.graphics.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.util.ColorUtils;
import org.stegosuite.util.FileUtils;
import org.stegosuite.util.ImageSwtAwtConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * All supported image formats need to extended this class and implement its abstract methods
 */
public abstract class ImageFormat {

	private static final Logger LOG = LoggerFactory.getLogger(ImageFormat.class);

	private static final Map<String, Class<? extends ImageFormat>> registeredImageExtensions = new TreeMap<>(
			String.CASE_INSENSITIVE_ORDER);

	static {
		registeredImageExtensions.put(BMPImage.FILE_EXTENSION, BMPImage.class);
		registeredImageExtensions.put(GIFImage.FILE_EXTENSION, GIFImage.class);
		registeredImageExtensions.put(JPGImage.FILE_EXTENSION, JPGImage.class);
		registeredImageExtensions.put(PNGImage.FILE_EXTENSION, PNGImage.class);
	}

	/**
	 * The file instance from which the image was loaded
	 */
	protected File file = null;

	/**
	 * The image data in AWT format
	 */
	protected BufferedImage image = null;

	/**
	 * The image data in SWT format
	 */
	protected ImageData imageData = null;

	/**
	 * Loads an image from disk
	 *
	 * @param file
	 * @throws SteganoImageException
	 */
	public void load(File file)
			throws SteganoImageException {
		LOG.info("Loading {} image from {}", getFileExtension(), file.getAbsolutePath());
		this.file = file;
		try {
			setBufferedImage(ImageIO.read(file));
		} catch (IOException e) {
			throw new SteganoImageException(e.getMessage());
		}
	}

	/**
	 * Saves the image to disk
	 *
	 * @param file
	 * @throws SteganoImageException
	 */
	public void save(File file)
			throws SteganoImageException {
		LOG.info("Saving {} image to {}", getFileExtension(), file.getAbsolutePath());
		try {
			ImageIO.write(image, getFileExtension(), file);
		} catch (IOException e) {
			throw new SteganoImageException(e.getMessage());
		}
	}

	/**
	 * Returns the file extension of the image format
	 *
	 * @return
	 */
	public abstract String getFileExtension();

	/**
	 * Returns the image's width
	 *
	 * @return
	 */
	public int getWidth() {
		return image.getWidth();

	}

	/**
	 * Returns the image's height
	 *
	 * @return
	 */
	public int getHeight() {
		return image.getHeight();
	}

	/**
	 * Returns the image in AWT's BufferedImage format
	 *
	 * @return
	 */
	public BufferedImage getBufferedImage() {
		return image;
	}

	/**
	 * Sets an image.
	 *
	 * @param image AWT's BufferedImage format
	 */
	public void setBufferedImage(BufferedImage image) {
		this.image = image;
		imageData = null;
	}

	/**
	 * Returns the image in SWT's ImageData format
	 *
	 * @return
	 */
	public ImageData getImageData() {
		if (imageData == null) {
			imageData = ImageSwtAwtConverter.convertToSWT(image);
		}
		return imageData;
	}

	public File getFile() {
		return file;
	}

	public ImageFormat clone(int imageTpye) {
		ImageFormat clonedImage = newInstance(this.getClass());
		clonedImage.setBufferedImage(ColorUtils.cloneBufferedImage(image, imageTpye));
		clonedImage.file = file;
		return clonedImage;
	}

	@Override
	public ImageFormat clone() {
		return this.clone(image.getType());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ImageFormat other = (ImageFormat) obj;
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		} else if (!file.equals(other.file)) {
			return false;
		}
		if (image == null) {
			if (other.image != null) {
				return false;
			}
		} else if (!image.equals(other.image)) {
			return false;
		}
		return true;
	}

	public static Set<String> getSupportedFormats() {
		return getRegisteredImageExtensions().keySet();
	}

	private static Map<String, Class<? extends ImageFormat>> getRegisteredImageExtensions() {
		return registeredImageExtensions;
	}

	/**
	 * Returns a new instance of an ImageFormat depending on which class registered the filename
	 * extension
	 *
	 * @param fileNameExtension
	 * @return
	 */
	public static ImageFormat newInstance(String fileNameExtension) {
		Class<? extends ImageFormat> imageFormatClass = getRegisteredImageExtensions().get(fileNameExtension);
		return imageFormatClass == null ? null : newInstance(imageFormatClass);
	}

	/**
	 * Returns a new instance of the imageFormatClass
	 *
	 * @param imageFormatClass
	 * @return
	 */
	public static ImageFormat newInstance(Class<? extends ImageFormat> imageFormatClass) {
		try {
			return imageFormatClass.getConstructor().newInstance();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return null;
	}

	/**
     * Returns the ImageFormat object for a given image file path
	 *
	 * @param path absolute file-path of the image
	 * @return the image format for the image
	 * @throws SteganoImageException
	 */
	public static ImageFormat getImageFormat(String path) throws SteganoImageException {
		ImageFormat image = null;
		if (path != null) {
			String extension = FileUtils.getFileExtension(path);
			if (getRegisteredImageExtensions().containsKey(extension)) {
				image = newInstance(extension);
				image.load(new File(path));
			}
		}
		return image;
	}

	public String getFilePath() {
		return file.getAbsolutePath();
	}
}
