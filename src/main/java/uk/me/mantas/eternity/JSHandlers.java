/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2015 Kim Mantas
 * <p>
 * Eternity Keeper is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * Eternity Keeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.me.mantas.eternity;

import org.cef.CefClient;
import org.cef.browser.CefMessageRouter;
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig;
import uk.me.mantas.eternity.handlers.*;

public class JSHandlers {
	private JSHandlers () {}

	public static void register (final CefClient cefClient, final EternityKeeper self) {
		CefMessageRouter getDefaultSaveLocationRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("getDefaultSaveLocation", "getDefaultSaveLocationCancel")
			, new GetDefaultSaveLocation());

		CefMessageRouter listSavedGamesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("listSavedGames", "listSavedGamesCancel")
			, new ListSavedGames());

		CefMessageRouter openSavedGameRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("openSavedGame", "openSavedGameCancel")
			, new OpenSavedGame());

		CefMessageRouter saveSettingsRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("saveSettings", "saveSettingsCancel")
			, new SaveSettings());

		CefMessageRouter saveChangesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("saveChanges", "saveChangesCancel")
			, new SaveChanges());

		CefMessageRouter closeWindowRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("closeWindow", "closeWindowCancel")
			, new CloseWindow(self));

		CefMessageRouter checkExtractionProgressRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("checkExtractionProgress", "checkExtractionProgressCancel")
			, new CheckExtractionProgress());

		CefMessageRouter checkForUpdatesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("checkForUpdates", "checkForUpdatesCancel")
			, new CheckForUpdates());

		CefMessageRouter downloadUpdateRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("downloadUpdate", "downloadUpdateCancel")
			, new DownloadUpdate());

		CefMessageRouter checkDownloadProgressRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("checkDownloadProgress", "checkDownloadProgressCancel")
			, new CheckDownloadProgress());

		CefMessageRouter exportCharacterRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("exportCharacter", "exportCharacterCancel")
			, new ExportCharacter());

		CefMessageRouter importCharacterRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("importCharacter", "importCharacterCancel")
			, new ImportCharacter());

		cefClient.addMessageRouter(getDefaultSaveLocationRouter);
		cefClient.addMessageRouter(listSavedGamesRouter);
		cefClient.addMessageRouter(openSavedGameRouter);
		cefClient.addMessageRouter(saveSettingsRouter);
		cefClient.addMessageRouter(saveChangesRouter);
		cefClient.addMessageRouter(closeWindowRouter);
		cefClient.addMessageRouter(checkExtractionProgressRouter);
		cefClient.addMessageRouter(checkForUpdatesRouter);
		cefClient.addMessageRouter(downloadUpdateRouter);
		cefClient.addMessageRouter(checkDownloadProgressRouter);
		cefClient.addMessageRouter(exportCharacterRouter);
		cefClient.addMessageRouter(importCharacterRouter);
	}
}
