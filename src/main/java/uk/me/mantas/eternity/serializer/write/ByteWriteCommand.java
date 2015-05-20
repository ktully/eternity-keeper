package uk.me.mantas.eternity.serializer.write;

import java.io.IOException;

public class ByteWriteCommand extends WriteCommand {
	public byte data;
	public ByteWriteCommand (byte data) {
		this.data = data;
	}

	@Override
	public void write (BinaryWriter writer) throws IOException {
		writer.out.writeByte(data);
	}
}
