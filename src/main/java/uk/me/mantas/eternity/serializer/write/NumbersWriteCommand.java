package uk.me.mantas.eternity.serializer.write;

import java.io.IOException;

public class NumbersWriteCommand extends WriteCommand {
	public int[] data;

	public NumbersWriteCommand (int[] data) {
		this.data = data;
	}

	@Override
	public void write (BinaryWriter writer) throws IOException {
		writer.writeNumbers(data);
	}
}
