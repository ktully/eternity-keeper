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

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.save.CharacterExporter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Vector;

import static org.cef.handler.CefDialogHandler.FileDialogMode;

public class ExportCharacter extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(ExportCharacter.class);

	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment.getInstance().workers().execute(
			new FileDialog(browser, request, callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		logger.error("Query #%d cancelled.%n", id);
	}

	private class FileDialog implements Runnable {
		private final CefBrowser browser;
		private final String request;
		private final CefQueryCallback callback;

		public FileDialog (
			CefBrowser browser
			, String request
			, CefQueryCallback callback) {

			this.browser = browser;
			this.request = request;
			this.callback = callback;
		}

		@Override
		public void run () {
			browser.runFileDialog(
				FileDialogMode.FILE_DIALOG_SAVE
				, "Save Character"
				, ""
				, new Vector<String>(){{add(".chr");}}
				, 0
				, new FileCallback(request, callback));
		}
	}

	private class FileCallback implements CefRunFileDialogCallback {
		private final String request;
		private final CefQueryCallback callback;

		public FileCallback (String request, CefQueryCallback callback) {
			this.request = request;
			this.callback = callback;
		}

		@Override
		public void onFileDialogDismissed (
			final int selectedAcceptFilter
			, final Vector<String> filenames) {

			if (filenames.size() < 1 || filenames.get(0).length() < 1) {
				callback.failure(-1, "NO_SAVENAME");
				return;
			}

			try {
				final JSONObject json = new JSONObject(request);
				final String guid = json.getString("GUID");
				final String savePath = json.getString("absolutePath");

				final CharacterExporter exporter = new CharacterExporter(
					savePath
					, guid
					, addChrExtension(filenames.get(0)));

				boolean exportedSuccessfully = exporter.export();

				if (exportedSuccessfully) {
					callback.success("true");
				} else {
					callback.failure(-1, "EXPORT_ERR");
				}
			} catch (JSONException e) {
				logger.error("Error parsing JSON request: %s%n", request);
				callback.failure(-1, "BAD_REQUEST");
			} catch (FileNotFoundException e) {
				logger.error("Unable to find file : %s%n", e.getMessage());
				callback.failure(-1, "FILE_NOT_FOUND");
			} catch (IOException e) {
				logger.error("Filesystem error: %s%n", e.getMessage());
				callback.failure(-1, "FILESYSTEM_ERR");
			}
		}

		private String addChrExtension (String filename) {
			if (filename.contains(".")) {
				Optional<String> extension = EKUtils.getExtension(filename);
				if (!extension.isPresent()) {
					return filename + ".chr";
				}

				if (extension.get().equals("chr")) {
					return filename;
				} else {
					return filename + ".chr";
				}
			} else {
				return filename + ".chr";
			}
		}
	}
}
