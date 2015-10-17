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

package uk.me.mantas.eternity.serializer;

import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// This is a specialised instance of the Deserializer when we know we are dealing with a file that
// has been serialized in PoE's particular format (i.e. the first object is a plain integer of the
// number of following ComponentPersistencePacket objects).

public class PacketDeserializer {
	private final File file;
	private final SharpSerializerFactory sharpSerializer;

	public PacketDeserializer (final File file) {
		this.file = file;
		sharpSerializer = Environment.getInstance().factory().sharpSerializer();
	}

	public PacketDeserializer (final String filename) {
		this(new File(filename));
	}

	public Optional<DeserializedPackets> deserialize () throws FileNotFoundException {
		final List<Property> deserialized = new ArrayList<>();
		final SharpSerializer deserializer = sharpSerializer.forFile(file.getAbsolutePath());
		final Optional<Property> objCountProp = deserializer.deserialize();

		if (!objCountProp.isPresent()) {
			return Optional.empty();
		}

		final int count = (int) objCountProp.get().obj;
		for (int i = 0; i < count; i++) {
			final Optional<Property> property = deserializer.deserialize();
			if (property.isPresent()) {
				deserialized.add(property.get());
			}
		}

		final SimpleProperty countProperty = (SimpleProperty) objCountProp.get();
		return Optional.of(new DeserializedPackets(deserialized, countProperty));
	}
}
