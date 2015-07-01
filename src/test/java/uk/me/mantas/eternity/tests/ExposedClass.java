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

// This class serves to expose encapsulated functionality of classes so they can be unit tested
// without compromising that encapsulation in non-test code.

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertNull;

public class ExposedClass {
	private final Class<?> cls;
	private final Object instance;

	public ExposedClass (final Class<?> cls) {
		this.cls = cls;
		this.instance = null;
	}

	public ExposedClass (final Object instance) {
		this.cls = instance.getClass();
		this.instance = instance;
	}

	private Class[] extractClasses (final Object[] args) {
		return Arrays.stream(args).map(arg -> {
			if (arg.getClass().getSimpleName().contains("Mockito")) {
				return arg.getClass().getSuperclass();
			} else {
				return arg.getClass();
			}
		}).toArray(Class[]::new);
	}

	public Object call (final String methodName, Object... args) {
		final Class[] argsClasses = extractClasses(args);
		final Map<Object, Class> argMap =
			IntStream.range(0, args.length)
				.mapToObj(i -> new SimpleImmutableEntry<>(args[i], argsClasses[i]))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		return call(methodName, argMap);
	}

	public Object call (final String methodName, final Map<Object, Class> args) {
		Method method = null;
		final Class[] argsClasses =
			args.entrySet().stream().map(Entry::getValue).toArray(Class[]::new);

		try {
			method = cls.getDeclaredMethod(methodName, argsClasses);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			assertNull(e);
		}

		method.setAccessible(true);
		final Object[] argsInstances =
			args.entrySet().stream().map(Entry::getKey).toArray(Object[]::new);

		try {
			return method.invoke(instance, argsInstances);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			assertNull(e);
		}

		return null;
	}

	public void set (final String fieldName, final Object value) {
		Field field = null;
		try {
			field = cls.getField(fieldName);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			assertNull(e);
		}

		field.setAccessible(true);

		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			assertNull(e);
		}
	}
}
