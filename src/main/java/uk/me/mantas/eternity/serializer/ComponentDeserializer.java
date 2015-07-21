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

import uk.me.mantas.eternity.Environment;
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

public class ComponentDeserializer {
	private final File file;
	private final SharpSerializerFactory sharpSerializer;
	private boolean deserializedState = false;
	private List<Property> components;
	private SimpleProperty countProperty;

	public ComponentDeserializer (final File file) {
		this.file = file;
		sharpSerializer = Environment.getInstance().sharpSerializer();
	}

	public ComponentDeserializer (final String filename) {
		this(new File(filename));
	}

	public boolean deserialize () throws FileNotFoundException {
		final List<Property> deserialized = new ArrayList<>();
		final SharpSerializer deserializer = sharpSerializer.forFile(file.getAbsolutePath());
		final Optional<Property> objCountProp = deserializer.deserialize();

		if (!objCountProp.isPresent()) {
			return false;
		}

		final int count = (int) objCountProp.get().obj;
		for (int i = 0; i < count; i++) {
			final Optional<Property> property = deserializer.deserialize();
			if (property.isPresent()) {
				deserialized.add(property.get());
			}
		}

		countProperty = (SimpleProperty) objCountProp.get();
		components = deserialized;
		deserializedState = true;

		return true;
	}

	public List<Property> getComponents () throws NotDeserializedException {
		if (!deserializedState) {
			throw new NotDeserializedException();
		}

		return components;
	}

	public void setComponents (final List<Property> newComponents) throws NotDeserializedException {
		if (!deserializedState) {
			throw new NotDeserializedException();
		}

		components = newComponents;
	}

	public SimpleProperty getCountProperty () throws NotDeserializedException {
		if (!deserializedState) {
			throw new NotDeserializedException();
		}

		return countProperty;
	}

	public void reserialize () throws FileNotFoundException, NotDeserializedException {
		reserialize(file);
	}

	public void reserialize (final File destinationFile)
		throws FileNotFoundException
		, NotDeserializedException {

		if (!deserializedState) {
			throw new NotDeserializedException();
		}

		final SharpSerializer serializer =
			sharpSerializer.forFile(destinationFile.getAbsolutePath());

		serializer.serialize(countProperty);
		for (final Property property : components) {
			serializer.serialize(property);
		}
	}

	public static class NotDeserializedException extends Exception {
		public NotDeserializedException () {
			super("File has not been deserialized yet. Call deserialize() first.");
		}
	}
}
