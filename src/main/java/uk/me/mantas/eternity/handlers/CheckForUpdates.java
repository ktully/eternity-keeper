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
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.environment.Configuration;
import uk.me.mantas.eternity.environment.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

public class CheckForUpdates extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(CheckForUpdates.class);

	@Override
	public boolean onQuery (
		final CefBrowser browser
		, final long id
		, final String request
		, final boolean persistent
		, final CefQueryCallback callback) {

		Environment.getInstance().workers().execute(new UpdateChecker(callback));
		return true;
	}

	@Override
	public void onQueryCanceled (final CefBrowser browser, final long id) {
		logger.error("Query #%d cancelled.%n", id);
	}

	public static class UpdateChecker implements Runnable {
		private final CefQueryCallback callback;

		public UpdateChecker (final CefQueryCallback callback) {
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

		private void update (final String update) {
			callback.success(
				new JSONStringer()
					.object()
						.key("available").value(true)
						.key("timestamp").value(update)
					.endObject()
					.toString());
		}

		private void legacy () {
			callback.success(
				new JSONStringer()
					.object()
						.key("legacy").value(true)
					.endObject()
					.toString());
		}

		private long getJarTimestamp (final File jarDirectory) {
			final File[] jars = jarDirectory.listFiles();
			if (jars == null) {
				logger.error("Jar directory '%s' was empty.%n", jarDirectory.getAbsolutePath());
				return 0L;
			}

			final Optional<File> timestampedJar =
				Arrays.stream(jars)
					// Matches a filename with 14 digits only.
					.filter(jar -> jar.getName().matches("\\d{14}\\.jar"))
					.findFirst();

			if (!timestampedJar.isPresent()) {
				logger.error("No timestamped jar in '%s'.&n", jarDirectory.getAbsolutePath());
				return 0L;
			}

			final String timestamp = EKUtils.removeExtension(timestampedJar.get().getName());
			try {
				return Long.parseLong(timestamp);
			} catch (final NumberFormatException e) {
				logger.error("Unable to convert timestamp '%s' to long.%n", timestamp);
			}

			return 0L;
		}

		private Optional<String> isUpdate (final String latest) {
			final File jarDirectory = Environment.getInstance().directory().jar();
			if (latest.equals("false") || !jarDirectory.exists()) {
				return Optional.empty();
			}

			final long currentTimestamp = getJarTimestamp(jarDirectory);

			try {
				if (Long.parseLong(latest) > currentTimestamp) {
					return Optional.of(latest);
				}
			} catch (final NumberFormatException e) {
				return Optional.empty();
			}

			return Optional.empty();
		}

		private boolean isLegacyCode () {
			final Environment environment = Environment.getInstance();

			if (environment.isWindows()) {
				final Optional<Long> exeSize = environment.detectExeSize();
				if (exeSize.isPresent()) {
					return exeSize.get() == Configuration.LEGACY_EXE_SIZE;
				} else {
					logger.error(
						"CEF reported this is a Windows system but eternity.exe was not found.%n");
				}
			}

			return false;
		}

		@Override
		public void run () {
			final String url = String.format(
				"http://eternity.mantas.me.uk/updates/?platform=%s"
				, Environment.detectPlatform());

			try {
				final Content response = Request.Get(url).execute().returnContent();
				final Optional<String> updateAvailable =
					isUpdate(response.asString(Charset.forName("UTF-8")).trim());

				if (updateAvailable.isPresent()) {
					if (isLegacyCode()) {
						legacy();
					} else {
						update(updateAvailable.get());
					}
				} else {
					noUpdate();
				}
			} catch (final IOException e) {
				callback.failure(-1, "HTTP_ERR");
			}
		}
	}
}
