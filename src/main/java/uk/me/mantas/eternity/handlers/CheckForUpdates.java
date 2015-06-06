package uk.me.mantas.eternity.handlers;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

public class CheckForUpdates extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment.getInstance().getWorkers().execute(
			new UpdateChecker(callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d cancelled.%n", id);
	}

	private class UpdateChecker implements Runnable {
		private final CefQueryCallback callback;

		public UpdateChecker (CefQueryCallback callback) {
			this.callback = callback;
		}

		private void noUpdate () {
			callback.success(
				new JSONStringer()
					.object()
						.key("available").value(false)
					.endObject()
					.toString());
		}

		private void update (String update) {
			callback.success(
				new JSONStringer()
					.object()
					.key("available").value(true)
					.key("jar").value(update)
					.endObject()
					.toString());
		}

		private Optional<String> isUpdate (String latest) {
			File jarDirectory = Environment.getInstance().getJarDirectory();
			if (!jarDirectory.exists()) {
				return Optional.empty();
			}

			File[] jars = jarDirectory.listFiles();
			if (jars == null || jars.length < 1) {
				return Optional.empty();
			}

			long currentTimestamp = EKUtils.getTimestampOfLatestJar(jars);
			if (Long.parseLong(EKUtils.removeExtension(latest)) >
				currentTimestamp) {

				return Optional.of(latest);
			}

			return Optional.empty();
		}

		@Override
		public void run () {
			try {
				Content response =
					Request.Get("http://eternity.mantas.me.uk/latest.php")
						.execute().returnContent();

				Optional<String> updateAvailable =
					isUpdate(response.asString(Charset.forName("UTF-8")));

				if (updateAvailable.isPresent()) {
					update(updateAvailable.get());
				} else {
					noUpdate();
				}
			} catch (IOException e) {
				callback.failure(-1, "HTTP_ERR");
			}
		}
	}
}
