package uk.me.mantas.eternity.serializer.write;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import uk.me.mantas.eternity.serializer.CSharpType;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.UUID;

public class BinaryWriter {
	public DataOutput out;

	public BinaryWriter (DataOutput out) {
		this.out = out;
	}

	public void writeValue (Object value) throws IOException {
		if (value == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			writeValueCore(value);
		}
	}

	private void writeValueCore (Object value) throws IOException {
		if (value == null) {
			System.err.printf("Cannot write null value!%n");
			return;
		}

		if (value instanceof byte[] || value instanceof Byte[]) {
			writeNumber(Array.getLength(value));
			out.write(unboxBytes(value));
			return;
		}

		if (value instanceof String) {
			writeString((String) value);
			return;
		}

		if (value instanceof Boolean) {
			out.writeBoolean((boolean) value);
			return;
		}

		if (value instanceof Byte) {
			out.writeByte((byte) value);
			return;
		}

		if (value instanceof Character) {
			out.writeChar((char) value);
			return;
		}

		if (value instanceof UUID) {
			UUID uuid = (UUID) value;
			byte[] guid = new byte[16];
			long mostSig = uuid.getMostSignificantBits();
			long leastSig = uuid.getLeastSignificantBits();
			byte[] ms = Longs.toByteArray(mostSig);
			byte[] ls = Longs.toByteArray(leastSig);
			int a = Integer.reverseBytes(ByteBuffer.wrap(ms).getInt(0));
			System.arraycopy(Ints.toByteArray(a), 0, guid, 0, 4);
			guid[4] = ms[5]; guid[5] = ms[4]; guid[6] = ms[7]; guid[7] = ms[6];
			System.arraycopy(ls, 0, guid, 8, 8);

			out.write(guid);
			return;
		}

		if (value instanceof Double) {
			out.writeDouble((double) value);
			return;
		}

		if (value instanceof Short) {
			out.writeShort((short) value);
			return;
		}

		if (value instanceof Integer) {
			out.writeInt((int) value);
			return;
		}

		if (value instanceof Long) {
			out.writeLong((long) value);
			return;
		}

		if (value instanceof Float) {
			out.writeFloat((float) value);
			return;
		}

		if (value.getClass().isEnum()) {
			int ordinal;
			Class cls = value.getClass();

			try {
				Field nField = cls.getField("n");
				ordinal = nField.getInt(value);
				out.writeInt(ordinal);
				return;
			} catch (NoSuchFieldException | IllegalAccessException ignored) {}

			Object[] constants = cls.getEnumConstants();
			for (int i = 0; i < constants.length; i++) {
				if (value == constants[i]) {
					out.writeInt(i);
					return;
				}
			}

			System.err.printf(
				"Supposed enum value couldn't be "
				+ "matched with any enum constants!%n");

			return;
		}

		if (value instanceof CSharpType) {
			writeString(((CSharpType) value).type);
			return;
		}

		if (value instanceof Class) {
			System.err.printf("Trying to write Class type unimplemented!%n");
		}
	}

	public void writeStringGuarded (String value) throws IOException {
		if (value == null || value.equals("")) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			writeString(value);
		}
	}

	private void writeString (String value)	throws IOException {
		byte[] bytes = value.getBytes("UTF-8");
		write7BitEncodedInt(bytes.length);
		out.write(bytes);
	}

	private void write7BitEncodedInt (int v) throws IOException {
		while (v >= 0x80) {
			out.writeByte((byte) (v | 0x80));
			v >>= 7;
		}

		out.writeByte((byte) v);
	}

	public void writeNumber (int n) throws IOException {
		byte size = getIntSizeInBytes(n);
		out.writeByte(size);

		if (size > 0) {
			switch (size) {
				case 1:
					out.writeByte((byte) n);
					break;

				case 2:
					out.writeShort((short) n);
					break;

				default:
					out.writeInt(n);
			}
		}
	}

	private byte getIntSizeInBytes (int n) {
		if (n == 0) {
			return 0;
		}

		if (n > Short.MAX_VALUE || n < Short.MIN_VALUE) {
			return 4;
		}

		if (n > Byte.MAX_VALUE || n < Byte.MIN_VALUE) {
			return 2;
		}

		return 1;
	}

	private byte[] unboxBytes (Object value) {
		if (value instanceof byte[]) {
			return (byte[]) value;
		}

		Byte[] in = (Byte[]) value;
		byte[] out = new byte[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = in[i];
		}

		return out;
	}

	public void writeNumbers (int[] n) throws IOException {
		writeNumber(n.length);
		for (int i : n) {
			writeNumber(i);
		}
	}
}
