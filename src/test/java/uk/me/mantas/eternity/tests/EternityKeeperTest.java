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

import org.junit.Test;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.EternityKeeper;

import java.io.File;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class EternityKeeperTest extends TestHarness {
	@Test
	public void cleanupOldUpdatesTestNoJarDirectory () {
		final Environment mockEnvironment = mockEnvironment();
		final File mockJarDirectory = mock(File.class);

		when(mockEnvironment.getJarDirectory()).thenReturn(mockJarDirectory);
		when(mockJarDirectory.exists()).thenReturn(false);

		final ExposedClass eternityKeeper = expose(EternityKeeper.class);
		eternityKeeper.call("cleanupOldUpdates");

		//noinspection ResultOfMethodCallIgnored
		verify(mockJarDirectory).mkdirs();
	}

	@Test
	public void cleanupOldUpdatesTestEmptyJarDirectory () {
		final Environment mockEnvironment = mockEnvironment();
		final File mockJarDirectory = mock(File.class);
		final ExposedClass eternityKeeper = expose(EternityKeeper.class);

		when(mockEnvironment.getJarDirectory()).thenReturn(mockJarDirectory);
		when(mockJarDirectory.exists()).thenReturn(true);
		when(mockJarDirectory.listFiles()).thenReturn(null);
		assertNull(eternityKeeper.call("cleanupOldUpdates"));

		when(mockJarDirectory.listFiles()).thenReturn(new File[]{new File("404")});
		assertNull(eternityKeeper.call("cleanupOldUpdates"));
	}

	private File mockJar (final String name) {
		final File mockFile = mock(File.class);
		when(mockFile.getName()).thenReturn(name);
		when(mockFile.isFile()).thenReturn(true);

		return mockFile;
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void cleanupOldUpdatesTestNoJars () {
		final Environment mockEnvironment = mockEnvironment();
		final File mockJarDirectory = mock(File.class);
		final ExposedClass eternityKeeper = expose(EternityKeeper.class);

		when(mockEnvironment.getJarDirectory()).thenReturn(mockJarDirectory);
		when(mockJarDirectory.exists()).thenReturn(true);

		final File discardedNotJar = mockJar("1.notjar");
		final File selectedNotJar = mockJar("2.notjar");
		when(mockJarDirectory.listFiles()).thenReturn(new File[]{
			discardedNotJar
			, selectedNotJar
		});

		eternityKeeper.call("cleanupOldUpdates");
		verify(discardedNotJar, never()).delete();
		verify(selectedNotJar, never()).delete();
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void cleanupOldUpdatesTestDeleteOldJars () {
		final Environment mockEnvironment = mockEnvironment();
		final File mockJarDirectory = mock(File.class);
		final ExposedClass eternityKeeper = expose(EternityKeeper.class);

		when(mockEnvironment.getJarDirectory()).thenReturn(mockJarDirectory);
		when(mockJarDirectory.exists()).thenReturn(true);

		final File discardedJar1 = mockJar("1.jar");
		final File discardedJar2 = mockJar("2.jar");
		final File latestJar = mockJar("3.jar");
		when(mockJarDirectory.listFiles()).thenReturn(new File[]{
			discardedJar1
			, discardedJar2
			, latestJar
		});

		eternityKeeper.call("cleanupOldUpdates");
		verify(discardedJar1).delete();
		verify(discardedJar2).delete();
		verify(latestJar, never()).delete();
	}
}
