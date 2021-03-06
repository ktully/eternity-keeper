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

package uk.me.mantas.eternity.environment;

import uk.me.mantas.eternity.handlers.DownloadUpdate.UpdateDownloader;
import uk.me.mantas.eternity.handlers.ListSavedGames.SaveInfoLister;

import java.io.File;

public class State {
	State () {}

	public boolean closing = false;

	private File previousSaveDirectory = null;
	public File previousSaveDirectory () { return previousSaveDirectory; }
	public void previousSaveDirectory (final File dir) { previousSaveDirectory = dir; }

	private SaveInfoLister currentSaveLister = null;
	public SaveInfoLister currentSaveLister () { return currentSaveLister; }
	public void currentSaveLister (final SaveInfoLister lister) { currentSaveLister = lister; }

	private UpdateDownloader currentUpdateDownloader = null;
	public UpdateDownloader currentUpdateDownloader () { return currentUpdateDownloader; }
	public void currentUpdateDownloader (final UpdateDownloader downloader) {
		currentUpdateDownloader = downloader;
	}
}
