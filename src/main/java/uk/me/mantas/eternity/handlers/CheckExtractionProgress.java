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
import uk.me.mantas.eternity.save.SaveGameExtractor;

import static uk.me.mantas.eternity.handlers.ListSavedGames.SaveInfoLister;

public class CheckExtractionProgress extends CefMessageRouterHandlerAdapter {
	@Override
	public boolean onQuery (
		CefBrowser browser
		, long id
		, String request
		, boolean persistent
		, CefQueryCallback callback) {

		Environment environment = Environment.getInstance();
		SaveInfoLister lister = environment.getCurrentSaveLister();

		if (lister == null) {
			callback.success(response(0d));
			return true;
		}

		SaveGameExtractor extractor = lister.extractor;
		if (extractor == null) {
			callback.success(response(0d));
			return true;
		}

		int total = extractor.totalFiles.get();
		int count = extractor.currentCount.get();
		double percentage = ((double) count / (double) total) * 100;

		callback.success(response(percentage));
		return true;
	}

	private String response (double val) {
		return new JSONStringer()
			.object()
				.key("update").value(val)
			.endObject()
			.toString();
	}

	@Override
	public void onQueryCanceled (CefBrowser browser, long id) {
		System.err.printf("Query #%d was cancelled.%n", id);
	}
}
