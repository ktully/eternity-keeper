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

package uk.me.mantas.eternity.tests.serializer;

import org.junit.Test;
import org.mockito.InOrder;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;
import uk.me.mantas.eternity.serializer.ComponentDeserializer;
import uk.me.mantas.eternity.serializer.ComponentDeserializer.NotDeserializedException;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;
import uk.me.mantas.eternity.tests.ExposedClass;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ComponentDeserializerTest extends TestHarness {
	private SharpSerializer mockSerializer (final Environment mockEnvironment)
		throws FileNotFoundException {

		final SharpSerializerFactory mockSerializerFactory = mock(SharpSerializerFactory.class);
		final SharpSerializer mockSerializer = mock(SharpSerializer.class);

		when(mockEnvironment.sharpSerializer()).thenReturn(mockSerializerFactory);
		when(mockSerializerFactory.forFile(anyString())).thenReturn(mockSerializer);

		return mockSerializer;
	}

	@Test
	public void testDeserializeNoObjectCount () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializer mockSerializer = mockSerializer(mockEnvironment);
		final ComponentDeserializer deserializer = new ComponentDeserializer("");

		when(mockSerializer.deserialize()).thenReturn(Optional.empty());
		assertFalse(deserializer.deserialize());
	}

	@Test
	public void testDeserialize () throws FileNotFoundException, NotDeserializedException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializer mockSerializer = mockSerializer(mockEnvironment);
		final SimpleProperty mockCount = mock(SimpleProperty.class);
		final Property mockProperty = mock(Property.class);
		final ComponentDeserializer deserializer = new ComponentDeserializer("");

		mockCount.obj = 2;

		when(mockSerializer.deserialize())
			.thenReturn(Optional.of(mockCount))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(mockProperty));

		assertTrue(deserializer.deserialize());
		assertSame(mockCount, deserializer.getCountProperty());
		assertEquals(1, deserializer.getComponents().size());
		assertSame(mockProperty, deserializer.getComponents().get(0));
	}

	@Test
	public void testReserializeThrowsException () throws FileNotFoundException {
		final ComponentDeserializer deserializer = new ComponentDeserializer("");
		boolean exceptionThrown = false;

		try {
			deserializer.reserialize();
		} catch (NotDeserializedException e) {
			exceptionThrown = true;
		}

		assertTrue(exceptionThrown);
	}

	@Test
	public void testReserialize () throws FileNotFoundException, NotDeserializedException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializer mockSerializer = mockSerializer(mockEnvironment);
		final File mockFile = mock(File.class);
		final Property mockProperty = mock(Property.class);
		final SimpleProperty mockCount = mock(SimpleProperty.class);
		final ComponentDeserializer deserializer = new ComponentDeserializer("");
		final ExposedClass exposed = expose(deserializer);

		when(mockFile.getAbsolutePath()).thenReturn("");

		exposed.set("deserializedState", true);
		exposed.set("countProperty", mockCount);
		exposed.set("components", new ArrayList<Property>() {{add(mockProperty);}});

		deserializer.reserialize();

		final InOrder inOrder = inOrder(mockSerializer);
		inOrder.verify(mockSerializer).serialize(mockCount);
		inOrder.verify(mockSerializer).serialize(mockProperty);
	}
}
