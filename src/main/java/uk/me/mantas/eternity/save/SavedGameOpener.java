package uk.me.mantas.eternity.save;

import org.cef.callback.CefQueryCallback;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.handlers.OpenSavedGame;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SavedGameOpener implements Runnable {
	private final String saveGameLocation;
	private final CefQueryCallback callback;

	public SavedGameOpener (
		String saveGameLocation
		, CefQueryCallback callback) {

		this.saveGameLocation = saveGameLocation;
		this.callback = callback;
	}

	@Override
	public void run () {
		File savedgame = new File(saveGameLocation);
		File mobileObjectsFile = new File(savedgame, "MobileObjects.save");

		if (!mobileObjectsFile.exists()) {
			OpenSavedGame.notExists(callback);
			return;
		}

		List<Property> gameObjects = deserialize(mobileObjectsFile);
		Map<String, Property> characters =
			extractCharacters(gameObjects);

		Environment.getInstance().setCharacterCache(characters);
		sendJSON(characters);
	}

	private void sendJSON (Map<String, Property> characters) {
		JSONObject[] jsonObjects = characters.entrySet().stream().map(entry -> {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("GUID", entry.getKey());
			Property property = entry.getValue();
			ObjectPersistencePacket packet =
				(ObjectPersistencePacket) property.obj;

			return jsonObject;
		}).toArray(JSONObject[]::new);

		String json = new JSONArray(jsonObjects).toString();
		callback.success(json);
	}

	private Map<String, Property> extractCharacters (
		List<Property> gameObjects) {

		Map<String, Property> characters = new HashMap<>();
		for (Property property : gameObjects) {
			if (!(property.obj instanceof ObjectPersistencePacket)) {
				continue;
			}

			ObjectPersistencePacket packet =
				(ObjectPersistencePacket) property.obj;

			if (packet.ObjectName == null || packet.ObjectID == null) {
				continue;
			}

			if (packet.ObjectName.startsWith("Player_")
				|| packet.ObjectName.startsWith("Companion_")) {

				characters.put(packet.ObjectID, property);
			}
		}

		return characters;
	}

	private List<Property> deserialize (File mobileObjectsFile) {
		List<Property> objects = new ArrayList<>();

		try {
			SharpSerializer deserializer =
				new SharpSerializer(mobileObjectsFile.getAbsolutePath());

			Optional<Property> objectCount = deserializer.deserialize();
			if (!objectCount.isPresent()) {
				OpenSavedGame.deserializationError(callback);
				return objects;
			}

			int count = (int) objectCount.get().obj;
			for (int i = 0; i < count; i++) {
				Optional<Property> gameObject = deserializer.deserialize();
				if (gameObject.isPresent()) {
					objects.add(gameObject.get());
				}
			}
		} catch (FileNotFoundException e) {
			OpenSavedGame.deserializationError(callback);
		}

		return objects;
	}
}
