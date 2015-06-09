package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.handler.CefDialogHandler.FileDialogMode;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import uk.me.mantas.eternity.Environment;

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
		private final String absolutePath;
		private final CefQueryCallback callback;

		public SelectChrFile (
			CefBrowser browser
			, String absolutePath
			, CefQueryCallback callback) {

			this.browser = browser;
			this.absolutePath = absolutePath;
			this.callback = callback;
		}

		@Override
		public void run () {
			browser.runFileDialog(
				FileDialogMode.FILE_DIALOG_OPEN
				, "Choose a character"
				, ""
				, new Vector<String>(){{add(".chr");}}
				, new FileCallback(absolutePath, callback));
		}
	}

	private class FileCallback implements CefRunFileDialogCallback {
		private final String absolutePath;
		private final CefQueryCallback callback;

		public FileCallback (
			String absolutePath
			, CefQueryCallback callback) {

			this.absolutePath = absolutePath;
			this.callback = callback;
		}

		@Override
		public void onFileDialogDismissed (
			CefBrowser browser
			, Vector<String> filenames) {

			if (filenames.size() < 1 || filenames.get(0).length() < 1) {
				callback.failure(-1, "NO_SAVE");
			}
		}
	}
}
