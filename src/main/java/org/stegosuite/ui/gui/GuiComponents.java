package org.stegosuite.ui.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Contains methods for creating the GUI-elements and setting their default parameters.
 */
public class GuiComponents {

	final static byte LOAD_MENU_ITEM = 0;
	EmbedUi embedUi;
	private final ResourceBundle L = ResourceBundle.getBundle("Messages");
	public static final int EMPTY = 1;
	public static final int NOT_EMPTY = 0;
	public static final int PROCESSING = 2;

	Shell createShell(Display display) {
		Shell shell = new Shell(display);
		shell.setText("Stegosuite");
		Image icon = new Image(display, this.getClass().getClassLoader().getResourceAsStream("icon.png"));
		shell.setImage(icon);
		// shell.setImage(new Image(display, "resources/images/man-hat.png"));
		shell.setMinimumSize(350, 350);
		shell.setSize(650, 400);
		shell.setLayout(new FormLayout());
		return shell;
	}

	Label createStatusBar(Shell shell) {
		Label label = new Label(shell, SWT.SHADOW_NONE);
		FormData labelData = new FormData();
		labelData.left = new FormAttachment(0, 5);
		labelData.right = new FormAttachment(100);
		labelData.bottom = new FormAttachment(100, -1);
		label.setLayoutData(labelData);
		return label;
	}

	Menu createMenuBar(Shell shell) {
		Menu menuBar = new Menu(shell, SWT.BAR);
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText(L.getString("file_menu"));

		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		cascadeFileMenu.setMenu(fileMenu);

		MenuItem loadItem = new MenuItem(fileMenu, SWT.PUSH, LOAD_MENU_ITEM);
		loadItem.setText(L.getString("load_image_menu"));

		//
		cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText("Help");

		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		cascadeFileMenu.setMenu(fileMenu);

		loadItem = new MenuItem(fileMenu, SWT.PUSH, LOAD_MENU_ITEM);
		loadItem.setText("About");

		loadItem.addListener(SWT.Selection, event -> {
			int style = SWT.ICON_INFORMATION | SWT.OK;

			MyDialog d = new MyDialog(shell, SWT.OK);
			d.setText("About Stegosuite");
			d.open();

			// MessageBox dia = new MessageBox(shell, style);
			// dia.setText("Information");
			// dia.setMessage("Download completed.");
			// dia.open();

		});

		shell.setMenuBar(menuBar);
		return menuBar;
	}

	Composite createLayout(Shell parent, Control below) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final FormData formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(below);
		composite.setLayoutData(formData);
		embedUi = new EmbedUi(composite, this);
		return composite;
	}

	Composite createControlsComposite(Composite parent) {
		Composite compositeControls = new Composite(parent, SWT.NONE);

		GridLayout mGridLayout = new GridLayout(2, true);
		mGridLayout.verticalSpacing = 18;
		mGridLayout.marginTop = 12;
		mGridLayout.marginLeft = 12;
		mGridLayout.makeColumnsEqualWidth = false;
		compositeControls.setLayout(mGridLayout);

		StyledText text = new StyledText(compositeControls, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		text.setAlwaysShowScrollBars(false);
		text.setToolTipText(L.getString("message_text_tooltip"));

		setPlaceholder(text, L.getString("message_text"));

		/*
		 * text.addListener(SWT.MouseDown, event -> { text.setText(""); });
		 */

		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		data.heightHint = 70;
		text.setLayoutData(data);

		return compositeControls;
	}

	private void setPlaceholder(Scrollable text, String placeholder) {
		text.setData(EMPTY);

		Listener entryListener = event -> {
			if (Objects.equals(text.getData(), EMPTY)) {
				text.setData(PROCESSING);
				setText(text, "");
				text.setForeground(null);
				text.setData(NOT_EMPTY);
			}
		};
		Listener exitListener = event -> {
			if (Objects.equals(text.getData(), NOT_EMPTY) && getText(text).isEmpty()) {
				text.setData(PROCESSING);
				setText(text, placeholder);
				text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
				text.setData(EMPTY);
			}
		};

		text.addListener(SWT.FocusIn, entryListener);
		text.addListener(SWT.Verify, entryListener);

		text.addListener(SWT.Modify, exitListener);
		text.addListener(SWT.FocusOut, exitListener);

		entryListener.handleEvent(null);
		exitListener.handleEvent(null);
	}

	private void setText(Scrollable textField, String value) {
		// Reflection was needed to call the method on instances of Text and StyledText
		// (whose common ancestor class is Scrollable)
		try {
			Method setText = textField.getClass().getMethod("setText", String.class);
			setText.invoke(textField, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getText(Scrollable textField) {
		// Reflection was needed to call the method on instances of Text and StyledText
		// (whose common ancestor class is Scrollable)
		try {
			Method getText = textField.getClass().getMethod("getText");
			return getText.invoke(textField).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	Composite createFileEmbedding(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		final GridData gridData2Columns = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		// gridData2Columns.heightHint = 100;
		composite.setLayoutData(gridData2Columns);

		GridLayout mGridLayout = new GridLayout(2, true);
		mGridLayout.verticalSpacing = 3;
		mGridLayout.marginLeft = 0;
		mGridLayout.marginRight = 0;
		mGridLayout.marginTop = 0;
		mGridLayout.horizontalSpacing = 0;
		mGridLayout.marginWidth = 0;
		mGridLayout.makeColumnsEqualWidth = false;
		composite.setLayout(mGridLayout);

		Label l = new Label(composite, SWT.NONE);
		Label l2 = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.END;
		l2.setLayoutData(gd);

		l2.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));

		final Table table = new Table(composite, SWT.SINGLE | SWT.BORDER);
		final GridData gridData2Columns2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gridData2Columns2.heightHint = 50;

		table.setLayoutData(gridData2Columns2);

		TableColumn column1 = new TableColumn(table, SWT.LEFT);
		TableColumn column2 = new TableColumn(table, SWT.RIGHT);

		final TableColumn[] columns = table.getColumns();
		// for (int i = 0; i < 1; i++) {
		TableItem item = new TableItem(table, SWT.NONE);

		item.setText(0, "Item");
		item.setText(1, "123 KB");
		item.setForeground(1, Display.getDefault().getSystemColor(SWT.COLOR_GRAY));

		// }
		for (TableColumn column : columns) {
			column.pack();
		}
		table.remove(0);
		l.setText(table.getItemCount() + " " + L.getString("files_text"));

		// final DropTarget dropTarget = new DropTarget(table, DND.DROP_MOVE);
		// dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		// dropTarget.addDropListener(new DropTargetAdapter() {
		//
		// @Override
		// public void drop(final DropTargetEvent event) {
		// final String[] filenames = (String[]) event.data;
		// final Path p = Paths.get(filenames[0]);
		// String file = p.getFileName().toString();
		// TableItem item = new TableItem(table, SWT.NONE);
		// item.setText(file);
		// }
		// });

		// Text t2 = new Text(parent, SWT.SINGLE | SWT.BORDER);
		// t2.setText(L.getString("file_text"));
		// t2.setEnabled(false);
		// t2.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		//
		// Button b = new Button(parent, SWT.PUSH);
		// b.setLayoutData(new GridData(SWT.RIGHT, SWT.BEGINNING, false, false));
		// b.setToolTipText(L.getString("file_button_tooltip"));
		// b.setText(L.getString("file_button"));
		return composite;
	}

	Text createPasswordField(Composite parent) {

		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1);

		// Label labelKey = new Label(g, SWT.LEFT);
		// labelKey.setText("Secret key:");
		// labelKey.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

		Text txtPassword = new Text(parent, SWT.SINGLE | SWT.BORDER);
		txtPassword.setToolTipText(L.getString("key_text_tooltip"));
		txtPassword.setLayoutData(data);

		setPlaceholder(txtPassword, L.getString("key_text"));

		// GridLayout mGridLayoutEncryption = new GridLayout(1, true);
		// mGridLayoutEncryption.verticalSpacing = 6;
		// mGridLayoutEncryption.marginWidth = 0;
		// mGridLayoutEncryption.marginHeight = 0;
		// Composite compositeEncryption = new Composite(g, SWT.NONE);
		// compositeEncryption.setLayoutData(data);
		// compositeEncryption.setLayout(mGridLayoutEncryption);

		// Button checkBox1 = new Button(compositeEncryption, SWT.CHECK);
		// checkBox1.setText("Encryption");
		// checkBox1.setToolTipText("Encrypt the payload using AES with a secret key.");
		//
		// Text t3 = new Text(compositeEncryption, SWT.SINGLE | SWT.BORDER);
		// t3.setEnabled(false);
		// t3.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		// t3.addListener(SWT.MouseDown, event -> {
		// t3.selectAll();
		// });
		//
		// checkBox1.addListener(SWT.Selection, event -> {
		// t3.setEnabled(checkBox1.getSelection());
		// if (checkBox1.getSelection()) {
		// t3.setText("secret-message-key");
		// } else {
		// t3.setText("");
		// }
		// });

		return txtPassword;
	}

	Button createMainButton(Composite parent, String text) {
		Button b = new Button(parent, SWT.PUSH);
		b.setText(text);
		GridData gd = new GridData();
		gd.widthHint = (int) ((b.computeSize(SWT.DEFAULT, SWT.DEFAULT).x) * 1.6);
		b.setLayoutData(gd);
		return b;
	}

	Composite createImageComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginRight = 18;
		gridLayout.marginLeft = 12;
		gridLayout.marginHeight = 17;
		gridLayout.verticalSpacing = 6;
		gridLayout.horizontalSpacing = 0;
		composite.setLayout(gridLayout);

		return composite;
	}
}

class MyDialog
		extends Dialog {

	Object result;

	public MyDialog(Shell parent, int style) {
		super(parent, style);
	}

	public MyDialog(Shell parent) {
		this(parent, 0);
	}

	private Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		image.dispose();
		return scaled;
	}

	public Object open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(getText());

		Image icon = new Image(shell.getDisplay(), this.getClass().getClassLoader().getResourceAsStream("icon.png"));

		icon = resize(icon, 96, 96);
		ImageData imageData = icon.getImageData();
		imageData.transparentPixel = imageData.getPixel(0, 0);
		icon.dispose();

		final Image icon2 = new Image(null, imageData);

		shell.setSize(400, 350);
		shell.setLayout(new FormLayout());

		Label lblIcon = null;
		if (icon2 != null) {
			lblIcon = new Label(shell, SWT.TRANSPARENT);
			lblIcon.setImage(icon2);
			lblIcon.setSize(new Point(96, 96));

			FormData fd_lblIcon = new FormData();
			int offsetX = -lblIcon.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2;

			fd_lblIcon.top = new FormAttachment(0, 20);
			fd_lblIcon.left = new FormAttachment(50, offsetX);
			lblIcon.setLayoutData(fd_lblIcon);

		}

		Label lblMessage = new Label(shell, SWT.WRAP);
		FormData fd_lblMessage = new FormData();
		lblMessage.setText("Stegosuite");

		final FontData[] fontData = lblMessage.getFont().getFontData();
		for (FontData element : fontData) {
			element.setHeight(13);
		}
		Font font = new Font(shell.getDisplay(),
				new FontData(fontData[0].getName(), fontData[0].getHeight(), SWT.BOLD));
		lblMessage.setFont(font);

		int offsetX = -lblMessage.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2;

		fd_lblMessage.top = new FormAttachment(lblIcon, 20);
		fd_lblMessage.left = new FormAttachment(50, offsetX);

		lblMessage.setLayoutData(fd_lblMessage);

		Label lblMessage2 = new Label(shell, SWT.WRAP);
		FormData fd_lblMessage2 = new FormData();
		lblMessage2.setText("0.8");
		offsetX = -lblMessage2.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2;
		fd_lblMessage2.top = new FormAttachment(lblMessage, 15);
		fd_lblMessage2.left = new FormAttachment(50, offsetX);
		lblMessage2.setLayoutData(fd_lblMessage2);

		Label lblMessage3 = new Label(shell, SWT.WRAP | SWT.CENTER);
		FormData fd_lblMessage3 = new FormData();
		lblMessage3.setText("Stegosuite is a free steganography tool for hiding\n information in image files. "
				+ "Written in Java using SWT");
		offsetX = -lblMessage3.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2;
		fd_lblMessage3.top = new FormAttachment(lblMessage2, 15);
		fd_lblMessage3.left = new FormAttachment(0, 15);
		fd_lblMessage3.right = new FormAttachment(100, -15);
		lblMessage3.setLayoutData(fd_lblMessage3);

		Button btnOk = new Button(shell, SWT.NONE);

		btnOk.addListener(SWT.Selection, event -> {
			shell.dispose();
		});

		FormData fd_btnOk = new FormData();
		fd_btnOk.bottom = new FormAttachment(100, -15);
		fd_btnOk.left = new FormAttachment(100, -95);
		fd_btnOk.right = new FormAttachment(100, -15);
		btnOk.setLayoutData(fd_btnOk);
		btnOk.setText("Close");

		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}
}
