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


package uk.me.mantas.eternity.tests.serializer;

import org.junit.Test;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeserializerTest {
	@Test
	public void deserializesSaveFile ()
		throws URISyntaxException, FileNotFoundException {

		File saveFile =
			new File(
				this.getClass().getResource("/MobileObjects.save").toURI());

		SharpSerializer deserializer =
			new SharpSerializer(saveFile.getAbsolutePath());

		List<Property> deserialized = new ArrayList<>();

		Optional<Property> objectCount = deserializer.deserialize();
		assertTrue(objectCount.isPresent());

		int count = (int) objectCount.get().obj;
		assertEquals(17, count);

		for (int i = 0; i < count; i++) {
			Optional<Property> obj = deserializer.deserialize();
			assertTrue(obj.isPresent());
			deserialized.add(obj.get());
		}

		assertEquals(17, deserialized.size());
	}
}
