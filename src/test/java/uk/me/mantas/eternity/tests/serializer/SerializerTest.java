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

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class SerializerTest {
	@Test
	public void serializesSaveFile () throws URISyntaxException, IOException {
		final File saveFile = new File(getClass().getResource("/MobileObjects.save").toURI());
		final SharpSerializer deserializer = new SharpSerializer(saveFile.getAbsolutePath());
		final List<Property> deserialized = new ArrayList<>();
		final Optional<Property> objectCount = deserializer.deserialize();
		final int count = (int) objectCount.get().obj;

		for (int i = 0; i < count; i++) {
			final Optional<Property> obj = deserializer.deserialize();
			deserialized.add(obj.get());
		}

		final File saveOutputFile = Files.createTempFile(null, null).toFile();
		try {
			final SharpSerializer serializer =
				new SharpSerializer(saveOutputFile.getAbsolutePath());

			serializer.serialize(objectCount.get());
			for (final Property obj : deserialized) {
				serializer.serialize(obj);
			}

			final byte[] actual = FileUtils.readFileToByteArray(saveOutputFile);
			final byte[] expected = FileUtils.readFileToByteArray(saveFile);
			assertArrayEquals(expected, actual);
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			assertTrue(saveOutputFile.delete());
		}
	}
}
