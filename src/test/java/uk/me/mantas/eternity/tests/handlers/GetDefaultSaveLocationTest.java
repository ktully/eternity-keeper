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
import org.junit.Before;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Environment.EnvKey;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.handlers.GetDefaultSaveLocation;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GetDefaultSaveLocationTest extends TestHarness {
	private static final String NO_DEFAULT =
		"{\"savesLocation\":\"\",\"gameLocation\":\"\"}";

	@Before
	public void setup () throws NoSuchFieldException, IllegalAccessException {
		super.setup();
		Settings.getInstance().json = new JSONObject();
	}

	@Test
	public void onQueryNoUserProfileTest () {
		Environment environment = Environment.getInstance();
		GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		// No USERPROFILE environment variable.
		environment.setEnvVar(EnvKey.USERPROFILE, null);
		environment.setEnvVar(EnvKey.SYSTEMDRIVE, null);
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
				"{\"savesLocation\":\"%s\",\"gameLocation\":\"\"}"
				, pillarsSaves.getAbsolutePath().replace("\\", "\\\\")));
	}

	@Test
	public void onQueryFoundGameInstallation () {
		Environment environment = Environment.getInstance();
		GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		CefBrowser mockBrowser = mock(CefBrowser.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		Optional<File> gameLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(gameLocation.isPresent());

		environment.setEnvVar(
			EnvKey.SYSTEMDRIVE
			, gameLocation.get().getAbsolutePath());

		environment.possibleInstallationLocations = new ArrayList<>();
		environment.possibleInstallationLocations.add("first");
		environment.possibleInstallationLocations.add("second");

		File firstLocation = new File(gameLocation.get(), "first");
		File secondLocation = new File(gameLocation.get(), "second");

		assertTrue(firstLocation.mkdirs());
		assertTrue(secondLocation.mkdirs());

		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(
			String.format(
				"{\"savesLocation\":\"\",\"gameLocation\":\"%s\"}"
				, firstLocation.getAbsolutePath().replace("\\", "\\\\")));
	}
}
