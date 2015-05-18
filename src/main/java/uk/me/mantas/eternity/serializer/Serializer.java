package uk.me.mantas.eternity.serializer;

import java.io.DataOutput;

public class Serializer {
	private final DataOutput stream;

	public Serializer (DataOutput stream) {
		this.stream = stream;
	}

	public void serialize (Object obj) {

	}
}
