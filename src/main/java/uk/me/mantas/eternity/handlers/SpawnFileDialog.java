package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.handler.CefDialogHandler.FileDialogMode;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import java.util.Vector;

public class SpawnFileDialog extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		CefRunFileDialogCallback fileDialogDone = (cefBrowser, vector) -> {
			System.out.printf("File dialog done.%n");
		};

		Vector<String> selected = new Vector<>();
		browser.runFileDialog(
			FileDialogMode.FILE_DIALOG_OPEN
			, request
			, ""
			, selected
			, fileDialogDone);

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
	}
}
