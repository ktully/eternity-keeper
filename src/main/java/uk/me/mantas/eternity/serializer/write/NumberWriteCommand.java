package uk.me.mantas.eternity.serializer.write;

import java.io.IOException;

public class NumberWriteCommand extends WriteCommand {
	public int data;
	public NumberWriteCommand (int data) {
		this.data = data;
	}

	@Override
	public void write (BinaryWriter writer) throws IOException {
		writer.writeNumber(data);
	}
}
