package uk.me.mantas.eternity.tests.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.json.JSONObject;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.handlers.SaveSettings;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SaveSettingsTest extends TestHarness {
	@Test
	public void settingsSaved () {
		Environment environment = Environment.getInstance();
		SaveSettings cls = new SaveSettings();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		Optional<File> settingsDirectory = EKUtils.createTempDir(PREFIX);
		assertTrue(settingsDirectory.isPresent());

		File settingsFile = new File(settingsDirectory.get(), "settings.json");
		environment.setSettingsFile(settingsFile);

		JSONObject settings = Settings.getInstance().json;
		String request = "{\"a\":\"b\",\"c\":\"d\"}";
		cls.onQuery(mockBrowser, 0, request, false, mockCallback);

		assertEquals("b", settings.getString("a"));
		assertEquals("d", settings.getString("c"));
	}
}
