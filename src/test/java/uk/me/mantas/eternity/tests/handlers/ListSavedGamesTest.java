/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2015 the authors.
 * <p>
 * Eternity Keeper is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * Eternity Keeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.me.mantas.eternity.tests.handlers;

import org.apache.commons.io.FileUtils;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.json.JSONObject;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.handlers.ListSavedGames;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListSavedGamesTest extends TestHarness {
	@Test
	public void onQueryTestSettingsUpdated () throws IOException {
		final Environment mockEnvironment = mockEnvironment();
		final ExecutorService mockWorkers = mock(ExecutorService.class);
		final File workingDirectory = EKUtils.createTempDir(PREFIX).get();
		final File settingsFile = new File(workingDirectory, "settings.json");
		FileUtils.writeStringToFile(settingsFile, "{}");

		when(mockEnvironment.directory().settingsFile()).thenReturn(settingsFile);
		when(mockEnvironment.workers()).thenReturn(mockWorkers);

		final Settings mockSettings = mockSettings();
		final CefBrowser mockBrowser = mock(CefBrowser.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ListSavedGames listSavedGames = new ListSavedGames();

		mockSettings.json = new JSONObject();
		listSavedGames.onQuery(mockBrowser, 0, "testLocation", false, mockCallback);
		assertEquals("testLocation", mockSettings.json.getString("savesLocation"));
	}
}
