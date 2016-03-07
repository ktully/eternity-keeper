/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 the authors.
 *
 *  Eternity Keeper is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  Eternity Keeper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.environment.Environment;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadUpdate extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(DownloadUpdate.class);

	@Override
	public boolean onQuery (
		final CefBrowser browser
		, final long id
		, final String request
		, final boolean persistent
		, final CefQueryCallback callback) {

		final Environment environment = Environment.getInstance();
		final UpdateDownloader downloader = new UpdateDownloader(request, callback);
		environment.state().currentUpdateDownloader(downloader);
		environment.workers().execute(downloader);

		return true;
	}

	@Override
	public void onQueryCanceled (final CefBrowser browser, final long id) {
		logger.error("Query #%d cancelled.%n", id);
	}

	public static class UpdateDownloader implements Runnable {
		public long totalBytes = -1;
		public final AtomicLong currentBytes = new AtomicLong(0L);
		public final AtomicBoolean failed = new AtomicBoolean(false);

		private final String updateTimestamp;
		private final CefQueryCallback callback;

		public UpdateDownloader (final String updateTimestamp, final CefQueryCallback callback) {
			this.updateTimestamp = updateTimestamp;
			this.callback = callback;
		}

		@Override
		public void run () {
			final String updateZip = String.format("%s.zip", updateTimestamp);
			final File destination =
				new File(Environment.getInstance().directory().root(), updateZip);
			downloadLatest(destination);
		}

		private void downloadLatest (final File destination) {
			final String url = String.format(
				"http://eternity.mantas.me.uk/updates/?platform=%s&download"
				, Environment.detectPlatform());
			final HttpClient client = HttpClients.createDefault();
			final HttpGet request = new HttpGet(url);

			try {
				final HttpResponse response = client.execute(request);
				final Header[] contentLength = response.getHeaders("X-Content-Length");

				if (contentLength != null && contentLength.length > 0) {
					totalBytes = Long.parseLong(contentLength[0].getValue());
				}

				final HttpEntity entity = response.getEntity();
				if (entity == null) {
					callback.failure(-1, "HTTP_ERR");
					return;
				}

				try (final InputStream in = new BufferedInputStream(entity.getContent());
					final OutputStream out =
						new BufferedOutputStream(new FileOutputStream(destination))) {

					signalStarted();
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
			} catch (final IOException e) {
				logger.error("Error downloading update: %s%n", e.getMessage());
				callback.failure(-1, "IO_EXCEPTION");
				failed.set(true);
			}
		}

		private void signalStarted () {
			// We only want to signal that we've started downloading once. The download progress
			// checker will take care of the rest.
			callback.success("true");
		}
	}
}
