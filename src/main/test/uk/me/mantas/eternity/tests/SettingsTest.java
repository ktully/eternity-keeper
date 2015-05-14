package uk.me.mantas.eternity.tests;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Settings;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

public class SettingsTest extends TestHarness {
	@Test
	public void createsNewSettingsFileIfNotExists () throws IOException {
		Optional<File> settingsLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(settingsLocation.isPresent());

		File settingsFile = new File(settingsLocation.get(), "settings.json");
		Environment.getInstance().setSettingsFile(settingsFile);
		assertFalse(settingsFile.exists());

		Settings.initialise();
		String contents = FileUtils.readFileToString(settingsFile);

		assertEquals("{}", contents);
	}

	private static final String SETTINGS_JSON =
		"{\"double\":3.14159,\"int\":42,\"string\":\"s\"}";

	@Test
	public void readCurrentSettingsFileIfExists () throws IOException {
		Optional<File> settingsLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(settingsLocation.isPresent());

		File settingsFile = new File(settingsLocation.get(), "settings.json");
		Environment.getInstance().setSettingsFile(settingsFile);
		assertTrue(settingsFile.createNewFile());

		FileUtils.writeStringToFile(settingsFile, SETTINGS_JSON, "UTF-8");
		Settings.initialise();

		JSONObject json = Settings.getInstance().json;
		assertEquals(3.14159, json.getDouble("double"), 1e-6);
		assertEquals(42, json.getInt("int"));
		assertEquals("s", json.getString("string"));
	}
}
