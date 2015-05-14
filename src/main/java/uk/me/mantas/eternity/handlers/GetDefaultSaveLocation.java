package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;

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
		, boolean persistant
		, CefQueryCallback callback) {

		Optional<String> userProfile =
			Environment.getInstance().getEnvVar(EnvKey.USERPROFILE);

		if (!userProfile.isPresent()) {
			callback.success(noDefault());
			return true;
		}

		Path defaultLocation = Paths.get(userProfile.get())
			.resolve("Saved Games\\Pillars of Eternity");

		if (!defaultLocation.toFile().exists()) {
			callback.success(noDefault());
			return true;
		}

		callback.success(foundDefault(defaultLocation.toString()));
		return true;
	}

	private String foundDefault (String defaultLocation) {
		return new JSONStringer()
			.object()
				.key("location").value(defaultLocation)
			.endObject()
			.toString();
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		// Not really sure what this means yet so log it for now.
		System.err.printf("Query #%d was cancelled.%n", id);
	}

	private String noDefault () {
		return new JSONStringer()
			.object()
				.key("error").value("NO_DEFAULT")
			.endObject()
			.toString();
	}
}
