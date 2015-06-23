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


package uk.me.mantas.eternity.tests;

import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Settings;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {
	public static Environment mockEnvironment ()
		throws NoSuchFieldException
		, IllegalAccessException {

		Environment environment = Environment.getInstance();
		Environment mockEnvironment = mock(Environment.class);
		Field instanceField = Environment.class.getDeclaredField("instance");

		instanceField.setAccessible(true);
		instanceField.set(environment, mockEnvironment);

		when(mockEnvironment.getWorkers()).thenReturn(environment.getWorkers());

		return mockEnvironment;
	}

	public static Settings mockSettings ()
		throws NoSuchFieldException
		, IllegalAccessException {

		Settings settings = Settings.getInstance();
		Settings mockSettings = mock(Settings.class);
		Field instanceField = Settings.class.getDeclaredField("instance");

		instanceField.setAccessible(true);
		instanceField.set(settings, mockSettings);

		return mockSettings;
	}
}
