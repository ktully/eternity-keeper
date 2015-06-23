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

import org.cef.callback.CefQueryCallback;
import org.json.JSONObject;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.save.SavedGameOpener;
import uk.me.mantas.eternity.tests.TestHarness;
import uk.me.mantas.eternity.tests.TestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SavedGameOpenerTest extends TestHarness {
	private static final String EXTRACTED =
		"[{\"isCompanion\":true"
		+ ",\"stats\":{"
			+ "\"BasePerception\":12"
			+ ",\"BaseResolve\":11"
			+ ",\"BaseDexterity\":10"
			+ ",\"BaseConstitution\":14"
			+ ",\"BaseIntellect\":12"
			+ ",\"BaseMight\":16}"
		+ ",\"GUID\":\"b1a7e809-0000-0000-0000-000000000000\""
		+ ",\"name\":\"Calisca\""
		+ ",\"portrait\":\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklE"
			+ "QVQI12P4DwABAQEAG7buVgAAAABJRU5ErkJggg==\"}"
		+ ",{\"isCompanion\":false"
		+ ",\"stats\":{"
			+ "\"BasePerception\":14"
			+ ",\"BaseResolve\":10"
			+ ",\"BaseDexterity\":13"
			+ ",\"BaseConstitution\":12"
			+ ",\"BaseIntellect\":10"
			+ ",\"BaseMight\":16}"
		+ ",\"GUID\":\"09517a0d-4fec-407c-a749-a531f3be64e0\""
		+ ",\"name\":\"Elenor\""
		+ ",\"portrait\":\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklE"
			+ "QVQI12P4DwABAQEAG7buVgAAAABJRU5ErkJggg==\"}]";

	@Test
	public void mobileObjectsFileNotExists () {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		SavedGameOpener cls = new SavedGameOpener("404", mockCallback);
		cls.run();

		verify(mockCallback).success("{\"error\":\"NOT_EXISTS\"}");
	}

	@Test
	public void deserializationError () throws IOException {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		Optional<File> saveDir = EKUtils.createTempDir(PREFIX);
		assertTrue(saveDir.isPresent());

		Files.createFile(
			new File(saveDir.get(), "MobileObjects.save").toPath());

		SavedGameOpener cls =
			new SavedGameOpener(saveDir.get().getAbsolutePath(), mockCallback);

		cls.run();
		verify(mockCallback).success("{\"error\":\"DESERIALIZATION_ERR\"}");
	}

	@Test
	public void saveGameOpened ()
		throws URISyntaxException
		, NoSuchFieldException
		, IllegalAccessException {

		Settings mockSettings = TestUtils.mockSettings();
		JSONObject mockJSON = mock(JSONObject.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		File resources = new File(getClass().getResource("/").toURI());
		SavedGameOpener cls =
			new SavedGameOpener(resources.getAbsolutePath(), mockCallback);

		mockSettings.json = mockJSON;
		when(mockJSON.getString("gameLocation"))
			.thenReturn(
				new File(resources, "SavedGameOpenerTest").getAbsolutePath());

		cls.run();
		verify(mockCallback).success(EXTRACTED);
	}
}
