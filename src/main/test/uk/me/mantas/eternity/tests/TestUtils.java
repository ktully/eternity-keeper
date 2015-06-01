package uk.me.mantas.eternity.tests;

import uk.me.mantas.eternity.Environment;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;

public class TestUtils {
	public static Environment mockEnvironment ()
		throws NoSuchFieldException
		, IllegalAccessException {

		Environment environment = Environment.getInstance();
		Environment mockEnvironment = mock(Environment.class);
		Field instanceField = Environment.class.getDeclaredField("instance");

		instanceField.setAccessible(true);
		instanceField.set(environment, mockEnvironment);

		return mockEnvironment;
	}
}
