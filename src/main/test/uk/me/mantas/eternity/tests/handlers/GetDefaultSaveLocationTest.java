package uk.me.mantas.eternity.tests.handlers;

import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Environment.EnvKey;
import uk.me.mantas.eternity.handlers.GetDefaultSaveLocation;
import uk.me.mantas.eternity.tests.TestHarness;
import uk.me.mantas.eternity.tests.mocks.MockCefBrowser;
import uk.me.mantas.eternity.tests.mocks.MockCefQueryCallback;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetDefaultSaveLocationTest extends TestHarness {
	protected String PREFIX = "EK-GDSL-";

	@Test
	public void onQueryTest () {
		Environment environment = Environment.getInstance();
		MockCefBrowser mockBrowser = new MockCefBrowser();
		GetDefaultSaveLocation cls = new GetDefaultSaveLocation();

		Consumer<String> noDefault = (String result) ->
			assertEquals("{\"error\":\"NO_DEFAULT\"}", result);

		CefQueryCallback noDefaultCallback =
			new MockCefQueryCallback(noDefault);

		// No USERPROFILE environment variable.
		environment.setEnvVar(EnvKey.USERPROFILE, null);
		cls.onQuery(mockBrowser, 0, "", false, noDefaultCallback);

		Optional<File> saveLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(saveLocation.isPresent());

		environment.setEnvVar(
			EnvKey.USERPROFILE
			, saveLocation.get().getAbsolutePath());

		// USERPROFILE environment variable is set but no Pillars directory.
		cls.onQuery(mockBrowser, 0, "", false, noDefaultCallback);

		File pillarsSaves = saveLocation.get().toPath()
			.resolve("Saved Games\\Pillars of Eternity").toFile();

		assertTrue(pillarsSaves.mkdirs());

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
