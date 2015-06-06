package uk.me.mantas.eternity.handlers;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import uk.me.mantas.eternity.Environment;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadUpdate extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment environment = Environment.getInstance();
		UpdateDownloader downloader = new UpdateDownloader(request, callback);
		environment.setCurrentUpdateDownloader(downloader);
		environment.getWorkers().execute(downloader);

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d cancelled.%n", id);
	}

	public static class UpdateDownloader implements Runnable {
		public long totalBytes = -1;
		public AtomicLong currentBytes = null;
		public AtomicBoolean failed = new AtomicBoolean(false);

		private final String jarName;
		private final CefQueryCallback callback;

		public UpdateDownloader (String jarName, CefQueryCallback callback) {
			this.jarName = jarName;
			this.callback = callback;
		}

		@Override
		public void run () {
			HttpClient client = HttpClients.createDefault();
			HttpGet request =
				new HttpGet("http://eternity.mantas.me.uk/latest.php?download");

			try {
				HttpResponse response = client.execute(request);
				Header[] contentLength =
					response.getHeaders("X-Content-Length");

				if (contentLength != null && contentLength.length > 0) {
					totalBytes = Long.parseLong(contentLength[0].getValue());
				}

				HttpEntity entity = response.getEntity();

				if (entity == null) {
					callback.failure(-1, "HTTP_ERR");
					return;
				}

				File updatedJar =
					new File(
						Environment.getInstance().getJarDirectory()
						, jarName);

				try (InputStream in =
						new BufferedInputStream(entity.getContent());
					OutputStream out =
						new BufferedOutputStream(
							new FileOutputStream(updatedJar))) {

					callback.success("true");
					int byteRead;
					long localCurrent = 0;

					while ((byteRead = in.read()) != -1) {
						localCurrent++;
						out.write(byteRead);

						if (localCurrent % 4096 == 0) {
							currentBytes.set(localCurrent);
						}
					}

					currentBytes.set(totalBytes);
				}

			} catch (IOException e) {
				System.err.printf(
					"Error downloading update: %s%n"
					, e.getMessage());

				callback.failure(-1, "IO_EXCEPTION");
				failed.set(true);
			}
		}
	}
}
