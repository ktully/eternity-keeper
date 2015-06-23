/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 Kim Mantas
 *
 *  Eternity Keeper is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  Eternity Keeper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
