/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2016 the authors.
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

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefQueryCallback;
import org.junit.Test;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.environment.ClassFinder;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.handlers.GetGameStructures;
import uk.me.mantas.eternity.tests.ExposedClass;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class GetGameStructuresTest extends TestHarness {
	private enum Enum {A, B}
	private enum Enum2 {X, Y, Z}
	private static class NotAnEnum {}
	private static class HasNestedEnum {
		private enum NestedEnum {}
		private static class HasSecondNestedEnum {
			private enum SecondNestedEnum {}
		}
	}

	@Test
	public void enumsToJSONTest () {
		final ExposedClass exposedGetGameStructures = expose(GetGameStructures.class);
		final Map<Object, Class> argMap = new HashMap<>();

		argMap.put(new HashSet<Class<?>>(), Set.class);
		final String testEmpty = exposedGetGameStructures.call("enumsToJSON", argMap);
		assertEquals("{}", testEmpty);

		final Set<Class<?>> enums = new LinkedHashSet<>();
		enums.add(Enum.class);
		enums.add(Enum2.class);
		argMap.clear();
		argMap.put(enums, Set.class);
		final String result = exposedGetGameStructures.call("enumsToJSON", argMap);
		assertEquals("{"
			+ "\"uk.me.mantas.eternity.tests.handlers.GetGameStructuresTest$Enum2\":"
				+ "[\"X\",\"Y\",\"Z\"]"
			+ ",\"uk.me.mantas.eternity.tests.handlers.GetGameStructuresTest$Enum\":[\"A\",\"B\"]"
		+ "}", result);
	}

	@Test
	public void findEnumsTest () {
		final ExposedClass exposedGetGameStructures = expose(GetGameStructures.class);
		final Map<Object, Class> argMap = new HashMap<>();

		argMap.put(new Class<?>[0], Class[].class);
		final Set<Class<?>> empty = exposedGetGameStructures.call("findEnums", argMap);
		assertEquals(0, empty.size());

		final Set<Class<?>> expected = new HashSet<>();
		expected.add(Enum.class);
		expected.add(HasNestedEnum.NestedEnum.class);
		expected.add(HasNestedEnum.HasSecondNestedEnum.SecondNestedEnum.class);

		final Class<?>[] classes = new Class<?>[]{Enum.class, NotAnEnum.class, HasNestedEnum.class};
		argMap.clear();
		argMap.put(classes, Class[].class);

		final Set<Class<?>> result = exposedGetGameStructures.call("findEnums", argMap);
		assertTrue(result.containsAll(expected));
	}

	@Test
	public void onQueryTest () throws IOException {
		final Environment environment = mockEnvironment();
		final CefBrowser mockBrowser = mock(CefBrowser.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ClassFinder mockClassFinder = mock(ClassFinder.class);
		final GetGameStructures getGameStructures = new GetGameStructures();
		final ClassPath classPath = ClassPath.from(getClass().getClassLoader());

		final Iterator<ClassInfo> it = classPath.getAllClasses().iterator();
		ClassInfo classInfo = null;
		while (it.hasNext()) {
			final ClassInfo info = it.next();
			if (info.getSimpleName().equals("HasNestedEnum")) {
				classInfo = info;
			}
		}

		final Set<ClassInfo> classInfoSet = new HashSet<>();
		assertNotNull(classInfo);
		classInfoSet.add(classInfo);

		when(environment.classFinder()).thenReturn(mockClassFinder);
		when(mockClassFinder.allClassesInPackage(anyString())).thenReturn(classInfoSet);
		when(environment.config().gameStructuresPkg()).thenReturn("");

		getGameStructures.onQuery(mockBrowser, 0, "true", false, mockCallback);
		verify(mockCallback).success("{"
			+ "\"uk.me.mantas.eternity.tests.handlers"
				+ ".GetGameStructuresTest$HasNestedEnum$HasSecondNestedEnum$SecondNestedEnum\":[]"
			+ ",\"uk.me.mantas.eternity.tests.handlers"
			+ ".GetGameStructuresTest$HasNestedEnum$NestedEnum\":[]"
		+ "}");
	}
}
