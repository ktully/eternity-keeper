package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.handler.CefDialogHandler.FileDialogMode;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.save.CharacterImporter;
import uk.me.mantas.eternity.save.SavedGameOpener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

public class ImportCharacter extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment.getInstance().getWorkers().execute(
			new SelectChrFile(browser, request, callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d cancelled.%n", id);
	}

	private class SelectChrFile implements Runnable {
		private final CefBrowser browser;
		private final String request;
		private final CefQueryCallback callback;

		public SelectChrFile (
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
				FileDialogMode.FILE_DIALOG_OPEN
				, "Choose a character"
				, ""
				, new Vector<String>(){{add(".chr");}}
				, new FileCallback(request, callback));
		}
	}

	private class FileCallback implements CefRunFileDialogCallback {
		private final String request;
		private final CefQueryCallback callback;

		public FileCallback (
			String request
			, CefQueryCallback callback) {

			this.request = request;
			this.callback = callback;
		}

		@Override
		public void onFileDialogDismissed (
			CefBrowser browser
			, Vector<String> filenames) {

			if (filenames.size() < 1 || filenames.get(0).length() < 1) {
				callback.failure(-1, "NO_SAVE");
				return;
			}

			doImport(request, callback, filenames.get(0));
		}
	}

	private void doImport (
		String request
		, CefQueryCallback callback
		, String chrFile) {

		try {
			CharacterImporter importer =
				new CharacterImporter(request, chrFile);

			boolean success = importer.importCharacter();

			if (success) {
				SavedGameOpener opener = new SavedGameOpener(
					importer.saveFile.getAbsolutePath()
					, callback);

				opener.run();
			} else {
				callback.failure(-1, "Character import failed.");
			}
		} catch (JSONException e) {
			System.err.printf("Error parsing JSON request: %s%n", request);
			callback.failure(-1, "Error parsing JSON request.");
		} catch (FileNotFoundException e) {
			System.err.printf("File not found: %s%n", e.getMessage());
			callback.failure(-1, "Unable to find your save or CHR file.");
		} catch (IOException e) {
			System.err.printf("%s%n", e.getMessage());
			callback.failure(
				-1
				, "Error modifying temporary MobileObjects.save");
		}
	}
}
