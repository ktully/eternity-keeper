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
