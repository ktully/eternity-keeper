package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.save.SaveGameExtractor;

import static uk.me.mantas.eternity.handlers.ListSavedGames.SaveInfoLister;

public class CheckExtractionProgress extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment environment = Environment.getInstance();
		SaveInfoLister lister = environment.getCurrentSaveLister();

		if (lister == null) {
			callback.success(response(0d));
			return true;
		}

		SaveGameExtractor extractor = lister.extractor;
		if (extractor == null) {
			callback.success(response(0d));
			return true;
		}

		int total = extractor.totalFiles.get();
		int count = extractor.currentCount.get();
		double percentage = ((double) count / (double) total) * 100;

		callback.success(response(percentage));
		return true;
	}

	private String response (double val) {
		return new JSONStringer()
			.object()
				.key("update").value(val)
			.endObject()
			.toString();
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
	}
}
