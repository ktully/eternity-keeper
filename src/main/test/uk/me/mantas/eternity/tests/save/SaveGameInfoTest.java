package uk.me.mantas.eternity.tests.save;

import org.junit.Test;
import uk.me.mantas.eternity.save.SaveGameInfo;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.me.mantas.eternity.save.SaveGameInfo.SaveFileInfoException;

public class SaveGameInfoTest extends TestHarness {
	protected String PREFIX = "EK-SGI-";
	private static final String SAVEINFO_XML = "/SaveGameInfoTest.saveinfo.xml";

	@SuppressWarnings("unchecked")
	@Test
	public void malformedSaveFolderName () {
		File mockSaveFolder = mock(File.class);
		Map mockInfoFiles = mock(Map.class);

		when(mockSaveFolder.getAbsolutePath()).thenReturn("/path/to/save");
		when(mockSaveFolder.getName()).thenReturn("save");

		try {
			new SaveGameInfo(mockSaveFolder, mockInfoFiles);
		} catch (SaveFileInfoException e) {
			assertNotNull(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void saveInfoXMLDoesNotExist () {
		File mockSaveFolder = getMockSaveFolder();
		Map mockInfoFiles = mock(Map.class);

		when(mockInfoFiles.get("saveinfo.xml")).thenReturn(new File("404"));

		try {
			new SaveGameInfo(mockSaveFolder, mockInfoFiles);
		} catch (SaveFileInfoException e) {
			assertNotNull(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void saveInfoXMLMalformed () throws URISyntaxException {
		File mockSaveFolder = getMockSaveFolder();
		Map mockInfoFiles = mock(Map.class);

		File saveinfoXMLTest = new File(
			this.getClass().getResource(SAVEINFO_XML).toURI());

		when(mockInfoFiles.get("saveinfo.xml")).thenReturn(saveinfoXMLTest);

		try {
			new SaveGameInfo(mockSaveFolder, mockInfoFiles);
		} catch (SaveFileInfoException e) {
			assertNotNull(e);
		}
	}

	private File getMockSaveFolder () {
		File mockSaveFolder = mock(File.class);

		when(mockSaveFolder.getAbsolutePath())
			.thenReturn("/path/to/guid systemname.savegame");

		when(mockSaveFolder.getName()).thenReturn("guid systemname.savegame");

		return mockSaveFolder;
	}
}
