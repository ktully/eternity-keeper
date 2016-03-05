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
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;

import java.util.Iterator;

public class SaveSettings extends CefMessageRouterHandlerAdapter {
	private static final Logger logger = Logger.getLogger(SaveSettings.class);

	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		// Consider running this in a separate thread if it takes too long.
		try {
			JSONObject settings = Settings.getInstance().json;
			JSONObject json = new JSONObject(request);
			Iterator<String> keys = json.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				settings.put(key, json.get(key));
			}

			Settings.getInstance().save();
		} catch (JSONException e) {
			logger.error(
				"Unable to read JSON-formatted settings data from client: %s%n"
				, e.getMessage());
		}

		callback.success("");
		return true;
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		logger.error("Query #%d was cancelled.%n", id);
	}
}
