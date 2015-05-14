package uk.me.mantas.eternity.tests.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Environment.EnvKey;
import uk.me.mantas.eternity.handlers.GetDefaultSaveLocation;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GetDefaultSaveLocationTest extends TestHarness {
	protected String PREFIX = "EK-GDSL-";

	private static final String NO_DEFAULT = "{\"error\":\"NO_DEFAULT\"}";

	@Test
	public void onQueryNoUserProfileTest () {
		Environment environment = Environment.getInstance();
		GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		// No USERPROFILE environment variable.
		environment.setEnvVar(EnvKey.USERPROFILE, null);
		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(NO_DEFAULT);
	}

	@Test
	public void onQueryNoPillarsSavesTest () {
		Environment environment = Environment.getInstance();
		GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		Optional<File> saveLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(saveLocation.isPresent());

		environment.setEnvVar(
			EnvKey.USERPROFILE
			, saveLocation.get().getAbsolutePath());

		// USERPROFILE environment variable is set but no Pillars directory.
		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(NO_DEFAULT);
	}

	@Test
	public void onQueryFoundSaves () {
		Environment environment = Environment.getInstance();
		GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		Optional<File> saveLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(saveLocation.isPresent());

		environment.setEnvVar(
			EnvKey.USERPROFILE
			, saveLocation.get().getAbsolutePath());

		File pillarsSaves = saveLocation.get().toPath()
			.resolve("Saved Games\\Pillars of Eternity").toFile();

		assertTrue(pillarsSaves.mkdirs());

		// We actually have a default save directory.
		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(
			String.format(
				"{\"location\":\"%s\"}"
				, pillarsSaves.getAbsolutePath().replace("\\", "\\\\")));
	}
}
