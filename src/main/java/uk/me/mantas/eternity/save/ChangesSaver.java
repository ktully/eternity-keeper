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

import com.google.common.primitives.UnsignedInteger;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.cef.callback.CefQueryCallback;
import org.joox.Match;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.PacketDeserializerFactory;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.handlers.SaveChanges;
import uk.me.mantas.eternity.serializer.DeserializedPackets;
import uk.me.mantas.eternity.serializer.PacketDeserializer;
import uk.me.mantas.eternity.serializer.properties.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static org.joox.JOOX.$;
import static uk.me.mantas.eternity.EKUtils.findSubComponent;
import static uk.me.mantas.eternity.EKUtils.unwrapPacket;

public class ChangesSaver implements Runnable {
	private static final Logger logger = Logger.getLogger(ChangesSaver.class);
	private final CefQueryCallback callback;
	private final JSONObject request;
	private final PacketDeserializerFactory packetDeserializer;

	public ChangesSaver (final String request, final CefQueryCallback callback) {
		this.callback = callback;
		this.request = new JSONObject(request);
		packetDeserializer = Environment.getInstance().factory().packetDeserializer();
	}

	@Override
	public void run () {
		final Environment environment = Environment.getInstance();
		try {
			boolean savedYet = request.getBoolean("savedYet");
			final String saveName = request.getString("saveName");
			final String absolutePath = request.getString("absolutePath");
			final JSONObject saveData = request.getJSONObject("saveData");

			File saveDirectory = environment.state().previousSaveDirectory();
			if (savedYet && saveDirectory == null) {
				logger.error(
					"Client claimed we had already saved but "
					+ "server had no record having saved previously.%n");

				savedYet = false;
			}

			if (!savedYet) {
				saveDirectory = createNewSave(absolutePath);
				environment.state().previousSaveDirectory(saveDirectory);
			}

			updateSaveInfo(saveDirectory, saveName);
			updateMobileObjects(saveDirectory, saveData);
			packageSaveGame(saveDirectory);
			callback.success("{\"success\":true}");
		} catch (final JSONException e) {
			callback.failure(-1, SaveChanges.jsonError());
		} catch (final IOException | ZipException e) {
			logger.error("%s%n", e.getMessage());
			callback.failure(-1, SaveChanges.ioError());
		} catch (final DeserializationException e) {
			logger.error("Unable to deserialize new save.%n");
			callback.failure(-1, SaveChanges.deserializationError());
		}
	}

	private void packageSaveGame (File saveDirectory) throws ZipException {
		File pillarsSavesDirectory;
		try {
			pillarsSavesDirectory = new File(
				Settings.getInstance().json.getString("savesLocation"));
		} catch (JSONException e) {
			logger.error(
				"Unable to determine Pillars of Eternity "
					+ "save game location!%n");

			return;
		}

		File saveFile =
			new File(pillarsSavesDirectory, saveDirectory.getName());

		if (!FileUtils.deleteQuietly(saveFile)) {
			logger.error(
				"Unable to delete old save game '%s'!%n"
				, saveFile.getAbsolutePath());
		}

		File[] saveContents = saveDirectory.listFiles();
		if (saveContents == null) {
			logger.error(
				"Save directory '%s' is empty!%n"
				, saveDirectory.getAbsolutePath());

			return;
		}

		ZipFile saveArchive = new ZipFile(saveFile);
		saveArchive.createZipFile(
			new ArrayList<>(Arrays.asList(saveContents))
			, new ZipParameters());
	}

	private void updateMobileObjects (final File saveDirectory, final JSONObject saveData)
		throws IOException, DeserializationException {

		final File mobileObjectsFile = new File(saveDirectory, "MobileObjects.save");
		final PacketDeserializer deserializer = packetDeserializer.forFile(mobileObjectsFile);
		final Optional<DeserializedPackets> deserialized = deserializer.deserialize();

		if (!deserialized.isPresent()) {
			throw new DeserializationException();
		}

		final List<Property> updatedMobileObjects =
			deserialized.get().getPackets().stream().map(
				packet -> updateMobileObject(packet, saveData))
			.collect(Collectors.toList());

		deserialized.get().setPackets(updatedMobileObjects);

		if (!mobileObjectsFile.delete()) {
			logger.error(
				"Unable to remove old MobileObjects.save at '%s'!%n"
				, mobileObjectsFile.getAbsolutePath());

			return;
		}

		Files.createFile(mobileObjectsFile.toPath());
		deserialized.get().reserialize(mobileObjectsFile);
	}

	private Property updateMobileObject (final Property property, final JSONObject saveData) {
		final ObjectPersistencePacket packet = unwrapPacket(property);
		final JSONArray characters = saveData.getJSONArray("characters");
		final float currency = (float) saveData.getDouble("currency");

		// TODO: Refactor out this check for the 'player' object.
		if (packet.ObjectName.toLowerCase().startsWith("player_")) {
			updateCurrency((ComplexProperty) property, currency);
		}

		for (int i = 0; i < characters.length(); i++) {
			final JSONObject character = characters.getJSONObject(i);
			if (character.getString("GUID").equals(packet.ObjectID)) {
				updateCharacter((ComplexProperty) property, character.getJSONObject("stats"));
				break;
			}
		}

		return property;
	}

	private void updateCurrency (final ComplexProperty root, final float currency) {
		final Optional<ComplexProperty> currencyValue =
			root.<SingleDimensionalArrayProperty>findProperty("ComponentPackets")
			.flatMap(components -> findSubComponent(components, "PlayerInventory"))
			.flatMap(playerInventory ->
				playerInventory.<DictionaryProperty>findProperty("Variables"))
			.flatMap(variables -> variables.<ComplexProperty>findEntry("currencyTotalValue"));

		if (!currencyValue.isPresent()) {
			logger.error("Unable to navigate property structure when updating currency!%n");
			return;
		}

		Property.update(currencyValue.get(), "v", currency);
	}

	private void updateCharacter (final ComplexProperty root, final JSONObject character) {
		// Having to do linear searches through everything multiple times
		// sucks for performance efficiency however that's our only option as
		// we have to use Property-first serialization. If profiling later shows
		// that this is taking too long then we may need to do some optimising.

		final Optional<DictionaryProperty> variables =
			root.<SingleDimensionalArrayProperty>findProperty("ComponentPackets")
			.flatMap(components -> findSubComponent(components, "CharacterStats"))
			.flatMap(characterStats ->
				characterStats.<DictionaryProperty>findProperty("Variables"));

		if (!variables.isPresent()) {
			logger.error("Unable to navigate property structure when updating character!%n");
			return;
		}

		for (final String updateKey : character.keySet()) {
			final String updateValue = character.getJSONObject(updateKey).getString("value");
			final Optional<SimpleProperty> savedValue =	variables.get().<SimpleProperty>findEntry(updateKey);

			if (!savedValue.isPresent()) {
				logger.error(
					"Wanted to update character stat %s but could not find it in saved data!%n"
					, updateKey);

				continue;
			}

			try {
				final Object typedValue = castValue(savedValue.get().obj, updateValue);
				Property.update(savedValue.get(), typedValue);
			} catch (final NumberFormatException e) {
				logger.error(
					"Unable to save value '%s' in character stat '%s'."
					, updateValue
					, updateKey);
			}
		}
	}

	private static Object castValue (final Object primitive, final String val) {
		final String cls = primitive.getClass().getSimpleName();

		if (cls.equals("int") || cls.equals("Integer")) {
			return Integer.parseInt(val);
		}

		if (cls.equals("double") || cls.equals("Double")) {
			return Double.parseDouble(val);
		}

		if (cls.equals("float") || cls.equals("Float")) {
			return Float.parseFloat(val);
		}

		if (cls.equals("boolean") || cls.equals("Boolean")) {
			return Boolean.parseBoolean(val);
		}

		if (cls.equals("UnsignedInteger")) {
			return UnsignedInteger.valueOf(val);
		}

		if (primitive.getClass().isEnum()) {
			for (final Object constant : primitive.getClass().getEnumConstants()) {
				final Optional<String> constantName = EKUtils.enumConstantName(constant);
				if (constantName.isPresent() && constantName.get().equals(val)) {
					return constant;
				}
			}

			logger.error(
				"Client returned non-existent enum value '%s' for class %s."
				, val
				, primitive.getClass().getName());
			return primitive.getClass().getEnumConstants()[0];
		}

		return val;
	}

	private void updateSaveInfo (File saveDirectory, String saveName)
		throws IOException {

		File saveinfoXML = new File(saveDirectory, "saveinfo.xml");
		String contents = new String(
			EKUtils.removeBOM(FileUtils.readFileToByteArray(saveinfoXML))
			, "UTF-8");

		ByteArrayOutputStream newContentsStream = new ByteArrayOutputStream(contents.length());
		try {
			Match xml = $(contents);
			xml.find("Simple[name='UserSaveName']").attr("value", saveName);
			xml.write(newContentsStream);
		} catch (DOMException e) {
			logger.error(
				"Error parsing copied saveinfo '%s': %s%n"
				, saveinfoXML.getAbsolutePath()
				, e.getMessage());
		}

		String newContents = newContentsStream.toString("UTF-8");
		byte[] newContentsBytes = newContents.getBytes();
		if (newContentsBytes[0] != -17) {
			newContentsBytes = EKUtils.addBOM(newContentsBytes);
		}

		FileUtils.writeByteArrayToFile(saveinfoXML, newContentsBytes, false);
	}

	private File createNewSave (final String absolutePath) throws IOException {
		final Environment environment = Environment.getInstance();
		final File workingDirectory = environment.directory().working();
		final File oldSave = new File(absolutePath);
		final String sessionID = oldSave.getName().split(" ")[0].replace("-", "");

		final int gameID = getAvailableGameID(workingDirectory, sessionID);
		final File newDirectory = createNewSaveDirectory(
			workingDirectory
			, oldSave.getName()
			, sessionID
			, gameID);

		FileUtils.copyDirectory(oldSave, newDirectory);
		return newDirectory;
	}

	private File createNewSaveDirectory (
		File workingDirectory
		, String oldSaveName
		, String sessionID
		, int gameID)
		throws IOException {

		String sceneTitle =
			oldSaveName.substring(oldSaveName.lastIndexOf(" ") + 1);

		File newSaveDirectory = new File(
			workingDirectory
			, String.format("%s %d %s", sessionID, gameID, sceneTitle));

		if (!newSaveDirectory.mkdir()) {
			throw new IOException();
		}

		return newSaveDirectory;
	}

	private int getAvailableGameID (File workingDirectory, String sessionID) {
		// Games are saved with the session ID, followed by a space, followed
		// by some number that I'm not sure about yet but is perhaps game time.

		// We call that number the 'game ID' in this case as it distinguishes
		// different games under the same session ID from each other. The game
		// actually doesn't care if this number is correct, just that it is
		// unique so we try to find a unique one in this method.

		int candidateID = 0;
		File[] existingSaves = workingDirectory.listFiles();

		if (existingSaves == null) {
			return candidateID;
		}

		Set<String> existingIDs =
			Arrays.stream(existingSaves)
				.filter(s -> s.getName().startsWith(sessionID))
				.map(s -> s.getName().split(" ")[1])
				.collect(Collectors.toSet());

		while (existingIDs.contains(String.format("%d", candidateID))) {
			candidateID++;
		}

		return candidateID;
	}

	private static class DeserializationException extends Exception {
		DeserializationException () {
			super();
		}
	}
}
