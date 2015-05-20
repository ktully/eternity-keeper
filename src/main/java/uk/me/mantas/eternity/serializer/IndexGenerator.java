package uk.me.mantas.eternity.serializer;

import java.util.ArrayList;
import java.util.List;

public class IndexGenerator<T> {
	public final List<T> items = new ArrayList<>();

	public int getIndexOfItem (T item) {
		int index = items.indexOf(item);
		if (index > -1) {
			return index;
		}

		items.add(item);
		return items.size() - 1;
	}
}
