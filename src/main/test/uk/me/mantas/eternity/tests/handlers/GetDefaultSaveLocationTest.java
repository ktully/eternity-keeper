package uk.me.mantas.eternity.tests.handlers;

import org.apache.commons.io.FileUtils;
import org.cef.callback.CefQueryCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Harness;
import uk.me.mantas.eternity.Harness.EnvKey;
import uk.me.mantas.eternity.tests.mocks.MockCefBrowser;
import uk.me.mantas.eternity.tests.mocks.MockCefQueryCallback;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class GetDefaultSaveLocationTest {
	private static final String PREFIX = "EK-GDSL-";

	@Before
	public void setup () {
		Harness.initialise();
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

	@Test
	public void onQueryTest () {
		Harness harness = Harness.getInstance();
		MockCefBrowser mockBrowser = new MockCefBrowser();
		GetDefaultSaveLocation cls = new GetDefaultSaveLocation();

		Consumer<String> noDefault = (String result) ->
			assertEquals("{\"error\":\"NO_DEFAULT\"}", result);

		CefQueryCallback noDefaultCallback =
			new MockCefQueryCallback(noDefault);

		// No USERPROFILE environment variable.
		harness.setEnvVar(EnvKey.USERPROFILE, null);
		cls.onQuery(mockBrowser, 0, "", false, noDefaultCallback);

		Optional<File> saveLocation = EKUtils.createTempDir(PREFIX);
		assertEquals(true, saveLocation.isPresent());

		harness.setEnvVar(
			EnvKey.USERPROFILE
			, saveLocation.get().getAbsolutePath());

		// USERPROFILE environment variable is set but no Pillars directory.
		cls.onQuery(mockBrowser, 0, "", false, noDefaultCallback);

		File pillarsSaves = saveLocation.get().toPath()
			.resolve("Saved Games\\Pillars of Eternity").toFile();

		assertEquals(true, pillarsSaves.mkdirs());

		Consumer<String> hasDefault = (String result) ->
			assertEquals(
				String.format(
					"{\"location\":\"%s\"}"
					, pillarsSaves.getAbsoluteFile().toString()
						.replace("\\", "\\\\"))
				, result);

		CefQueryCallback hasDefaultCallback =
			new MockCefQueryCallback(hasDefault);

		// We actually have a default save directory.
		cls.onQuery(mockBrowser, 0, "", false, hasDefaultCallback);
	}
}
