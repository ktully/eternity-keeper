/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2015 the authors.
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
		final CefMessageRouter getDefaultSaveLocationRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("getDefaultSaveLocation", "getDefaultSaveLocationCancel")
			, new GetDefaultSaveLocation());

		final CefMessageRouter listSavedGamesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("listSavedGames", "listSavedGamesCancel")
			, new ListSavedGames());

		final CefMessageRouter openSavedGameRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("openSavedGame", "openSavedGameCancel")
			, new OpenSavedGame());

		final CefMessageRouter saveSettingsRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("saveSettings", "saveSettingsCancel")
			, new SaveSettings());

		final CefMessageRouter saveChangesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("saveChanges", "saveChangesCancel")
			, new SaveChanges());

		final CefMessageRouter closeWindowRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("closeWindow", "closeWindowCancel")
			, new CloseWindow(self));

		final CefMessageRouter checkExtractionProgressRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("checkExtractionProgress", "checkExtractionProgressCancel")
			, new CheckExtractionProgress());

		final CefMessageRouter checkForUpdatesRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("checkForUpdates", "checkForUpdatesCancel")
			, new CheckForUpdates());

		final CefMessageRouter downloadUpdateRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("downloadUpdate", "downloadUpdateCancel")
			, new DownloadUpdate());

		final CefMessageRouter checkDownloadProgressRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("checkDownloadProgress", "checkDownloadProgressCancel")
			, new CheckDownloadProgress());

		final CefMessageRouter exportCharacterRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("exportCharacter", "exportCharacterCancel")
			, new ExportCharacter());

		final CefMessageRouter importCharacterRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("importCharacter", "importCharacterCancel")
			, new ImportCharacter());

		final CefMessageRouter getGameStructuresRouter = CefMessageRouter.create(
			new CefMessageRouterConfig("getGameStructures", "getGameStructuresCancel")
			, new GetGameStructures());

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
		cefClient.addMessageRouter(getGameStructuresRouter);
	}
}
