package uk.me.mantas.eternity.serializer.write;

import uk.me.mantas.eternity.serializer.BinaryWriter;

import java.io.IOException;

public abstract class WriteCommand {
	public abstract void write (BinaryWriter writer) throws IOException;
}