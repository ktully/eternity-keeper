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
	}
}
