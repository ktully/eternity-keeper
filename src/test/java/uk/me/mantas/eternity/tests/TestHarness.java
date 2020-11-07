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


package uk.me.mantas.eternity.tests;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.*;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;
import uk.me.mantas.eternity.serializer.SerializerFormat;
import uk.me.mantas.eternity.serializer.SharpSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class TestHarness {
	protected static String PREFIX = "EK-";

	@BeforeClass
	public static void setDefaultLocale () {
		Locale.setDefault(Locale.UK);
	}

	@Before
	public void setup () {
		Environment.initialise();
		Settings.clear();
	}

	@After
	public void cleanup () {
		File temp = new File(System.getProperty("java.io.tmpdir"));
		File[] files = temp.listFiles();

		if (files != null) {
			Arrays.stream(files)
				.filter((file) -> file.getName().startsWith(PREFIX))
				.forEach((file) -> {
					if (file.isDirectory()) {
						try {
							FileUtils.deleteDirectory(file);
						} catch (IOException e) {
							System.err.printf(
								"Unable to delete temporary directory '%s': "
									+ "%s%n"
								, file.getAbsoluteFile()
								, e.getMessage());
						}
					} else {
						assertTrue(file.delete());
					}
				});
		}
	}

	protected static Environment mockEnvironment () {
		Environment environment = Environment.getInstance();
		Environment mockEnvironment = mock(Environment.class);

		Field instanceField = null;
		try {
			instanceField = Environment.class.getDeclaredField("instance");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			assertNull(e);
		}

		instanceField.setAccessible(true);

		try {
			instanceField.set(environment, mockEnvironment);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			assertNull(e);
		}

		final Configuration mockConfiguration = mock(Configuration.class);
		final Directories mockDirectories = mock(Directories.class);
		final Factories mockFactory = mock(Factories.class);
		final State mockState = mock(State.class);
		final Variables mockVariables = mock(Variables.class);

		when(mockEnvironment.config()).thenReturn(mockConfiguration);
		when(mockEnvironment.directory()).thenReturn(mockDirectories);
		when(mockEnvironment.factory()).thenReturn(mockFactory);
		when(mockEnvironment.state()).thenReturn(mockState);
		when(mockEnvironment.variables()).thenReturn(mockVariables);
		when(mockEnvironment.workers()).thenReturn(environment.workers());

		return mockEnvironment;
	}

	protected static Settings mockSettings () {
		final Settings settings = Settings.getInstance();
		final Settings mockSettings = mock(Settings.class);

		Field instanceField = null;
		try {
			instanceField = Settings.class.getDeclaredField("instance");
		} catch (final NoSuchFieldException e) {
			assertNull(e);
		}

		instanceField.setAccessible(true);

		try {
			instanceField.set(settings, mockSettings);
		} catch (final IllegalAccessException e) {
			assertNull(e);
		}

		return mockSettings;
	}

	protected static ExposedClass expose (final Class<?> cls) {
		return new ExposedClass(cls);
	}

	protected static ExposedClass expose (final Object instance) {
		return new ExposedClass(instance);
	}

	protected static Logger interceptLogging (final Class cls) {
		final ExposedClass exposedClass = expose(cls);
		final Logger mockLogger = mock(Logger.class);
		exposedClass.set("logger", mockLogger);
		return mockLogger;
	}

	protected static SharpSerializer mockSerializer (final Environment mockEnvironment)
		throws FileNotFoundException {

		final SharpSerializerFactory mockSerializerFactory = mock(SharpSerializerFactory.class);
		final SharpSerializer mockSerializer = mock(SharpSerializer.class);

		when(mockEnvironment.factory().sharpSerializer()).thenReturn(mockSerializerFactory);
		when(mockSerializerFactory.forFile(anyString())).thenReturn(mockSerializer);
		when(mockSerializer.toFormat(any(SerializerFormat.class))).thenReturn(mockSerializer);

		return mockSerializer;
	}
}
