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

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONStringer;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

public class CheckForUpdates extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(CheckForUpdates.class);

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
		logger.error("Query #%d cancelled.%n", id);
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
			if (Long.parseLong(latest) > currentTimestamp) {
				return Optional.of(latest + ".jar");
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
					isUpdate(
						response.asString(Charset.forName("UTF-8")).trim());

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
