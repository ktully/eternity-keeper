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
import uk.me.mantas.eternity.serializer.DeserializedPackets;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class DeserializedPacketsTest extends TestHarness {
	@Test
	public void testReserialize () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializer mockSerializer = mockSerializer(mockEnvironment);
		final File mockFile = mock(File.class);
		final Property mockProperty = mock(Property.class);
		final SimpleProperty mockCount = mock(SimpleProperty.class);
		final List<Property> components = new ArrayList<Property>() {{add(mockProperty);}};
		final DeserializedPackets deserialized =
			new DeserializedPackets(components, mockCount);

		when(mockFile.getAbsolutePath()).thenReturn("");
		deserialized.reserialize(mockFile);

		final InOrder inOrder = inOrder(mockSerializer);
		inOrder.verify(mockSerializer).serialize(mockCount);
		inOrder.verify(mockSerializer).serialize(mockProperty);
	}
}
