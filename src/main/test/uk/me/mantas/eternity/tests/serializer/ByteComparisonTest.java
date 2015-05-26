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
		File root = new File("C:\\Users\\Kim\\Documents\\SharpSerializer");
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
}
