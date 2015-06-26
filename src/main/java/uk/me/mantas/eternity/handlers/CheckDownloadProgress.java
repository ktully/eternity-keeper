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

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Logger;

import static uk.me.mantas.eternity.handlers.DownloadUpdate.UpdateDownloader;

public class CheckDownloadProgress extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(CheckDownloadProgress.class);

	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		UpdateDownloader downloader = Environment.getInstance().getCurrentUpdateDownloader();

		if (downloader.failed.get()) {
			callback.failure(-1, "true");
		} else {
			long currentBytes = downloader.currentBytes.get();
			double percentage = (double) currentBytes / (double) downloader.totalBytes * 100;

			if (percentage > 100) {
				percentage = 100;
			}

			respond(callback, percentage, downloader.jarDownloaded.get());
		}

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		logger.error("Query #%d cancelled.%n", id);
	}

	private void respond (CefQueryCallback callback, double percentage, boolean jarDownloaded) {
		callback.success(
			new JSONStringer()
				.object()
					.key("jarDownloaded").value(jarDownloaded)
					.key("percentage").value(percentage)
				.endObject()
				.toString());
	}
}
