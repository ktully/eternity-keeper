package uk.me.mantas.eternity.tests.handlers;

import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.handlers.ListSavedGames.SaveInfoLister;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

	@Test
	public void noSaveFilesFound () throws IOException {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		File workingDirectory = Files.createTempDirectory(PREFIX).toFile();
		Environment.getInstance().setWorkingDirectory(workingDirectory);

		new SaveInfoLister("404", mockCallback).run();
		verify(mockCallback).success("{\"error\":\"NO_RESULTS\"}");
	}

	@Test
	public void saveFilesListedSuccessfully () throws IOException, URISyntaxException {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		File workingDirectory = Files.createTempDirectory(PREFIX).toFile();
		Environment.getInstance().setWorkingDirectory(workingDirectory);

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
}
