package org.stegosuite.ui.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.*;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoImageException;

import java.util.ResourceBundle;

/**
 * Base class for the GUI. Contains global GUI-elements and global listeners.
 */
public class Gui {

	private final Display display;
	private final Shell shell;
	private final GuiComponents guiComponents;
	private final Menu menuBar;
	private Composite composite;

	private static Label statusBar;
	private final ResourceBundle L = ResourceBundle.getBundle("Messages");

	private String imagePath = null;

	public Gui(String path) {
		display = new Display();
		guiComponents = new GuiComponents();
		shell = guiComponents.createShell(display);
		statusBar = guiComponents.createStatusBar(shell);
		menuBar = guiComponents.createMenuBar(shell);

		if (path != null) {
			imagePath = path;
		} else {
			showStartScreen();
		}

		final String[] FILTER_NAMES = { "All supported files (*.bmp/*.gif/*.jpg/*.png)", "BMP-Files (*.bmp)",
				"GIF-Files (*.gif)", "JPG-Files (*.jpg)", "PNG-Files (*.png)" };
		final String[] FILTER_EXTS = { "*.bmp;*.gif;*.jpg;*.png", "*.bmp", "*.gif", "*.jpg", "*.png" };

		// Drag and drop files into the window to load them
		final DropTarget dropTarget = new DropTarget(shell, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(final DropTargetEvent event) {
				final String[] filenames = (String[]) event.data;
				loadImages(filenames[0]);
			}
		});

		// when user clicks in menubar on "Load file", open a file dialog
		menuBar.getItem(0).getMenu().getItem(0).addListener(SWT.Selection, event -> {
			FileDialog dlg = new FileDialog(shell, SWT.OPEN);
			dlg.setFilterNames(FILTER_NAMES);
			dlg.setFilterExtensions(FILTER_EXTS);
			loadImages(dlg.open());
		});
		shell.setMenuBar(menuBar);
		startEventLoop();
	}

	private void startEventLoop() {
		// Display Window in the middle of screen
		final Rectangle bds = display.getBounds();
		final Point p = shell.getSize();
		final int nLeft = (bds.width - p.x) / 2;
		final int nTop = (bds.height - p.y) / 2;
		shell.setBounds(nLeft, nTop, p.x, p.y);
		// ======================================
		shell.open();

		if (imagePath != null) {
			loadImages(imagePath);
		}

		// main loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * Loads a gif- or bmp-image and displays it.
	 *
	 * @param path absolute file-path of the image
	 */
	private void loadImages(String path) {
		try {
			ImageFormat image = ImageFormat.getImageFormat(path);
            if (image != null) {
				initializeEmbedUi();
				guiComponents.embedUi.loadImage(image);
			}
		} catch (SteganoImageException e) {
			e.printStackTrace();
		}
	}

	private void initializeEmbedUi() {
		if (composite == null) {
            removeStartScreen();
            startLayout();
        }
	}

	private void removeStartScreen() {
		if (shell.getChildren().length > 1) {
            shell.getChildren()[1].dispose();
        }
	}

	/**
	 * Sets the message of the global status bar.
	 *
	 * @param s String which gets displayed.
	 */
	static void setStatusBarMsg(final String s) {
		statusBar.setText(s);
	}

	private void showStartScreen() {
		final Label label = new Label(shell, SWT.SHADOW_NONE);
		label.setText(L.getString("start_text"));

		// increase font size
		final FontData[] fontData = label.getFont().getFontData();
		for (FontData element : fontData) {
			element.setHeight(20);
		}
		label.setFont(new Font(display, fontData));

		// place label in the middle of the window
		shell.layout(true, true);
		final int offset = label.getBounds().width / 2;
		final FormData labelData = new FormData();
		labelData.left = new FormAttachment(50, -offset);
		labelData.bottom = new FormAttachment(50);
		label.setLayoutData(labelData);
		shell.layout(true, true);
	}

	private void startLayout() {
		composite = guiComponents.createLayout(shell, statusBar);
		shell.layout(true, true);
	}
}
