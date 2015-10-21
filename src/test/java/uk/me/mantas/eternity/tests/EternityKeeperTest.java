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

package uk.me.mantas.eternity.tests;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.me.mantas.eternity.EternityKeeper;
import uk.me.mantas.eternity.environment.Environment;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class EternityKeeperTest extends TestHarness {
	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void rolloverLogFileFailureStates () {
		final ExposedClass cls = expose(EternityKeeper.class);
		final File notExists = mock(File.class);
		final File notFile = mock(File.class);
		final File tooShort = mock(File.class);

		when(notExists.exists()).thenReturn(false);
		when(notFile.exists()).thenReturn(true);
		when(tooShort.exists()).thenReturn(true);
		when(notFile.isFile()).thenReturn(false);
		when(tooShort.isFile()).thenReturn(true);
		when(tooShort.length()).thenReturn(0L);

		cls.call("rolloverLogFile", notExists);
		cls.call("rolloverLogFile", notFile);
		cls.call("rolloverLogFile", tooShort);

		verify(notExists).exists();
		verify(notFile).exists();
		verify(tooShort).exists();
		verify(notExists, never()).isFile();
		verify(notFile).isFile();
		verify(tooShort).isFile();
		verify(notExists, never()).length();
		verify(notFile, never()).length();
		verify(tooShort).length();
	}

	@Test
	public void rolloverLogFile () throws URISyntaxException, IOException {
		final Environment mockEnvironment = mockEnvironment();
		final ExposedClass cls = expose(EternityKeeper.class);
		final String oldContents = "1234567\n1234567\n1234567\n1234567\n1234567\n1234567\n";
		final File log = new File(getClass().getResource("/eternity.log").toURI());

		when(mockEnvironment.config().maxLogSize()).thenReturn(48L);
		cls.call("rolloverLogFile", log);

		final String newContents = FileUtils.readFileToString(log, "UTF-8");
		assertEquals("1234567\n1234567\n1234567\n", newContents);
		FileUtils.write(log, oldContents, "UTF-8");
	}
}
