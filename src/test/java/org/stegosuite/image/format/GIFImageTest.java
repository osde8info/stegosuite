package org.stegosuite.image.format;

import org.junit.Before;
import org.junit.Test;
import org.stegosuite.model.exception.SteganoImageException;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GIFImageTest {

	private GIFImage image = null;

	/**
	 * Using @BeforeMethod here so that we work with a fresh copy of the image for each test
	 * 
	 * @throws SteganoImageException
	 */
	@Before
	public void beforeMethod()
			throws SteganoImageException {
		image = new GIFImage();
		image.load(new File(this.getClass().getClassLoader().getResource("sunflower.gif").getFile()));
	}

	@Test
	public void testWidth() {
		assertEquals(image.getWidth(), 1160);
	}

	@Test
	public void testHeight() {
		assertEquals(image.getHeight(), 1376);
	}

	@Test
	public void testGetPixels() {
		Integer[] pixels = IntStream.of(image.getPixels()).boxed().toArray(Integer[]::new);
		assertEquals(pixels.length, image.getWidth() * image.getHeight());
		assertEquals(Arrays.deepHashCode(pixels), -2099205017);
	}

	@Test
	public void testSetPixels() {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		Arrays.setAll(pixels, i -> 0);
		image.setPixels(pixels);
		assertArrayEquals(image.getPixels(), pixels);
	}
}
