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


package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static uk.me.mantas.eternity.environment.Variables.Key.*;

public class GetDefaultSaveLocation extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(GetDefaultSaveLocation.class);

	@Override
	public boolean onQuery (
		final CefBrowser browser
		, final long id
		, final String request
		, final boolean persistent
		, final CefQueryCallback callback) {

		final Environment environment = Environment.getInstance();
		final JSONObject settings = Settings.getInstance().json;
		String defaultSaveLocation = null;
		String defaultGameLocation = null;

		try {
			defaultSaveLocation = settings.getString("savesLocation");
		} catch (final JSONException ignored) {}

		try {
			defaultGameLocation = settings.getString("gameLocation");
		} catch (final JSONException ignored) {}

		if (defaultSaveLocation == null || defaultSaveLocation.length() < 1) {
			final Optional<String> userProfile = environment.variables().get(USERPROFILE);
			final Optional<String> xdgDataHome = environment.variables().get(XDG_DATA_HOME);
			final Optional<String> home = environment.variables().get(HOME);
			final String linuxSaves = "PillarsOfEternity/SavedGames";

			if (userProfile.isPresent()) {
				final Path defaultLocation =
					Paths.get(userProfile.get()).resolve("Saved Games\\Pillars of Eternity");

				if (defaultLocation.toFile().exists()) {
					defaultSaveLocation = defaultLocation.toString();
				}
			} else if (xdgDataHome.isPresent()) {
				final Path defaultLocation = Paths.get(xdgDataHome.get()).resolve(linuxSaves);
				if (defaultLocation.toFile().exists()) {
					defaultSaveLocation = defaultLocation.toString();
				}
			} else if (home.isPresent()) {
				final Path defaultLocation =
					Paths.get(home.get()).resolve(".local/share").resolve(linuxSaves);

				if (defaultLocation.toFile().exists()) {
					defaultSaveLocation = defaultLocation.toString();
				}
			}
		}

		if (defaultSaveLocation == null) {
			defaultSaveLocation = "";
		}

		if (defaultGameLocation == null || defaultGameLocation.length() < 1) {
			final Optional<String> systemDrive = environment.variables().get(SYSTEMDRIVE);
			final Optional<String> home = environment.variables().get(HOME);
			Optional<File> foundLocation = Optional.empty();

			if (systemDrive.isPresent()) {
				final Path root = Paths.get(systemDrive.get());
				foundLocation = searchLikelyLocations(root.toFile());
			} else if (home.isPresent()) {
				final Path defaultLocation =
					Paths.get(home.get())
						.resolve(".steam/steam/SteamApps/common/Pillars of Eternity");

				if (defaultLocation.toFile().exists()) {
					foundLocation = Optional.of(defaultLocation.toFile());
				}
			}

			if (foundLocation.isPresent()) {
				defaultGameLocation = foundLocation.get().getAbsolutePath();
			}
		}

		if (defaultGameLocation == null) {
			defaultGameLocation = "";
		} else {
			settings.put("gameLocation", defaultGameLocation);
		}

		callback.success(foundDefault(defaultSaveLocation, defaultGameLocation));
		return true;
	}

	private Optional<File> searchLikelyLocations (final File systemDrive) {
		final Environment environment = Environment.getInstance();
		for (final String possibleLocation : environment.config().possibleInstallationLocations()) {
			final File resolvedLocation = new File(systemDrive, possibleLocation);
			if (resolvedLocation.exists()) {
				return Optional.of(resolvedLocation);
			}
		}

		return Optional.empty();
	}

	private String foundDefault (final String savesLocation, final String gameLocation) {
		return new JSONStringer()
			.object()
				.key("savesLocation").value(savesLocation)
				.key("gameLocation").value(gameLocation)
			.endObject()
			.toString();
	}

	@Override
	public void onQueryCanceled (final CefBrowser browser, final long id) {
		// Not really sure what this means yet so log it for now.
		logger.error("Query #%d was cancelled.%n", id);
	}
}
