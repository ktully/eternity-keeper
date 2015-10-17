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
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.serializer.DeserializedPackets;
import uk.me.mantas.eternity.serializer.PacketDeserializer;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.FileNotFoundException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PacketDeserializerTest extends TestHarness {
	@Test
	public void testDeserializeNoObjectCount () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializer mockSerializer = mockSerializer(mockEnvironment);
		final PacketDeserializer deserializer = new PacketDeserializer("");

		when(mockSerializer.deserialize()).thenReturn(Optional.empty());
		assertFalse(deserializer.deserialize().isPresent());
	}

	@Test
	public void testDeserialize () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializer mockSerializer = mockSerializer(mockEnvironment);
		final SimpleProperty mockCount = mock(SimpleProperty.class);
		final Property mockProperty = mock(Property.class);
		final PacketDeserializer deserializer = new PacketDeserializer("");

		mockCount.obj = 2;

		when(mockSerializer.deserialize())
			.thenReturn(Optional.of(mockCount))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(mockProperty));

		final Optional<DeserializedPackets> deserialized = deserializer.deserialize();

		assertTrue(deserialized.isPresent());
		assertSame(mockCount, deserialized.get().getCount());
		assertEquals(1, deserialized.get().getPackets().size());
		assertSame(mockProperty, deserialized.get().getPackets().get(0));
	}
}
