package org.stegosuite.application;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.stegosuite.image.embedding.EmbeddingProgress;

import java.util.Observable;
import java.util.Observer;

public class EmbeddingProgressObserver
		implements Observer {

	private ProgressBar progressbar;

	public EmbeddingProgressObserver(ProgressBar p, EmbeddingProgress e) {
		progressbar = p;
		e.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		Display.getDefault().asyncExec(() -> progressbar.setSelection((int) arg));
	}
}
