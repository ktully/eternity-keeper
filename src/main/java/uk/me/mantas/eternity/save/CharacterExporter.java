package uk.me.mantas.eternity.save;

import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CharacterExporter {
	private final File saveDirectory;
	private final File chrFile;
	private final String guid;

	public CharacterExporter (
		String savePath
		, String guid
		, String chrFileName)
		throws FileNotFoundException {

		File saveDirectory = new File(savePath);
		if (!saveDirectory.exists()) {
			throw new FileNotFoundException(savePath);
		}

		File chrFile = new File(chrFileName);

		this.saveDirectory = saveDirectory;
		this.chrFile = chrFile;
		this.guid = guid;
	}

	public boolean export () throws FileNotFoundException {
		File mobileObjectsFile = new File(saveDirectory, "MobileObjects.save");
		if (!mobileObjectsFile.exists()) {
			throw new FileNotFoundException(
				mobileObjectsFile.getAbsolutePath());
		}

		SharpSerializer deserializer =
			new SharpSerializer(mobileObjectsFile.getAbsolutePath());

		Optional<Property> numObjectsProp = deserializer.deserialize();
		if (!numObjectsProp.isPresent()) {
			return false;
		}

		List<Property> mobileObjects = new ArrayList<>();
		int count = (int) numObjectsProp.get().obj;

		for (int i = 0; i < count; i++) {
			Optional<Property> property = deserializer.deserialize();
			if (property.isPresent()) {
				mobileObjects.add(property.get());
			}
		}

		List<Property> extractedObjects =
			extractCharactersObjects(mobileObjects);

		if (extractedObjects.size() < 1) {
			return false;
		}

		SharpSerializer serializer =
			new SharpSerializer(chrFile.getAbsolutePath());

		numObjectsProp.get().obj = extractedObjects.size();
		((SimpleProperty) numObjectsProp.get()).value = extractedObjects.size();
		serializer.serialize(numObjectsProp.get());

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
