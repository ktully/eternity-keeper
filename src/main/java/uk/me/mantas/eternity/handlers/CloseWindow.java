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
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.WindowCloser;
import uk.me.mantas.eternity.environment.Environment;

import javax.swing.*;

public class CloseWindow extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(CloseWindow.class);
	private final JFrame frame;

	public CloseWindow (final JFrame frame) {
		super();
		this.frame = frame;
	}

	@Override
	public boolean onQuery (
		final CefBrowser browser
		, final long id
		, final String request
		, final boolean persistent
		, final CefQueryCallback callback) {

		// Need to close the window in a separate thread otherwise we deadlock.
		callback.success("true");
		Environment.getInstance().state().closing = true;
		Environment.getInstance().workers().execute(new WindowCloser(frame));

		return true;
	}

	@Override
	public void onQueryCanceled (final CefBrowser browser, final long id) {
		logger.error("Query #%d cancelled.%n", id);
	}
}
