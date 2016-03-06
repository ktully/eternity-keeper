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

import org.apache.commons.io.FileUtils;
import org.cef.callback.CefQueryCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.PacketDeserializerFactory;
import uk.me.mantas.eternity.game.ComponentPersistencePacket;
import uk.me.mantas.eternity.game.CurrencyValue;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.handlers.OpenSavedGame;
import uk.me.mantas.eternity.serializer.DeserializedPackets;
import uk.me.mantas.eternity.serializer.PacketDeserializer;
import uk.me.mantas.eternity.serializer.properties.Property;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry;
import static uk.me.mantas.eternity.EKUtils.*;

public class SavedGameOpener implements Runnable {
	private static final Logger logger = Logger.getLogger(SavedGameOpener.class);
	private final String saveGameLocation;
	private final CefQueryCallback callback;
	private final PacketDeserializerFactory packetDeserializer;

	public SavedGameOpener (final String saveGameLocation, final CefQueryCallback callback) {
		this.saveGameLocation = saveGameLocation;
		this.callback = callback;
		packetDeserializer = Environment.getInstance().factory().packetDeserializer();
	}

	@Override
	public void run () {
		final File savedgame = new File(saveGameLocation);
		final File mobileObjectsFile = new File(savedgame, "MobileObjects.save");

		if (!mobileObjectsFile.exists()) {
			OpenSavedGame.notExists(callback);
			return;
		}

		final List<Property> gameObjects =
			deserialize(mobileObjectsFile).stream()
				.filter(this::isObjectPersistencePacket)
				.filter(this::hasObjectName)
				.collect(Collectors.toList());

		final Map<String, Property> characters = extractCharacters(gameObjects);
		//final Map<String, Integer> globals = extractGlobals(gameObjects);
		final float currency = extractCurrency(gameObjects);
		//sendJSON(currency, globals, characters);
		sendJSON(currency, characters);
	}

	private boolean isObjectPersistencePacket (final Property property) {
		return property.obj instanceof ObjectPersistencePacket;
	}

	private boolean hasObjectName (final Property property) {
		final ObjectPersistencePacket packet = (ObjectPersistencePacket) property.obj;
		return packet.ObjectName != null;
	}

	private float extractCurrency (final List<Property> gameObjects) {
		final Optional<Property> playerProperty =
			findProperty(gameObjects, objectName -> objectName.startsWith("Player_"));

		if (!playerProperty.isPresent()) {
			logger.error("Unable to find player mobile object.%n");
			return 0f;
		}

		final ObjectPersistencePacket playerPacket = unwrapPacket(playerProperty.get());
		final Optional<ComponentPersistencePacket> inventoryComponent =
			findComponent(playerPacket.ComponentPackets, "PlayerInventory");

		if (!inventoryComponent.isPresent()) {
			logger.error("Unable to find PlayerInventory component.");
			return 0f;
		}

		final Object currencyValue = inventoryComponent.get().Variables.get("currencyTotalValue");
		if (currencyValue == null) {
			logger.error("Unable to find currencyTotalValue in PlayerInventory component.");
			return 0f;
		}

		return ((CurrencyValue) currencyValue).v;
	}

	private Optional<JSONObject> charactersToJSON (final Entry<String, Property> entry) {
		final JSONObject jsonObject = new JSONObject();
		jsonObject.put("GUID", entry.getKey());

		final Property property = entry.getValue();
		final ObjectPersistencePacket packet = (ObjectPersistencePacket) property.obj;
		final boolean isCompanion = detectCompanion(packet);
		final boolean isDead = detectDead(packet);
		String name = extractName(packet);

		final Optional<Map<String, Object>> stats = extractCharacterStats(packet);
		if (!stats.isPresent()) {
			// This is a stored character that is not presently in the party.
			return Optional.empty();
		}

		if (stats.get().get("OverrideName") != null
			&& !stats.get().get("OverrideName").equals("")) {

			name = (String) stats.get().get("OverrideName");
		} else if (isCompanion) {
			final String mappedName =
				Environment.getInstance().config().companionNameMap().get(name);

			if (mappedName != null) {
				name = mappedName;
			}

			stats.get().put("OverrideName", name);
		}

		jsonObject.put("isCompanion", isCompanion);
		jsonObject.put("isDead", isDead);
		jsonObject.put("name", name);
		jsonObject.put("portrait", extractPortrait(packet, isCompanion));
		jsonObject.put("stats", stats.get());

		return Optional.of(jsonObject);
	}

	private void sendJSON (
		final float currency
		//, final Map<String, Integer> globals
		, final Map<String, Property> characters) {

		final JSONObject json = new JSONObject();
		json.put("currency", currency);

//		final JSONObject[] jsonGlobals = globals.entrySet().stream().map(this::globalsToJSON);
//		json.put("globals", new JSONArray(jsonGlobals));

		final JSONObject[] jsonCharacters =
			characters.entrySet().stream()
				.map(this::charactersToJSON)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toArray(JSONObject[]::new);

		json.put("characters", new JSONArray(jsonCharacters));
		callback.success(json.toString());
	}

	private boolean detectCompanion (final ObjectPersistencePacket packet) {
		final String objectName = packet.ObjectName.toLowerCase();
		return objectName.startsWith("companion_") && !objectName.startsWith("companion_generic");
	}

	private Optional<Map<String, Object>> extractCharacterStats (
		final ObjectPersistencePacket packet) {

		return findComponent(packet.ComponentPackets, "CharacterStats")
			.map(c ->
				c.Variables.entrySet().stream()
					.filter(entry -> isSupportedType(entry.getValue()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
	}

	private boolean isSupportedType (Object obj) {
		String cls = obj.getClass().getSimpleName();
		return cls.equals("int") || cls.equals("Integer")
			|| cls.equals("float") || cls.equals("Float")
			|| cls.equals("double") || cls.equals("Double")
			|| cls.equals("boolean") || cls.equals("Boolean")
			|| cls.equals("String");
	}

	private String extractName (ObjectPersistencePacket packet) {
		String name = "";
		if (packet.ObjectName.contains("_")) {
			name = packet.ObjectName.split("_")[1];
			if (name.contains("(")) {
				name = name.split("\\(")[0];
			}
		}

		return name;
	}

	private boolean detectDead (final ObjectPersistencePacket packet) {
		final Optional<Float> currentHealth =
			findComponent(packet.ComponentPackets, "Health")
				.map(c -> (Float) c.Variables.get("CurrentHealth"));

		return currentHealth.isPresent() && currentHealth.get() == 0f;
	}

	private String extractPortrait (
		final ObjectPersistencePacket packet
		, final boolean isCompanion) {

		final JSONObject settings = Settings.getInstance().json;
		Optional<String> portraitSubPath =
			findComponent(packet.ComponentPackets, "Portrait")
			.map(c -> (String) c.Variables.get("m_textureLargePath"));

		if (isCompanion) {
			final String name = extractName(packet);
			String mappedName = Environment.getInstance().config().companionNameMap().get(name);
			mappedName = (mappedName == null) ? null : mappedName.toLowerCase().replace(" ", "_");

			portraitSubPath = Optional.of(
				String.format(
					Environment.getInstance().config().companionPortraitPath()
					, (mappedName == null) ? name.toLowerCase() : mappedName));
		}

		if (!portraitSubPath.isPresent()) {
			return "";
		}

		final String installationPath;
		try {
			installationPath = settings.getString("gameLocation");
		} catch (final JSONException e) {
			return "";
		}

		final Path portraitPath =
			Paths.get(installationPath)
				.resolve(Environment.getInstance().config().pillarsDataDirectory())
				.resolve(portraitSubPath.get())
				.normalize();

		if (!portraitPath.toFile().exists()) {
			logger.error(
				"Game files contained reference to portrait at '%s' "
				+ "but it didn't exist.%n", portraitPath.toString());

			return "";
		}

		try {
			final byte[] portraitData = FileUtils.readFileToByteArray(portraitPath.toFile());
			return Base64.getEncoder().encodeToString(portraitData);
		} catch (final IOException e) {
			logger.error(
				"Unable to open portrait file '%s': %s%n"
				, portraitPath.toString()
				, e.getMessage());
		}

		return "";
	}

	private Map<String, Property> extractCharacters (final List<Property> gameObjects) {
		final Map<String, Property> characters = new HashMap<>();
		for (final Property property : gameObjects) {
			final ObjectPersistencePacket packet = (ObjectPersistencePacket) property.obj;
			final String objectName = packet.ObjectName.toLowerCase();

			if (packet.ObjectID != null
				&& (objectName.startsWith("player_")
					|| objectName.startsWith("companion_"))) {

				characters.put(packet.ObjectID, property);
			}
		}

		return characters;
	}

	private List<Property> deserialize (final File mobileObjectsFile) {
		final PacketDeserializer deserializer = packetDeserializer.forFile(mobileObjectsFile);
		List<Property> objects = new ArrayList<>();

		try {
			final Optional<DeserializedPackets> deserialized = deserializer.deserialize();
			if (!deserialized.isPresent()) {
				OpenSavedGame.deserializationError(callback);
				return objects;
			}

			objects = deserialized.get().getPackets();
		} catch (final FileNotFoundException | IndexOutOfBoundsException e) {
			OpenSavedGame.deserializationError(callback);
		}

		return objects;
	}
}
