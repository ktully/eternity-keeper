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
import uk.me.mantas.eternity.save.SavedGameOpener;

import java.io.File;

public class OpenSavedGame extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(OpenSavedGame.class);

	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		if (!(new File(request).exists())) {
			notExists(callback);
			return true;
		}

		Environment.getInstance().getWorkers().execute(
			new SavedGameOpener(request, callback));

		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		logger.error("Query #%d was cancelled.%n", id);
		Environment.joinAllWorkers();
	}

	public static void notExists (CefQueryCallback callback) {
		String json = new JSONStringer()
			.object()
				.key("error").value("NOT_EXISTS")
			.endObject()
			.toString();

		callback.success(json);
	}

	public static void deserializationError (CefQueryCallback callback) {
		String json = new JSONStringer()
			.object()
				.key("error").value("DESERIALIZATION_ERR")
			.endObject()
			.toString();

		callback.success(json);
	}
}
