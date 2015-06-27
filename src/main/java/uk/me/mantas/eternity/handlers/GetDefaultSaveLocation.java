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


package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static uk.me.mantas.eternity.Environment.EnvKey;

public class GetDefaultSaveLocation extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(GetDefaultSaveLocation.class);

	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment environment = Environment.getInstance();
		JSONObject settings = Settings.getInstance().json;
		String defaultSaveLocation = null;
		String defaultGameLocation = null;

		try {
			defaultSaveLocation = settings.getString("savesLocation");
		} catch (JSONException ignored) {}

		try {
			defaultGameLocation = settings.getString("gameLocation");
		} catch (JSONException ignored) {}

		if (defaultSaveLocation == null || defaultSaveLocation.length() < 1) {
			Optional<String> userProfile = environment.getEnvVar(EnvKey.USERPROFILE);

			if (userProfile.isPresent()) {
				Path defaultLocation = Paths.get(userProfile.get())
					.resolve("Saved Games\\Pillars of Eternity");

				if (defaultLocation.toFile().exists()) {
					defaultSaveLocation = defaultLocation.toString();
				}
			}
		}

		if (defaultSaveLocation == null) {
			defaultSaveLocation = "";
		}

		if (defaultGameLocation == null || defaultGameLocation.length() < 1) {
			Optional<File> foundLocation = Optional.empty();
			Optional<String> systemDrive = environment.getEnvVar(EnvKey.SYSTEMDRIVE);

			if (systemDrive.isPresent()) {
				Path root = Paths.get(systemDrive.get());
				foundLocation = searchLikelyLocations(root.toFile());
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
		for (String possibleLocation : environment.possibleInstallationLocations) {
			File resolvedLocation = new File(systemDrive, possibleLocation);
			if (resolvedLocation.exists()) {
				return Optional.of(resolvedLocation);
			}
		}

		return Optional.empty();
	}

	private String foundDefault (String savesLocation, String gameLocation) {
		return new JSONStringer()
			.object()
				.key("savesLocation").value(savesLocation)
				.key("gameLocation").value(gameLocation)
			.endObject()
			.toString();
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		// Not really sure what this means yet so log it for now.
		logger.error("Query #%d was cancelled.%n", id);
	}
}
