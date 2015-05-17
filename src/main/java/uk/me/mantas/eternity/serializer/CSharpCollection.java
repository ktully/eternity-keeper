package uk.me.mantas.eternity.serializer;

import java.util.ArrayList;
import java.util.List;

public class CSharpCollection {
	private List backing = new ArrayList<>();
	public int Capacity;

	@SuppressWarnings("unchecked")
	public void add (Object item) {
		backing.add(item);
	}
}
