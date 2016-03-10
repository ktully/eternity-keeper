/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 the authors.
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
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.handlers.GetDefaultSaveLocation;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.me.mantas.eternity.environment.Variables.Key.*;

public class GetDefaultSaveLocationTest extends TestHarness {
	private static final String NO_DEFAULT = "{\"savesLocation\":\"\",\"gameLocation\":\"\"}";
	private static final String JSON_SKELETON =
		"{\"savesLocation\":\"%s\",\"gameLocation\":\"%s\"}";

	@Before
	public void setup () {
		super.setup();
		Settings.getInstance().json = new JSONObject();
	}

	@Test
	public void onQueryNoUserProfileTest () {
		final Environment environment = Environment.getInstance();
		final GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		final CefBrowser mockBrowser = mock(CefBrowser.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		// No USERPROFILE environment variable.
		environment.variables().set(USERPROFILE, null);
		environment.variables().set(SYSTEMDRIVE, null);
		environment.variables().set(XDG_DATA_HOME, null);
		environment.variables().set(HOME, null);
		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(NO_DEFAULT);
	}

	@Test
	public void onQueryNoPillarsSavesTest () {
		final Environment environment = Environment.getInstance();
		final GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		final CefBrowser mockBrowser = mock(CefBrowser.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		final Optional<File> saveLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(saveLocation.isPresent());

		environment.variables().set(USERPROFILE, saveLocation.get().getAbsolutePath());
		environment.variables().set(SYSTEMDRIVE, "404");
		environment.variables().set(XDG_DATA_HOME, null);
		environment.variables().set(HOME, null);

		// USERPROFILE environment variable is set but no Pillars directory.
		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(NO_DEFAULT);
	}

	@Test
	public void onQueryFoundSaves () {
		final Environment environment = Environment.getInstance();
		final GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		final CefBrowser mockBrowser = mock(CefBrowser.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		final Optional<File> saveLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(saveLocation.isPresent());

		environment.variables().set(USERPROFILE, saveLocation.get().getAbsolutePath());
		environment.variables().set(SYSTEMDRIVE, "404");
		environment.variables().set(XDG_DATA_HOME, null);
		environment.variables().set(HOME, null);

		final File pillarsSaves =
			saveLocation.get().toPath().resolve("Saved Games\\Pillars of Eternity").toFile();

		assertTrue(pillarsSaves.mkdirs());

		// We actually have a default save directory.
		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(
			String.format(JSON_SKELETON, pillarsSaves.getAbsolutePath().replace("\\", "\\\\"), ""));
	}

	@Test
	public void onQueryFoundGameInstallation () {
		final Environment environment = Environment.getInstance();
		final GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		final CefBrowser mockBrowser = mock(CefBrowser.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		final Optional<File> gameLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(gameLocation.isPresent());

		environment.variables().set(USERPROFILE, "404");
		environment.variables().set(SYSTEMDRIVE, gameLocation.get().getAbsolutePath());
		environment.variables().set(XDG_DATA_HOME, null);
		environment.variables().set(HOME, null);

		environment.config().possibleInstallationLocations(new ArrayList<>());
		environment.config().possibleInstallationLocations().add("first");
		environment.config().possibleInstallationLocations().add("second");

		final File firstLocation = new File(gameLocation.get(), "first");
		final File secondLocation = new File(gameLocation.get(), "second");

		assertTrue(firstLocation.mkdirs());
		assertTrue(secondLocation.mkdirs());

		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(
			String.format(
				JSON_SKELETON
				, ""
				, firstLocation.getAbsolutePath().replace("\\", "\\\\")));
	}

	@Test
	public void findsLinuxSaveDirectory () {
		final Environment environment = Environment.getInstance();
		final GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		final CefBrowser mockBrowser = mock(CefBrowser.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		final Optional<File> saveLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(saveLocation.isPresent());

		environment.variables().set(USERPROFILE, null);
		environment.variables().set(SYSTEMDRIVE, null);
		environment.variables().set(XDG_DATA_HOME, saveLocation.get().getAbsolutePath());

		final File pillarsSaves =
			saveLocation.get().toPath().resolve("PillarsOfEternity/SavedGames").toFile();

		assertTrue(pillarsSaves.mkdirs());

		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(
			String.format(JSON_SKELETON, pillarsSaves.getAbsolutePath().replace("\\", "\\\\"), ""));

		final File localPillarsSaves =
			saveLocation.get().toPath()
				.resolve(".local/share/PillarsOfEternity/SavedGames")
				.toFile();

		assertTrue(localPillarsSaves.mkdirs());

		environment.variables().set(XDG_DATA_HOME, null);
		environment.variables().set(HOME, saveLocation.get().getAbsolutePath());

		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(
			String.format(
				JSON_SKELETON
				, localPillarsSaves.getAbsolutePath().replace("\\", "\\\\")
				, ""));
	}

	@Test
	public void findsLinuxGameDirectory () {
		final Environment environment = Environment.getInstance();
		final GetDefaultSaveLocation cls = new GetDefaultSaveLocation();
		final CefBrowser mockBrowser = mock(CefBrowser.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		final Optional<File> gameLocation = EKUtils.createTempDir(PREFIX);
		assertTrue(gameLocation.isPresent());

		environment.variables().set(USERPROFILE, null);
		environment.variables().set(SYSTEMDRIVE, null);
		environment.variables().set(XDG_DATA_HOME, null);
		environment.variables().set(HOME, gameLocation.get().getAbsolutePath());

		final File pillarsInstall =
			gameLocation.get().toPath()
				.resolve(".steam/steam/SteamApps/common/Pillars of Eternity").toFile();

		assertTrue(pillarsInstall.mkdirs());

		cls.onQuery(mockBrowser, 0, "", false, mockCallback);
		verify(mockCallback).success(
			String.format(
				JSON_SKELETON
				, ""
				, pillarsInstall.getAbsolutePath().replace("\\", "\\\\")));
	}
}
