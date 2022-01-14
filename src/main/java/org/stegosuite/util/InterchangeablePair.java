package org.stegosuite.util;

public class InterchangeablePair<F, S> {

	public final F first;
	public final S second;

	public InterchangeablePair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof InterchangeablePair)) {
			return false;
		}

		InterchangeablePair<?, ?> that = (InterchangeablePair<?, ?>) o;
		return first.equals(that.first) && second.equals(that.second)
				|| first.equals(that.second) && second.equals(that.first);
	}

	@Override
	public int hashCode() {
		return first.hashCode() ^ second.hashCode();
	}
}
