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


package uk.me.mantas.eternity.tests.handlers;

import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.handlers.ListSavedGames.SaveInfoLister;
import uk.me.mantas.eternity.save.SaveGameExtractor;
import uk.me.mantas.eternity.save.SaveGameInfo;
import uk.me.mantas.eternity.tests.ExposedClass;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

public class SaveInfoListerTest extends TestHarness {
	private final static String SAVES_JSON =
		"[{\"date\":\"2015-05-10 15:44:52\",\"difficulty\":\"Hard\""
		+ ",\"chapter\":1,\"systemName\":\"systemname\",\"playerName\":\"Elenor\""
		+ ",\"portraits\":["
			+ "\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVQI12P4Dw"
			+ "ABAQEAG7buVgAAAABJRU5ErkJggg==\",\"iVBORw0KGgoAAAANSUhEUgAAAAEAA"
			+ "AABCAAAAAA6fptVAAAACklEQVQI12P4DwABAQEAG7buVgAAAABJRU5ErkJggg==\""
			+ ",\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVQI12P4D"
			+ "wABAQEAG7buVgAAAABJRU5ErkJggg==\",\"iVBORw0KGgoAAAANSUhEUgAAAAEA"
			+ "AAABCAAAAAA6fptVAAAACklEQVQI12P4DwABAQEAG7buVgAAAABJRU5ErkJggg==\""
			+ ",\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVQI12P4D"
			+ "wABAQEAG7buVgAAAABJRU5ErkJggg==\",\"iVBORw0KGgoAAAANSUhEUgAAAAEA"
			+ "AAABCAAAAAA6fptVAAAACklEQVQI12P4DwABAQEAG7buVgAAAABJRU5ErkJggg==\"]"
		+ ",\"guid\":\"guid\",\"absolutePath\":\"%s\""
		+ ",\"screenshot\":\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACk"
			+ "lEQVQI12P4DwABAQEAG7buVgAAAABJRU5ErkJggg==\""
		+ ",\"trialOfIron\":false,\"userSaveName\":\"Start\""
		+ ",\"sceneTitle\":\"Encampment\"}"
		+ ",{\"date\":\"2015-05-10 15:44:52\",\"difficulty\":\"Hard\""
		+ ",\"chapter\":1,\"systemName\":\"systemname\",\"playerName\":\"Elenor\""
		+ ",\"portraits\":["
			+ "\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVQI12P4Dw"
			+ "ABAQEAG7buVgAAAABJRU5ErkJggg==\"]"
		+ ",\"guid\":\"guid2\",\"absolutePath\":\"%s\""
		+ ",\"screenshot\":\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACk"
			+ "lEQVQI12P4DwABAQEAG7buVgAAAABJRU5ErkJggg==\""
		+ ",\"trialOfIron\":false,\"userSaveName\":\"Start\""
		+ ",\"sceneTitle\":\"Encampment\"}]";

	private final static String NO_RESULTS = "{\"error\":\"NO_RESULTS\"}";

	@Test
	public void noSaveFilesFound () throws IOException {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		File workingDirectory = Files.createTempDirectory(PREFIX).toFile();
		Environment.getInstance().directory().working(workingDirectory);

		new SaveInfoLister("404", mockCallback).run();
		verify(mockCallback).success(NO_RESULTS);
	}

	@Test
	public void saveFilesListedSuccessfully () throws IOException, URISyntaxException {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		File workingDirectory = Files.createTempDirectory(PREFIX).toFile();
		Environment.getInstance().directory().working(workingDirectory);

		String save1 = new File(workingDirectory, "guid systemname.savegame")
			.getAbsolutePath().replace("\\", "\\\\");

		String save2 = new File(workingDirectory, "guid2 systemname.savegame")
			.getAbsolutePath().replace("\\", "\\\\");

		String savesLocation = new File(
			this.getClass().getResource("/SaveGameExtractorTest").toURI())
			.getAbsolutePath();

		new SaveInfoLister(savesLocation, mockCallback).run();
		verify(mockCallback).success(String.format(SAVES_JSON, save1, save2));
	}

	@Test
	public void unpackAllSavesTestNoSaves () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final SaveGameExtractor mockExtractor = mock(SaveGameExtractor.class);
		final SaveInfoLister saveInfoLister = new SaveInfoLister("404", mockCallback);
		final ExposedClass exposedLister = expose(saveInfoLister);

		when(mockExtractor.unpackAllSaves()).thenReturn(Optional.empty());
		exposedLister.call("unpackAllSaves", mockExtractor);

		when(mockExtractor.unpackAllSaves()).thenReturn(Optional.of(new SaveGameInfo[0]));
		exposedLister.call("unpackAllSaves", mockExtractor);

		verify(mockCallback, times(2)).success(NO_RESULTS);
	}

	@Test
	public void unpackAllSavesTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final SaveGameExtractor mockExtractor = mock(SaveGameExtractor.class);
		final SaveGameInfo mockInfo = mock(SaveGameInfo.class);
		final SaveInfoLister saveInfoLister = new SaveInfoLister("404", mockCallback);
		final ExposedClass exposedLister = expose(saveInfoLister);

		when(mockExtractor.unpackAllSaves()).thenReturn(Optional.of(new SaveGameInfo[]{mockInfo}));
		exposedLister.call("unpackAllSaves", mockExtractor);

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(mockCallback).success(argument.capture());
		assertNotEquals(NO_RESULTS, argument.getValue());
	}
}
