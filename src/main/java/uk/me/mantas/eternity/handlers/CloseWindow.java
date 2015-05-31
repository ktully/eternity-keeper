package uk.me.mantas.eternity.handlers;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.WindowCloser;

import javax.swing.*;

public class CloseWindow extends CefMessageRouterHandlerAdapter {
	private final JFrame frame;

	public CloseWindow (JFrame frame) {
		super();
		this.frame = frame;
	}

	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		// Need to close the window in a separate thread otherwise we deadlock.
		callback.success("true");
		Environment.getInstance().closing = true;
		Environment.getInstance().getWorkers().execute(new WindowCloser(frame));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d cancelled.%n", id);
	}
}
