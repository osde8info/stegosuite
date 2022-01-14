package org.stegosuite.image.embedding;

import java.util.Observable;

public class EmbeddingProgress
		extends Observable {

	private int currentPrecentage = 0;

	public EmbeddingProgress() {
		super();
	}

	public void progressUpdate(int currentByte, int totalBytes) {
		int newPercentage = (100 * currentByte) / totalBytes;
		if (newPercentage > currentPrecentage) {
			currentPrecentage = newPercentage;
			super.setChanged();
			super.notifyObservers(currentPrecentage);
		}
	}
}
