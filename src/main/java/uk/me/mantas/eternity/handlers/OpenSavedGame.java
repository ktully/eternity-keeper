package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.save.SavedGameOpener;

import java.io.File;

public class OpenSavedGame extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		if (!(new File(request).exists())) {
			notExists(callback);
			return true;
		}

		Environment.getInstance().getWorkers().execute(
			new SavedGameOpener(request, callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
		Environment.joinAllWorkers();
	}

	public static void notExists (CefQueryCallback callback) {
		String json = new JSONStringer()
			.object()
				.key("error").value("NOT_EXISTS")
			.endObject()
			.toString();

		callback.success(json);
	}

	public static void deserializationError (CefQueryCallback callback) {
		String json = new JSONStringer()
			.object()
				.key("error").value("DESERIALIZATION_ERR")
			.endObject()
			.toString();

		callback.success(json);
	}
}
