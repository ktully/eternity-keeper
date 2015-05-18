package uk.me.mantas.eternity.serializer;

import java.util.TreeMap;

public class SortedList {
	private TreeMap backing = new TreeMap();
	public int Capacity;

	@SuppressWarnings("unchecked")
	public void put (Object key, Object value) {
		backing.put(key, value);
	}
}
