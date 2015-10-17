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


package uk.me.mantas.eternity.tests.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.json.JSONObject;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;
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
		environment.directory().settingsFile(settingsFile);

		JSONObject settings = Settings.getInstance().json;
		String request = "{\"a\":\"b\",\"c\":\"d\"}";
		cls.onQuery(mockBrowser, 0, request, false, mockCallback);

		assertEquals("b", settings.getString("a"));
		assertEquals("d", settings.getString("c"));
	}
}
