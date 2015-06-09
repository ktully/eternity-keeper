package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.save.CharacterExporter;

import java.io.FileNotFoundException;
import java.util.Vector;

import static org.cef.handler.CefDialogHandler.FileDialogMode;

public class ExportCharacter extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment.getInstance().getWorkers().execute(
			new FileDialog(browser, request, callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d cancelled.%n", id);
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
				, new Vector<String>(){{add("chr");}}
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
			CefBrowser browser
			, Vector<String> filenames) {

			if (filenames.size() < 1) {
				callback.failure(-1, "NO_SAVENAME");
				return;
			}

			try {
				JSONObject json = new JSONObject(request);
				String guid = json.getString("GUID");
				String savePath = json.getString("absolutePath");

				CharacterExporter exporter = new CharacterExporter(
					savePath
					, guid
					, filenames.get(0));

				boolean exportedSuccessfully = exporter.export();

				if (exportedSuccessfully) {
					callback.success("true");
				} else {
					callback.failure(-1, "EXPORT_ERR");
				}
			} catch (JSONException e) {
				System.err.printf("Error parsing JSON request: %s%n", request);
				callback.failure(-1, "BAD_REQUEST");
			} catch (FileNotFoundException e) {
				System.err.printf("Unable to find file : %s%n", e.getMessage());
			}
		}
	}
}