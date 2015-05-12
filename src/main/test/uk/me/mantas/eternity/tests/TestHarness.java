package uk.me.mantas.eternity.tests;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import uk.me.mantas.eternity.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public abstract class TestHarness {
	protected static String PREFIX = "EK-";

	@Before
	public void setup () {
		Environment.initialise();
	}

	@After
	public void cleanup () {
		File temp = new File(System.getProperty("java.io.tmpdir"));
		File[] files = temp.listFiles();

		if (files != null) {
			Arrays.stream(files)
				.filter((file) -> file.getName().startsWith(PREFIX))
				.forEach((file) -> {
					try {
						FileUtils.deleteDirectory(file);
					} catch (IOException e) {
						System.err.printf(
							"Unable to delete temporary directory '%s': %s%n"
							, file.getAbsoluteFile()
							, e.getMessage());
					}
				});
		}
	}
}
