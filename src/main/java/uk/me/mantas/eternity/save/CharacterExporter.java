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


package uk.me.mantas.eternity.save;

import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.PacketDeserializerFactory;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.serializer.DeserializedPackets;
import uk.me.mantas.eternity.serializer.PacketDeserializer;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CharacterExporter {
	private final File saveDirectory;
	private final File chrFile;
	private final String guid;

	private final PacketDeserializerFactory packetDeserializer;
	private final SharpSerializerFactory sharpSerializer;

	public CharacterExporter (
		final String savePath
		, final String guid
		, final String chrFileName)
		throws IOException {

		final File saveDirectory = new File(savePath);
		if (!saveDirectory.exists()) {
			throw new FileNotFoundException(savePath);
		}

		final File chrFile = new File(chrFileName);
		//noinspection ResultOfMethodCallIgnored
		chrFile.delete();

		if (!chrFile.createNewFile()) {
			throw new FileNotFoundException("Cannot create " + chrFileName);
		}

		this.saveDirectory = saveDirectory;
		this.chrFile = chrFile;
		this.guid = guid;

		final Environment environment = Environment.getInstance();
		packetDeserializer = environment.factory().packetDeserializer();
		sharpSerializer = environment.factory().sharpSerializer();
	}

	public boolean export () throws FileNotFoundException {
		final File mobileObjectsFile = new File(saveDirectory, "MobileObjects.save");
		if (!mobileObjectsFile.exists()) {
			throw new FileNotFoundException(mobileObjectsFile.getAbsolutePath());
		}

		final PacketDeserializer deserializer = packetDeserializer.forFile(mobileObjectsFile);
		final Optional<DeserializedPackets> deserialized = deserializer.deserialize();
		if (!deserialized.isPresent()) {
			return false;
		}

		final List<Property> mobileObjects = deserialized.get().getPackets();
		final SimpleProperty count = deserialized.get().getCount();
		final List<Property> extractedObjects = extractCharactersObjects(mobileObjects);
		if (extractedObjects.size() < 1) {
			return false;
		}

		SharpSerializer serializer = sharpSerializer.forFile(chrFile.getAbsolutePath());
		Property.update(count, extractedObjects.size());
		serializer.serialize(count);

		for (Property property : extractedObjects) {
			serializer.serialize(property);
		}

		return true;
	}

	private List<Property> extractCharactersObjects (
		List<Property> mobileObjects) {

		List<Property> objs = new ArrayList<>();
		Optional<String> objectName = getObjectNameForGUID(mobileObjects);

		if (!objectName.isPresent()) {
			return objs;
		}

		for (Property property : mobileObjects) {
			ObjectPersistencePacket packet =
				(ObjectPersistencePacket) property.obj;

			if (packet.ObjectID.equals(guid)
				|| packet.Parent.equals(objectName.get())) {

				objs.add(property);
			}
		}

		return objs;
	}

	private Optional<String> getObjectNameForGUID (
		List<Property> mobileObjects) {

		for (Property property : mobileObjects) {
			ObjectPersistencePacket packet =
				(ObjectPersistencePacket) property.obj;

			if (packet.ObjectID.equals(guid)) {
				return Optional.of(packet.ObjectName);
			}
		}

		return Optional.empty();
	}
}
