package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import uk.me.mantas.eternity.Settings;

import java.util.Iterator;

public class SaveSettings extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		// Consider running this in a separate thread if it takes too long.
		try {
			JSONObject settings = Settings.getInstance().json;
			JSONObject json = new JSONObject(request);
			Iterator<String> keys = json.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				settings.put(key, json.get(key));
			}

			Settings.getInstance().save();
		} catch (JSONException e) {
			System.err.printf(
				"Unable to read JSON-formatted settings data from client: %s%n"
				, e.getMessage());
		}

		callback.success("");
		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
	}
}
