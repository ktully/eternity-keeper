package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.save.SaveGameExtractor;

import static uk.me.mantas.eternity.save.SaveGameExtractor.NoSavesFoundException;

public class ListSavedGames extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		// Spawn a separate thread to handle all the file stuff so we don't
		// lock up the UI.
		Environment.getInstance().getWorkers().execute(
			new SaveInfoLister(request, callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
	}

	public class SaveInfoLister implements Runnable {
		private String savesLocation;
		private CefQueryCallback callback;

		public SaveInfoLister (String savesLocation, CefQueryCallback callback) {
			this.savesLocation = savesLocation;
			this.callback = callback;
		}

		@Override
		public void run () {
			Environment.getInstance().emptyWorkingDirectory();

			try {
				SaveGameExtractor extractor = new SaveGameExtractor(
					savesLocation
					, Environment.getInstance().getWorkingDirectory());
			} catch (NoSavesFoundException e) {
				notFound(callback);
			}
		}
	}

	private void notFound (CefQueryCallback callback) {
		String json = new JSONStringer()
			.object()
				.key("error")
				.value("NO_RESULTS")
			.endObject()
			.toString();

		callback.success(json);
	}
}
