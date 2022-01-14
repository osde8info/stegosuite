package org.stegosuite.ui.gui;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.application.EmbeddingProgressObserver;
import org.stegosuite.application.StegosuitePresenter;
import org.stegosuite.application.StegosuiteUI;
import org.stegosuite.image.embedding.EmbeddingProgress;
import org.stegosuite.image.embedding.Visualizer;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoEmbedException;
import org.stegosuite.model.exception.SteganoExtractException;
import org.stegosuite.model.payload.block.FileBlock;
import org.stegosuite.ui.gui.ImageContainer.ImageState;

/**
 * Contains the GUI for embedding/extracting data.
 */
public class EmbedUi implements StegosuiteUI {

	private Composite compositeImage;
	private Text passwordField;
	private Button checkBoxVisualize;
	private ImageContainer imageContainer;
	private Label imageLabel, payloadFileCounter, payloadFileSize;
	private int fileSizeSum = 0;
	private Button embedButton, extractButton;
	private Cursor cursor;
	private ProgressBar progressBar;
	private Table fileTable;
	private StyledText messageField;
	private Composite composite;
	private static final Logger LOG = LoggerFactory.getLogger(EmbedUi.class);
	private final ResourceBundle L = ResourceBundle.getBundle("Messages");
	private StegosuitePresenter presenter;

	public EmbedUi(Composite composite, GuiComponents components) {
		this.composite = composite;

		initializeGui(components);
	}

	private void initializeGui(GuiComponents components) {
		composite.setLayout(new FillLayout());

		Composite compositeControls = components.createControlsComposite(composite);
		Composite fileEmbedding = components.createFileEmbedding(compositeControls);

		messageField = (StyledText) compositeControls.getChildren()[0];
		fileTable = (Table) fileEmbedding.getChildren()[2];
		payloadFileCounter = (Label) fileEmbedding.getChildren()[0];
		payloadFileSize = (Label) fileEmbedding.getChildren()[1];
		passwordField = components.createPasswordField(compositeControls);

		embedButton = components.createMainButton(compositeControls, L.getString("embed_button"));
		extractButton = components.createMainButton(compositeControls, L.getString("extract_button"));
		((GridData) extractButton.getLayoutData()).horizontalAlignment = SWT.END;

		compositeImage = components.createImageComposite(composite);
		imageLabel = new Label(compositeImage, SWT.NONE);
		imageLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		compositeImage.addListener(SWT.Resize, event -> {
			if (imageLabel.getImage() != null) {
				imageLabel.setImage(imageContainer.scaleImage());
				compositeImage.layout(true, true);
			}
		});

		final DropTarget dropTarget = new DropTarget(fileTable, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(final DropTargetEvent event) {
				final String[] filenames = (String[]) event.data;
				String filename = filenames[0];
				if (filename != null) {
					presenter.addFileToPayload(filename);
				}
			}
		});

		Menu menu = new Menu(composite.getShell(), SWT.POP_UP);
		fileTable.setMenu(menu);

		MenuItem deleteFileMenu = new MenuItem(menu, SWT.PUSH);
		deleteFileMenu.setText(L.getString("files_menu_delete"));
		deleteFileMenu.addListener(SWT.Selection, event -> removeSelectedPayloadFile());

		MenuItem addFileMenu = new MenuItem(menu, SWT.PUSH);
		addFileMenu.setText(L.getString("files_menu_add"));
		addFileMenu.addListener(SWT.Selection, event -> {
			final FileDialog dlg = new FileDialog(composite.getShell(), SWT.OPEN);
			final String filePath = dlg.open();
			if (filePath != null) {
				presenter.addFileToPayload(filePath);
			}
		});

		fileTable.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeSelectedPayloadFile();
				}
			}
		});

		embedButton.addListener(SWT.Selection, event -> {
			startEmbedding(compositeControls);
		});

		extractButton.addListener(SWT.Selection, event -> {
			startExtraction(compositeControls);
		});
	}

	private void startEmbedding(Composite compositeControls) {
		progressBar = new ProgressBar(compositeControls, SWT.SMOOTH);
		progressBar.setSelection(0);

		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.horizontalSpan = 2;
		progressBar.setLayoutData(data);
		compositeControls.layout(true, true);

		adjustWindowSize();

		Gui.setStatusBarMsg("Embedding data...");
		embedButton.setEnabled(false);

		cursor = new Cursor(Display.getDefault(), SWT.CURSOR_WAIT);
		composite.getShell().setCursor(cursor);

		EmbeddingProgress progress = new EmbeddingProgress();
		new EmbeddingProgressObserver(progressBar, progress);

		String message = messageField.getText();
		String password = getEnteredPassword();
		
		presenter.addMessageToPayload(message);

		runInNewThread(() -> presenter.embedNotifying(progress, password));
	}

	private void startExtraction(Composite compositeControls) {
		progressBar = new ProgressBar(compositeControls, SWT.SMOOTH);
		progressBar.setSelection(0);

		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.horizontalSpan = 2;
		progressBar.setLayoutData(data);
		compositeControls.layout(true, true);

		adjustWindowSize();

		EmbeddingProgress progress = new EmbeddingProgress();
		new EmbeddingProgressObserver(progressBar, progress);

		Gui.setStatusBarMsg("Extracting data...");
		extractButton.setEnabled(false);

		cursor = new Cursor(Display.getDefault(), SWT.CURSOR_WAIT);
		composite.getShell().setCursor(cursor);

		String password = getEnteredPassword();
		runInNewThread(() -> presenter.extractNotifying(progress, password));
	}

	private String getEnteredPassword() {
		if (passwordField.getText().isEmpty()) {
			return null;
		} else {
			return passwordField.getText();
		}
	}

	private void runInNewThread(Runnable runnable) {
		new Thread(runnable).start();
	}

	@Override
	public void showEmbeddingError(SteganoEmbedException e) {
		displayError("Embedding aborted.", embedButton, e.getMessage());
	}

	@Override
	public void showExtractingError(SteganoExtractException e) {
		displayError("Extracting aborted.", extractButton, e.getMessage());
	}

	@Override
	public void extractingCompleted(String extractedMessage, List<String> filePaths, Visualizer visualizer, ImageData imageData) {
		runInGuiThread(() -> {
			messageField.setText(extractedMessage);
			
			String status = "Extracting completed.";
			if (!filePaths.isEmpty()) {
				status += " Extracted file saved to " + filePaths.get(filePaths.size() - 1);
			}
			
			Gui.setStatusBarMsg(status);
			imageContainer.setImageData(ImageState.STEG, imageData);
			if (visualizer != null) {
				imageContainer.setImageData(ImageState.STEG_VISUALIZED, visualizer.getImageData());
			}
			extractButton.setEnabled(true);
			updateVisualizationCheckbox();
			cursor = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
			compositeImage.getShell().setCursor(cursor);
			progressBar.dispose();
		});
	}

	private void runInGuiThread(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	@Override
	public void embeddingCompleted(ImageFormat embeddedImage, String outputPath, Visualizer visualizer) {
		runInGuiThread(() -> {
			imageContainer.setImageData(ImageState.STEG, embeddedImage.getImageData());
			if (visualizer != null) {
				imageContainer.setImageData(ImageState.STEG_VISUALIZED, visualizer.getImageData());
			}
			Gui.setStatusBarMsg("Embedding completed. File saved to " + outputPath);
			embedButton.setEnabled(true);
			imageLabel.setImage(imageContainer.scaleImage(ImageState.STEG));
			updateVisualizationCheckbox();
			compositeImage.layout(true, true);
			presenter.clearPayload();
			fileTable.clearAll();
			fileTable.setItemCount(0);
			fileSizeSum = 0;
			payloadFileSize.setText("");
			updateFilesCount();
			cursor = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
			compositeImage.getShell().setCursor(cursor);
			progressBar.dispose();
		});
	}

	private void displayError(String statusBarMessage, Button actionButton, String message) {
		runInGuiThread(() -> {
			MessageBox dialog = new MessageBox(composite.getShell(), SWT.ICON_ERROR | SWT.OK);
			dialog.setText("Error");
			dialog.setMessage(message);
			dialog.open();
			actionButton.setEnabled(true);
			cursor = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
			compositeImage.getShell().setCursor(cursor);
			progressBar.dispose();
			Gui.setStatusBarMsg(statusBarMessage);
		});
	}

	/**
	 * Loads an image from the given path, displays it and start capacity- and noise-calculations.
	 *
	 * @param image The image to load
	 */
	void loadImage(ImageFormat image) {
		presenter = new StegosuitePresenter(image, this);

		embedButton.setEnabled(false);
		extractButton.setEnabled(false);
		imageContainer = new ImageContainer(compositeImage);

		Image img = imageContainer.loadImage(image.getImageData());
		imageLabel.setImage(img);
		imageLabel.setToolTipText(image.getFilePath());

		messageField.setEnabled(true);
		messageField.setText("");
		passwordField.setText("");

		fileTable.clearAll();
		fileTable.setItemCount(0);
		payloadFileSize.setText("");
		fileSizeSum = 0;
		updateFilesCount();
		presenter.clearPayload();
		updateVisualizationCheckbox();
		compositeImage.layout(true, true);

		Gui.setStatusBarMsg("Searching for homogeneous areas in the image...");
		cursor = new Cursor(Display.getDefault(), SWT.CURSOR_WAIT);
		passwordField.getParent().setCursor(cursor);
		passwordField.getParent().layout(true, true);

		runInNewThread(() -> {
			int capacity = presenter.getEmbeddingCapacity();

			runInGuiThread(() -> {
				Gui.setStatusBarMsg(L.getString("statusbar_capacity") + ": " + ImageUtils.formatSize(capacity));
				embedButton.setEnabled(true);
				extractButton.setEnabled(true);
				cursor = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
				passwordField.getParent().setCursor(cursor);
			});
		});
	}

	private void updateVisualizationCheckbox() {
		if (imageContainer.getImageData(ImageState.STEG_VISUALIZED) != null) {
			if (checkBoxVisualize == null || checkBoxVisualize.isDisposed()) {
				checkBoxVisualize = new Button(compositeImage, SWT.CHECK);
				checkBoxVisualize.setText(L.getString("visualize_checkbox"));
				checkBoxVisualize.addListener(SWT.Selection, event2 -> {
					ImageState state = checkBoxVisualize.getSelection() ? ImageState.STEG_VISUALIZED : ImageState.STEG;
					imageLabel.setImage(imageContainer.scaleImage(state));
				});
			} else {
				checkBoxVisualize.setSelection(false);
			}
		} else {
			if (checkBoxVisualize != null) {
				checkBoxVisualize.dispose();
			}
		}
	}

	private void adjustWindowSize() {
		final Point newSize = passwordField.getShell().computeSize(passwordField.getShell().getSize().x, SWT.DEFAULT,
				true);
		if (newSize.y > passwordField.getShell().getSize().y) {
			passwordField.getShell().setSize(newSize);
		}
	}

	@Override
	public void addPayloadFile(String filename, String extension, long fileSize) {
		runInGuiThread(() -> {
			fileSizeSum += fileSize;
			addTableItemForFile(filename, fileSize, systemIconFor(extension));
			updateFilesCount();
			updateTotalFilesSize(fileSizeSum);
			redrawFilesTable();
		});
	}

	private void updateFilesCount() {
		payloadFileCounter.setText(fileTable.getItemCount() + " " + L.getString("files_text"));
	}

	private void redrawFilesTable() {
		for (TableColumn column : fileTable.getColumns()) {
			column.pack();
		}
		payloadFileSize.getParent().layout(true, true);
	}

	private void updateTotalFilesSize(int fileSizeSum) {
		payloadFileSize.setText(ImageUtils.formatSize(fileSizeSum));
	}

	private static Optional<Image> systemIconFor(String extension) {
		return Optional.ofNullable(Program.findProgram(extension))
				.map(program -> program.getImageData())
				.map(data -> new Image(Display.getDefault(), data));
	}

	private void addTableItemForFile(String filename, long fileSize, Optional<Image> icon) {
		TableItem item = new TableItem(fileTable, SWT.NONE);
		item.setText(filename);
		item.setText(1, ImageUtils.formatSize(fileSize));
		item.setForeground(1, Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		icon.ifPresent(image -> item.setImage(image));
	}

	private void removeSelectedPayloadFile() {
		if (aFileIsSelected()) {
			String selectedFilename = getSelectedFilename();

            fileTable.remove(fileTable.getSelectionIndex());
            // FIXME: If two files with the same name are added (being both the same file or not),
			// all occurrences are deleted internally, but only one is deleted on the ui table
            presenter.payloadFileBlocksWithFilename(selectedFilename)
                    .forEach(this::removeFileBlock);

            updateFilesCount();
            payloadFileSize.getParent().layout(true, true);
		}
	}

	private String getSelectedFilename() {
		TableItem selectedItem = fileTable.getItem(fileTable.getSelectionIndex());
		return selectedItem.getText();
	}

	private boolean aFileIsSelected() {
		return fileTable.getItemCount() > 0 && fileTable.getSelectionIndex() >= 0;
	}

	private void removeFileBlock(FileBlock fileBlock) {
		fileSizeSum -= fileBlock.getSize();
		updateTotalFilesSize(fileSizeSum);
		presenter.removeBlock(fileBlock);
		LOG.debug("Fileblock removed.");
	}

}
