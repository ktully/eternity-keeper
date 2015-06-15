package uk.me.mantas.eternity.save;

import org.json.JSONObject;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.serializer.SharpSerializer;
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
	public final File saveFile;
	private final File chrFile;

	public CharacterImporter (String request, String chrFilePath)
		throws FileNotFoundException {

		JSONObject json = new JSONObject(request);
		boolean savedYet = json.getBoolean("savedYet");
		String oldSavePath = json.getString("oldSave");

		File saveFile = new File(oldSavePath);
		if (savedYet) {
			File previouslySaved =
				Environment.getInstance().getPreviousSaveDirectory();

			if (previouslySaved == null) {
				System.err.printf(
					"Client reported we had already saved "
					+ "but directory didn't exist!%n");
			} else {
				saveFile = previouslySaved;
			}
		}

		if (!saveFile.exists()) {
			throw new FileNotFoundException(saveFile.getAbsolutePath());
		}

		File chrFile = new File(chrFilePath);
		if (!chrFile.exists()) {
			throw new FileNotFoundException(chrFilePath);
		}

		this.saveFile = saveFile;
		this.chrFile = chrFile;
	}

	public boolean importCharacter () throws IOException {
		List<Property> chrObjects = EKUtils.deserializeFile(chrFile);
		if (chrObjects.size() < 1) {
			return false;
		}

		File mobileObjectsFile = new File(saveFile, "MobileObjects.save");
		SharpSerializer deserializer =
			new SharpSerializer(mobileObjectsFile.getAbsolutePath());

		List<Property> mobileObjects = new ArrayList<>();
		Optional<Property> objCount = deserializer.deserialize();
		if (!objCount.isPresent()) {
			System.err.printf("Unable to deserialize MobileObjects.save.%n");
			return false;
		}

		int count = (int) objCount.get().obj;
		for (int i = 0; i < count; i++) {
			Optional<Property> mobileObjectProperty =
				deserializer.deserialize();

			if (mobileObjectProperty.isPresent()) {
				mobileObjects.add(mobileObjectProperty.get());
			}
		}

		if (mobileObjects.size() < 1) {
			System.err.printf("No objects in MobileObjects.save.%n");
			return false;
		}

		// We need to find a character in the existing save to 'anchor' the
		// imported character to, i.e. set their area and location co-ords.
		Optional<ObjectPersistencePacket> anchorPoint =
			findAnchorPoint(mobileObjects);

		if (!anchorPoint.isPresent()) {
			System.err.printf("Unable to find anchor point in save.%n");
			return false;
		}

		Optional<Property> characterProperty = findCharacter(chrObjects);
		if (!characterProperty.isPresent()) {
			System.err.printf("Unable to find character in CHR file.%n");
			return false;
		}

		boolean anchored =
			anchorCharacter(characterProperty.get(), anchorPoint.get());

		if (!anchored) {
			System.err.printf("Unable to anchor character!%n");
			return false;
		}

		// Need to regenerate the GUID since it seems to be the same for all
		// player characters.
		UUID newGUID = UUID.randomUUID();
		Property.update(characterProperty.get(), "GUID", newGUID);
		Property.update(
			characterProperty.get()
			, "ObjectID"
			, newGUID.toString());

		SimpleProperty simpleObjCount = (SimpleProperty) objCount.get();
		simpleObjCount.value = count + chrObjects.size();
		simpleObjCount.obj = count + chrObjects.size();

		List<Property> totalObjects = new ArrayList<>();
		totalObjects.addAll(chrObjects);
		totalObjects.addAll(mobileObjects);

		if (!mobileObjectsFile.delete()) {
			System.err.printf(
				"Unable to delete '%s'.%n"
				, mobileObjectsFile.getAbsolutePath());

			return false;
		}

		if (!mobileObjectsFile.createNewFile()) {
			System.err.printf(
				"Unable to create '%s'.%n"
				, mobileObjectsFile.getAbsolutePath());

			return false;
		}

		reserialize(mobileObjectsFile, simpleObjCount, totalObjects);
		return true;
	}

	private void reserialize (
		File mobileObjectsFile
		, Property count
		, List<Property> totalObjects)
		throws FileNotFoundException {

		SharpSerializer serializer =
			new SharpSerializer(mobileObjectsFile.getAbsolutePath());

		serializer.serialize(count);
		for (Property property : totalObjects) {
			serializer.serialize(property);
		}
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
			System.err.printf(
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
			System.err.printf("Unable to locate character's location!%n");
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
