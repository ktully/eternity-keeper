package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Settings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static uk.me.mantas.eternity.Environment.EnvKey;

public class GetDefaultSaveLocation extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

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
			Optional<String> userProfile =
				Environment.getInstance().getEnvVar(EnvKey.USERPROFILE);

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
			File steamLocation = new File("C:\\Program Files (x86)\\Steam\\"
				+ "SteamApps\\common\\Pillars of Eternity");

			if (steamLocation.exists()) {
				defaultGameLocation = steamLocation.getAbsolutePath();
			}
		}

		if (defaultGameLocation == null) {
			defaultGameLocation = "";
		}

		callback.success(
			foundDefault(defaultSaveLocation, defaultGameLocation));

		return true;
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
		System.err.printf("Query #%d was cancelled.%n", id);
	}
}
