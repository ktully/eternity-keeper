package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;

import static uk.me.mantas.eternity.handlers.DownloadUpdate.UpdateDownloader;

public class CheckDownloadProgress extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		UpdateDownloader downloader =
			Environment.getInstance().getCurrentUpdateDownloader();

		if (downloader.failed.get()) {
			callback.failure(-1, "true");
		} else {
			long currentBytes = downloader.currentBytes.get();
			double percentage =
				(double) currentBytes / (double) downloader.totalBytes * 100;

			if (percentage > 100) {
				percentage = 100;
			}

			callback.success(
				new JSONStringer()
					.object()
						.key("percentage").value(percentage)
					.endObject()
					.toString());
		}

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d cancelled.%n", id);
	}
}
