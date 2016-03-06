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

package uk.me.mantas.eternity.tests.handlers;

import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.handlers.CheckForUpdates;
import uk.me.mantas.eternity.handlers.CheckForUpdates.UpdateChecker;
import uk.me.mantas.eternity.tests.ExposedClass;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CheckForUpdatesTest extends TestHarness {
	@Test
	public void getJarTimestampDirectoryNotExists () {
		final File notExists = new File("404");
		final Logger mockLogger = interceptLogging(CheckForUpdates.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		final long timestamp = cls.call("getJarTimestamp", notExists);
		verify(mockLogger).error("Jar directory '%s' was empty.%n", notExists.getAbsolutePath());
		assertEquals(0L, timestamp);
	}

	@Test
	public void getJarTimestampNoMatches () {
		final File mockDirectory = mock(File.class);
		final Logger mockLogger = interceptLogging(CheckForUpdates.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		final File[] files = new File[] {
			new File("1234567890123.jar")
			, new File("12345678901234")
			, new File("123456789abcde.jar")
			, new File(".")
			, new File("..")
		};

		when(mockDirectory.getAbsolutePath()).thenReturn("N/A");
		when(mockDirectory.listFiles()).thenReturn(files);

		final long timestamp = cls.call("getJarTimestamp", mockDirectory);
		verify(mockLogger).error("No timestamped jar in '%s'.&n", "N/A");
		assertEquals(0L, timestamp);
	}

	@Test
	public void getJarTimestampFindsFirst () {
		final File mockDirectory = mock(File.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		final File[] files = new File[] {
			new File("12345678901234.jar")
			, new File("12345678901235.jar")
		};

		when(mockDirectory.listFiles()).thenReturn(files);

		final long timestamp = cls.call("getJarTimestamp", mockDirectory);
		assertEquals(12345678901234L, timestamp);
	}

	@Test
	public void isUpdateServerReturnsFalse () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		final Optional<?> latest = cls.call("isUpdate", "false");
		assertFalse(latest.isPresent());
	}

	@Test
	public void isUpdateJarDirectoryNotExists () {
		final Environment mockEnvironment = mockEnvironment();
		final File mockDirectory = mock(File.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		when(mockEnvironment.directory().jar()).thenReturn(mockDirectory);
		when(mockDirectory.exists()).thenReturn(false);

		final Optional<?> latest = cls.call("isUpdate", "0");
		assertFalse(latest.isPresent());
	}

	@Test
	public void isUpdateCheckTimestamp () {
		final Environment mockEnvironment = mockEnvironment();
		final File mockDirectory = mock(File.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		final File[] files = new File[] { new File("12345678901234.jar") };

		when(mockEnvironment.directory().jar()).thenReturn(mockDirectory);
		when(mockDirectory.exists()).thenReturn(true);
		when(mockDirectory.listFiles()).thenReturn(files);

		final Optional<?> lessThan = cls.call("isUpdate", "0");
		assertFalse(lessThan.isPresent());

		final Optional<String> greaterThan = cls.call("isUpdate", "12345678901235");
		assertTrue(greaterThan.isPresent());
		assertEquals("12345678901235", greaterThan.get());
	}

	@Test
	public void isLegacyCodeNotWindows () {
		final Environment mockEnvironment = mockEnvironment();
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		when(mockEnvironment.isWindows()).thenReturn(false);
		assertFalse(cls.call("isLegacyCode"));
	}

	@Test
	public void isLegacyCodeExeNotFound () {
		final Environment mockEnvironment = mockEnvironment();
		final Logger mockLogger = interceptLogging(CheckForUpdates.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		when(mockEnvironment.isWindows()).thenReturn(true);
		when(mockEnvironment.detectExeSize()).thenReturn(Optional.empty());

		assertFalse(cls.call("isLegacyCode"));
		verify(mockLogger).error(
			"CEF reported this is a Windows system but eternity.exe was not found.%n");
	}

	@Test
	public void isLegacyCodeNotLegacyExe () {
		final Environment mockEnvironment = mockEnvironment();
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		when(mockEnvironment.isWindows()).thenReturn(true);
		when(mockEnvironment.detectExeSize()).thenReturn(Optional.of(0L));
		assertFalse(cls.call("isLegacyCode"));
	}

	@Test
	public void isLegacyCode () {
		final Environment mockEnvironment = mockEnvironment();
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ExposedClass cls = expose(new UpdateChecker(mockCallback));

		when(mockEnvironment.isWindows()).thenReturn(true);
		when(mockEnvironment.detectExeSize()).thenReturn(Optional.of(0x17400L));
		assertTrue(cls.call("isLegacyCode"));
	}
}
