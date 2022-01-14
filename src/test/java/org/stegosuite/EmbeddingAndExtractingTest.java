package org.stegosuite;

import org.eclipse.swt.graphics.ImageData;
import org.junit.After;
import org.junit.Test;
import org.stegosuite.application.StegosuitePresenter;
import org.stegosuite.application.StegosuiteUI;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.exception.SteganoImageException;
import org.stegosuite.model.exception.SteganoKeyException;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.stegosuite.Resources.pathOf;

public class EmbeddingAndExtractingTest {

    private static final String THE_MESSAGE = "a message";
    private static final String THE_PASSWORD = "a password";
    private static final String INCORRECT_PASSWORD = "an incorrect password";

    private final StegosuiteUI ui = new UIStub();
    private String outputPath;
    private String extractedMessage;

    @After
    public void tearDown() throws Exception {
    	Resources.delete(outputPath);
    }

    @Test
    public void testEmbeddingAndExtractingFromGifFile() throws Exception {
        testEmbeddingAndThenExtractingOk("sunflower.gif");
    }

    @Test
    public void testEmbeddingAndExtractingFromBmpFile() throws Exception {
        testEmbeddingAndThenExtractingOk("snow.bmp");
    }

    @Test
    public void testEmbeddingAndExtractingFromJpgFile() throws Exception {
        testEmbeddingAndThenExtractingOk("landscape.jpg");
    }

    @Test(expected = SteganoKeyException.class)
    public void testIncorrectPasswordFromGifFile() throws Throwable {
        testExtractingWhenPasswordIsIncorrect("sunflower_embed_ok.gif");
    }

    @Test(expected = SteganoKeyException.class)
    public void testIncorrectPasswordFromBmpFile() throws Throwable {
        testExtractingWhenPasswordIsIncorrect("snow_embed_ok.bmp");
    }

    @Test(expected = SteganoKeyException.class)
    public void testIncorrectPasswordFromJpgFile() throws Throwable {
        testExtractingWhenPasswordIsIncorrect("landscape_embed_ok.jpg");
    }

    private void testEmbeddingAndThenExtractingOk(String imageName) throws SteganoImageException, SteganoEmbedException, InterruptedException, java.util.concurrent.ExecutionException, SteganoExtractException {
        embedPayload(imageName, THE_PASSWORD, THE_MESSAGE);
        assertTrue(new File(outputPath).exists());

        extractPayload(outputPath, THE_PASSWORD);
        assertEquals(THE_MESSAGE, extractedMessage);
    }

    private void testExtractingWhenPasswordIsIncorrect(String imageName) throws Throwable {
        try {
            extractPayload(pathOf(imageName), INCORRECT_PASSWORD);
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void embedPayload(String imageName, String password, String message) throws SteganoImageException, SteganoEmbedException {
        String imagePath = pathOf(imageName);
        StegosuitePresenter presenter = getPresenterFor(imagePath);
        presenter.addMessageToPayload(message);
        presenter.embedNotifying(new EmbeddingProgress(), password);
    }

    private void extractPayload(String imagePath, String password) throws SteganoImageException, SteganoExtractException {
        StegosuitePresenter presenter = getPresenterFor(imagePath);
        presenter.extractNotifying(new EmbeddingProgress(), password);
    }

    private StegosuitePresenter getPresenterFor(String imagePath) throws SteganoImageException {
        ImageFormat image = ImageFormat.getImageFormat(imagePath);
        return new StegosuitePresenter(image, ui);
    }

    private class UIStub implements StegosuiteUI {
        @Override
		public void showEmbeddingError(SteganoEmbedException e) {
			throw new RuntimeException(e);
		}

        @Override
		public void showExtractingError(SteganoExtractException e) {
			throw new RuntimeException(e);
		}

        @Override
		public void extractingCompleted(String extractedMessage, List<String> filePaths, Visualizer visualizer, ImageData imageData) {
            EmbeddingAndExtractingTest.this.extractedMessage = extractedMessage;
		}

        @Override
		public void embeddingCompleted(ImageFormat embeddedImage, String outputPath, Visualizer visualizer) {
			EmbeddingAndExtractingTest.this.outputPath = outputPath;
		}

        @Override
		public void addPayloadFile(String filename, String extension, long fileSize) {
		}
    }
}
