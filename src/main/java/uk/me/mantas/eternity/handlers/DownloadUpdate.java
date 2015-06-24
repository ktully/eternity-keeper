/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 Kim Mantas
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

	/**
	 * This class is a bit messy because it was retroactively modified to take into account
	 * updating the UI code as well as the Java code. What we want to do here is fire off one
	 * request to the server for the new JAR and keep track of its download progress. Then we want
	 * to signal that the JAR has finished downloading and then download the UI ZIP and keep track
	 * of its progress too.
	 */

	public static class UpdateDownloader implements Runnable {
		public long totalBytes = -1;
		public AtomicLong currentBytes = new AtomicLong(0L);
		public AtomicBoolean failed = new AtomicBoolean(false);
		public AtomicBoolean jarDownloaded = new AtomicBoolean(false);

		private final String jarName;
		private final CefQueryCallback callback;

		public UpdateDownloader (String jarName, CefQueryCallback callback) {
			this.jarName = jarName;
			this.callback = callback;
		}

		@Override
		public void run () {
			downloadJAR();
			jarDownloaded.set(true);

			if (!failed.get()) {
				downloadUI();
			}
		}

		private void downloadJAR () {
			File destination = new File(Environment.getInstance().getJarDirectory(), jarName);
			downloadLatest("download", destination);
		}

		private void downloadUI () {
			String uiName = uiNameFromJARName();
			File destination = new File(Environment.getInstance().getUiDirectory(), uiName);
			downloadLatest("ui", destination);
		}

		private String uiNameFromJARName () {
			return jarName.replace("jar", "zip");
		}

		private void downloadLatest (String querystr, File destination) {
			HttpClient client = HttpClients.createDefault();
			HttpGet request = new HttpGet("http://eternity.mantas.me.uk/latest.php?" + querystr);

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

				try (InputStream in = new BufferedInputStream(entity.getContent());
					OutputStream out =
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
			} catch (IOException e) {
				System.err.printf(
					"Error downloading update: %s%n"
					, e.getMessage());

				callback.failure(-1, "IO_EXCEPTION");
				failed.set(true);
			}
		}

		private void signalStarted () {
			// We only want to signal that we've started downloading once. The download progress
			// checker will take care of the rest.

			if (!jarDownloaded.get()) {
				callback.success("true");
			}
		}
	}
}
