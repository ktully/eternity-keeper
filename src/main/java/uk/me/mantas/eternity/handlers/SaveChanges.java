package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.save.ChangesSaver;

public class SaveChanges extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment.getInstance().getWorkers().execute(
			new ChangesSaver(request, callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
		Environment.joinAllWorkers();
	}

	public static String jsonError () {
		return new JSONStringer()
			.object()
				.key("error").value("Error parsing JSON request.")
			.endObject()
			.toString();
	}

	public static String ioError () {
		return new JSONStringer()
			.object()
				.key("error").value("Unable to write new save file.")
			.endObject()
			.toString();
	}
}
