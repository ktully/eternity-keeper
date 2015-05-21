package uk.me.mantas.eternity.serializer;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

@SuppressWarnings("unchecked")
public class SortedList {
	private TreeMap backing = new TreeMap();
	public int Capacity;

	public void put (Object key, Object value) {
		backing.put(key, value);
	}

	public Set<Entry> entrySet () {
		return backing.entrySet();
	}

	public Iterator<Entry> iterator () {
		return backing.entrySet().iterator();
	}
}
