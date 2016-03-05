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

import org.json.JSONObject;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.PacketDeserializerFactory;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.serializer.DeserializedPackets;
import uk.me.mantas.eternity.serializer.PacketDeserializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.me.mantas.eternity.game.UnityEngine.Vector3;

public class CharacterImporter {
	private static final Logger logger = Logger.getLogger(CharacterImporter.class);
	public final File saveFile;
	private final File chrFile;
	private final PacketDeserializerFactory packetDeserializer;

	public CharacterImporter (final String request, final String chrFilePath)
		throws FileNotFoundException {

		final Environment environment = Environment.getInstance();
		final JSONObject json = new JSONObject(request);
		final String oldSavePath = json.getString("oldSave");
		final boolean savedYet = json.getBoolean("savedYet");

		File saveFile = new File(oldSavePath);
		if (savedYet) {
			final File previouslySaved = environment.state().previousSaveDirectory();

			if (previouslySaved == null) {
				logger.error("Client reported we had already saved but directory didn't exist!%n");
			} else {
				saveFile = previouslySaved;
			}
		}

		if (!saveFile.exists()) {
			throw new FileNotFoundException(saveFile.getAbsolutePath());
		}

		final File chrFile = new File(chrFilePath);
		if (!chrFile.exists()) {
			throw new FileNotFoundException(chrFilePath);
		}

		this.saveFile = saveFile;
		this.chrFile = chrFile;

		packetDeserializer = environment.factory().packetDeserializer();
	}

	public boolean importCharacter () throws IOException {
		final PacketDeserializer chrDeserializer = packetDeserializer.forFile(chrFile);
		final Optional<DeserializedPackets> chrDeserialized = chrDeserializer.deserialize();
		if (!chrDeserialized.isPresent()) {
			return false;
		}

		final List<Property> chrObjects = chrDeserialized.get().getPackets();
		if (chrObjects.size() < 1) {
			return false;
		}

		final File mobileObjectsFile = new File(saveFile, "MobileObjects.save");
		final PacketDeserializer deserializer = packetDeserializer.forFile(mobileObjectsFile);
		final Optional<DeserializedPackets> deserialized = deserializer.deserialize();
		if (!deserialized.isPresent()) {
			logger.error("Unable to deserialize MobileObjects.save.%n");
			return false;
		}

		final List<Property> mobileObjects = deserialized.get().getPackets();
		if (mobileObjects.size() < 1) {
			logger.error("No objects in MobileObjects.save.%n");
			return false;
		}

		// We need to find a character in the existing save to 'anchor' the
		// imported character to, i.e. set their area and location co-ords.
		final Optional<ObjectPersistencePacket> anchorPoint = findAnchorPoint(mobileObjects);
		if (!anchorPoint.isPresent()) {
			logger.error("Unable to find anchor point in save.%n");
			return false;
		}

		final Optional<Property> characterProperty = findCharacter(chrObjects);
		if (!characterProperty.isPresent()) {
			logger.error("Unable to find character in CHR file.%n");
			return false;
		}

		final boolean anchored = anchorCharacter(characterProperty.get(), anchorPoint.get());
		if (!anchored) {
			logger.error("Unable to anchor character!%n");
			return false;
		}

		// Need to regenerate the GUID since it seems to be the same for all
		// player characters.
		final UUID newGUID = UUID.randomUUID();
		Property.update(characterProperty.get(), "GUID", newGUID);
		Property.update(characterProperty.get(), "ObjectID", newGUID.toString());

		final SimpleProperty simpleObjCount = deserialized.get().getCount();
		final int count = (int) simpleObjCount.obj;
		Property.update(simpleObjCount, count + chrObjects.size());

		final List<Property> totalObjects = new ArrayList<>();
		totalObjects.addAll(chrObjects);
		totalObjects.addAll(mobileObjects);

		if (!mobileObjectsFile.delete()) {
			logger.error("Unable to delete '%s'.%n", mobileObjectsFile.getAbsolutePath());
			return false;
		}

		if (!mobileObjectsFile.createNewFile()) {
			logger.error("Unable to create '%s'.%n", mobileObjectsFile.getAbsolutePath());
			return false;
		}

		deserialized.get().setPackets(totalObjects);
		deserialized.get().reserialize(mobileObjectsFile);

		return true;
	}

	boolean anchorCharacter (
		Property chrProperty
		, ObjectPersistencePacket anchorPoint) {

		String areaName = anchorPoint.LevelName;
		Vector3 location = anchorPoint.Location;

		// Just naively try to put the character somewhere near the anchor
		// point. Revisit this if it causes problems.
		Vector3 newLocation = new Vector3();
		newLocation.x = location.x + 1;
		newLocation.y = location.y + 1;
		newLocation.z = location.z;

		boolean updatedAreaName =
			Property.update(chrProperty, "LevelName", areaName);

		if (!updatedAreaName) {
			return false;
		}

		Optional<Property> locationProperty =
			Property.find(chrProperty, "Location");

		if (!locationProperty.isPresent()) {
			logger.error(
				"Unable to find Location property of character object!%n");
			return false;
		}

		boolean updatedLocation =
			Property.update(locationProperty.get(), "x", newLocation.x);

		updatedLocation = updatedLocation
			&& Property.update(locationProperty.get(), "y", newLocation.y);

		updatedLocation = updatedLocation
			&& Property.update(locationProperty.get(), "z", newLocation.z);

		if (!updatedLocation) {
			logger.error("Unable to locate character's location!%n");
			return false;
		}

		return true;
	}

	private Optional<Property> findCharacter (List<Property> chrObjects) {
		for (Property property : chrObjects) {
			ObjectPersistencePacket packet =
				(ObjectPersistencePacket) property.obj;

			if (packet.ObjectName.startsWith("Player_")
				|| packet.ObjectName.startsWith("Companion_")) {

				return Optional.of(property);
			}
		}

		return Optional.empty();
	}

	private Optional<ObjectPersistencePacket> findAnchorPoint (
		List<Property> mobileObjects) {

		for (Property property : mobileObjects) {
			ObjectPersistencePacket packet =
				(ObjectPersistencePacket) property.obj;

			if (packet.ObjectName.startsWith("Player_")) {
				return Optional.of(packet);
			}
		}

		return Optional.empty();
	}
}
