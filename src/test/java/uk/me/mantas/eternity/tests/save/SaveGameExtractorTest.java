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


package uk.me.mantas.eternity.tests.save;

import org.junit.Test;
import uk.me.mantas.eternity.save.SaveGameExtractor;
import uk.me.mantas.eternity.save.SaveGameInfo;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SaveGameExtractorTest extends TestHarness {
	@Test
	public void savesLocationNotExists () {
		File mockWorkingDirectory = mock(File.class);
		SaveGameExtractor saveGameExtractor =
			new SaveGameExtractor("404", mockWorkingDirectory);

		assertFalse(saveGameExtractor.unpackAllSaves().isPresent());
	}

	@Test
	public void saveGamesExtractedSuccessfully ()
		throws IOException, URISyntaxException {

		String savesLocation = new File(
			this.getClass().getResource("/SaveGameExtractorTest").toURI())
			.getAbsolutePath();

		File workingDirectory = Files.createTempDirectory(PREFIX).toFile();

		SaveGameExtractor saveGameExtractor =
			new SaveGameExtractor(savesLocation, workingDirectory);

		Optional<SaveGameInfo[]> saveGameInfo =
			saveGameExtractor.unpackAllSaves();

		assertTrue(saveGameInfo.isPresent());
		assertEquals(2, saveGameInfo.get().length);
		assertEquals(6, saveGameInfo.get()[0].portraits.size());
		assertEquals(4, saveGameExtractor.totalFiles.get());
		assertEquals(3, saveGameExtractor.currentCount.get());
	}
}
