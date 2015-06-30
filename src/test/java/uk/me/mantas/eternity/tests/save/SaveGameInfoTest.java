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


package uk.me.mantas.eternity.tests.save;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import uk.me.mantas.eternity.save.SaveGameInfo;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.me.mantas.eternity.save.SaveGameInfo.SaveFileInfoException;

@SuppressWarnings("unchecked")
public class SaveGameInfoTest extends TestHarness {
	private static final String PNG_FILE = "/png";
	private static final String SAVEINFO_XML = "/SaveGameInfoTest.saveinfo.xml";
	private static final String SAVEINFO_XML_MALFORMED =
		"/SaveGameInfoTest.saveinfo.malformed.xml";

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

	@Test
	public void saveInfoXMLMalformed () throws URISyntaxException {
		File mockSaveFolder = getMockSaveFolder();
		Map mockInfoFiles = mock(Map.class);

		File saveinfoXMLTest = new File(
			this.getClass().getResource(SAVEINFO_XML_MALFORMED).toURI());

		when(mockInfoFiles.get("saveinfo.xml")).thenReturn(saveinfoXMLTest);

		try {
			new SaveGameInfo(mockSaveFolder, mockInfoFiles);
		} catch (SaveFileInfoException e) {
			assertNotNull(e);
		}
	}


	@Test
	public void imageFilesNotExist () throws URISyntaxException {
		File mockSaveFolder = getMockSaveFolder();
		Map mockInfoFiles = mock(Map.class);

		File saveinfoXML = new File(this.getClass().getResource(SAVEINFO_XML).toURI());
		when(mockInfoFiles.get("saveinfo.xml")).thenReturn(saveinfoXML);
		when(mockInfoFiles.get("screenshot.png")).thenReturn(new File("404"));

		try {
			new SaveGameInfo(mockSaveFolder, mockInfoFiles);
		} catch (SaveFileInfoException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void parsesSaveInfoSuccessfully ()
		throws URISyntaxException, SaveFileInfoException, IOException {

		File mockSaveFolder = getMockSaveFolder();
		Map mockInfoFiles = mock(Map.class);

		String pngData = getPNGData();
		File png = new File(this.getClass().getResource(PNG_FILE).toURI());
		File saveinfoXML = new File(this.getClass().getResource(SAVEINFO_XML).toURI());

		when(mockInfoFiles.get("saveinfo.xml")).thenReturn(saveinfoXML);
		when(mockInfoFiles.get("screenshot.png")).thenReturn(png);
		when(mockInfoFiles.get("0.png")).thenReturn(png);
		when(mockInfoFiles.get("1.png")).thenReturn(png);

		SaveGameInfo info = new SaveGameInfo(mockSaveFolder, mockInfoFiles);

		assertEquals("guid", info.guid);
		assertEquals("systemname", info.systemName);
		assertEquals("/path/to/guid systemname.savegame", info.absolutePath);
		assertEquals("Elenor", info.playerName);
		assertEquals("Encampment", info.sceneTitle);
		assertEquals(1, info.chapter);
		assertEquals(false, info.trialOfIron);
		assertEquals("Start", info.userSaveName);
		assertEquals("Hard", info.difficulty);
		assertEquals(pngData, info.screenshot);

		assertArrayEquals(
			new String[]{pngData, pngData}
			, info.portraits.toArray(new String[info.portraits.size()]));

		assertEquals(new DateTime(2015, 5, 10, 15, 44, 52, DateTimeZone.UTC), info.timestamp);
	}

	private File getMockSaveFolder () {
		File mockSaveFolder = mock(File.class);
		when(mockSaveFolder.getAbsolutePath()).thenReturn("/path/to/guid systemname.savegame");
		when(mockSaveFolder.getName()).thenReturn("guid systemname.savegame");

		return mockSaveFolder;
	}

	private String getPNGData () throws URISyntaxException, IOException {
		return Base64.getEncoder().encodeToString(
			FileUtils.readFileToByteArray(
				new File(this.getClass().getResource(PNG_FILE).toURI())));
	}
}
