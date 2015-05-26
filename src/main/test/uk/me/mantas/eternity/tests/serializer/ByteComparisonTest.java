package uk.me.mantas.eternity.tests.serializer;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;

public class ByteComparisonTest {
	@Test
	public void compare () throws IOException {
		File root = new File(System.getProperty("serializer.root"));
		for (int i = 0; i < 17; i++) {
			System.out.printf("%d%n", i);
			Path outPath = Paths.get("out", String.format("%d", i));
			Path origPath = Paths.get("orig", String.format("%d", i));

			byte[] actual = FileUtils.readFileToByteArray(
				new File(root, outPath.toString()));

			byte[] expected = FileUtils.readFileToByteArray(
				new File(root, origPath.toString()));

			assertArrayEquals(expected, actual);
		}
	}

	@Test
	public void split () throws IOException {
		File dest = new File(System.getProperty("serializer.out"));
		File in = new File(System.getProperty("serializer.in"));
		byte[] contents = FileUtils.readFileToByteArray(in);

		int chunks = 0;
		int start = 0;
		int end;
		byte[] buf = new byte[4];
		int pos = 0;

		for (int i = 0; i < contents.length; i++) {
			if (i == contents.length - 1) {
				end = i;
				byte[] chunk = new byte[(end - start) + 1];
				System.arraycopy(contents, start, chunk, 0, (end - start) + 1);
				FileUtils.writeByteArrayToFile(
					new File(dest, String.format("%d", chunks)), chunk);

				break;
			}

			buf[pos] = contents[i];
			pos++;

			if (pos > 3) {
				if (new String(buf).equals("Root")) {
					buf = new byte[4];
					pos = 0;

					end = i - 7;
					if (end <= 0) {
						continue;
					}

					byte[] chunk = new byte[end - start];
					System.arraycopy(
						contents
						, start
						, chunk
						, 0
						, end - start);

					start = end;
					FileUtils.writeByteArrayToFile(
						new File(dest, String.format("%d", chunks)), chunk);

					chunks++;
				} else {
					buf[0] = buf[1];
					buf[1] = buf[2];
					buf[2] = buf[3];
					pos = 3;
				}
			}
		}
	}
}
