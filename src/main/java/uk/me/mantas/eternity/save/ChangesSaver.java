package uk.me.mantas.eternity.save;

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
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.game.ComponentPersistencePacket;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.handlers.SaveChanges;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.joox.JOOX.$;

public class ChangesSaver implements Runnable {
	private final CefQueryCallback callback;
	private final JSONObject request;

	public ChangesSaver (String request, CefQueryCallback callback) {
		this.callback = callback;
		this.request = new JSONObject(request);
	}

	@Override
	public void run () {
		Environment environment = Environment.getInstance();
		try {
			boolean savedYet = request.getBoolean("savedYet");
			String saveName = request.getString("saveName");
			String absolutePath = request.getString("absolutePath");
			JSONArray characterData = request.getJSONArray("characterData");

			File saveDirectory = environment.getPreviousSaveDirectory();
			if (savedYet && saveDirectory == null) {
				System.err.printf(
					"Client claimed we had already saved but "
					+ "server had no record having saved previously.%n");

				savedYet = false;
			}

			if (!savedYet) {
				saveDirectory = createNewSave(absolutePath);
				environment.setPreviousSaveDirectory(saveDirectory);
			}

			updateSaveInfo(saveDirectory, saveName);
			updateMobileObjects(saveDirectory, characterData);
			packageSaveGame(saveDirectory);
			callback.success("{\"success\":true}");
		} catch (JSONException e) {
			callback.failure(-1, SaveChanges.jsonError());
		} catch (IOException | ZipException e) {
			System.err.printf("%s%n", e.getMessage());
			callback.failure(-1, SaveChanges.ioError());
		}
	}

	private void packageSaveGame (File saveDirectory) throws ZipException {
		File pillarsSavesDirectory;
		try {
			pillarsSavesDirectory = new File(
				Settings.getInstance().json.getString("savesLocation"));
		} catch (JSONException e) {
			System.err.printf(
				"Unable to determine Pillars of Eternity "
				+ "save game location!%n");

			return;
		}

		File saveFile =
			new File(pillarsSavesDirectory, saveDirectory.getName());

		if (!FileUtils.deleteQuietly(saveFile)) {
			System.err.printf(
				"Unable to delete old save game '%s'!%n"
				, saveFile.getAbsolutePath());
		}

		File[] saveContents = saveDirectory.listFiles();
		if (saveContents == null) {
			System.err.printf(
				"Save directory '%s' is empty!%n"
				, saveDirectory.getAbsolutePath());

			return;
		}

		ZipFile saveArchive = new ZipFile(saveFile);
		saveArchive.createZipFile(
			new ArrayList<>(Arrays.asList(saveContents))
			, new ZipParameters());
	}

	private void updateMobileObjects (
		File saveDirectory
		, JSONArray characterData)
		throws IOException {

		List<Property> mobileObjects = new ArrayList<>();
		File mobileObjectsFile = new File(saveDirectory, "MobileObjects.save");
		SharpSerializer deserializer =
			new SharpSerializer(mobileObjectsFile.getAbsolutePath());

		SimpleProperty objectCount =
			(SimpleProperty) deserializer.deserialize().get();

		int count = (int) objectCount.value;
		for (int i = 0; i < count; i++) {
			Optional<Property> potentialProperty = deserializer.deserialize();
			if (!potentialProperty.isPresent()
				|| !(potentialProperty.get().obj
					instanceof ObjectPersistencePacket)) {

				System.err.printf("Deserialization error!%n");
				continue;
			}

			Property property =
				updateMobileObject(potentialProperty.get(), characterData);

			mobileObjects.add(property);
		}

		if (!mobileObjectsFile.delete()) {
			System.err.printf(
				"Unable to remove old MobileObjects.save at '%s'!%n"
				, mobileObjectsFile.getAbsolutePath());

			return;
		}

		Files.createFile(mobileObjectsFile.toPath());
		SharpSerializer serializer =
			new SharpSerializer(mobileObjectsFile.getAbsolutePath());

		serializer.serialize(objectCount);
		for (Property obj : mobileObjects) {
			serializer.serialize(obj);
		}
	}

	private Property updateMobileObject (
		Property property
		, JSONArray characterData) {

		ObjectPersistencePacket packet = (ObjectPersistencePacket) property.obj;
		for (int i = 0; i < characterData.length(); i++) {
			JSONObject character = characterData.getJSONObject(i);
			if (character.getString("GUID").equals(packet.ObjectID)) {
				updateCharacter(
					(ComplexProperty) property
					, character.getJSONObject("stats"));

				break;
			}
		}

		return property;
	}

	@SuppressWarnings("unchecked")
	private void updateCharacter (
		ComplexProperty property
		, JSONObject character) {

		// Having to do linear searches through everything multiple times
		// sucks for performance efficiency however that's our only option as
		// we have to use Property-first serialization. If profiling later shows
		// that this is taking too long then we may need to do some optimising.

		SingleDimensionalArrayProperty componentPackets = null;
		for (Property subProperty : (List<Property>) property.properties) {
			if (subProperty.name.equals("ComponentPackets")) {
				componentPackets = (SingleDimensionalArrayProperty) subProperty;
				break;
			}
		}

		if (componentPackets == null) {
			System.err.printf("No ComponentPackets property found!%n");
			return;
		}

		ComplexProperty characterStats = null;
		for (Property subProperty : (List<Property>) componentPackets.items) {
			ComponentPersistencePacket packet =
				(ComponentPersistencePacket) subProperty.obj;

			if (packet.TypeString.equals("CharacterStats")) {
				characterStats = (ComplexProperty) subProperty;
				break;
			}
		}

		if (characterStats == null) {
			System.err.printf("Unable to find CharacterStats!%n");
			return;
		}

		DictionaryProperty variables = null;
		for (Property subProperty
			: (List<Property>) characterStats.properties) {

			if (subProperty.name.equals("Variables")) {
				variables = (DictionaryProperty) subProperty;
				break;
			}
		}

		if (variables == null) {
			System.err.printf("No Variables property found!%n");
			return;
		}

		for (String updateKey : character.keySet()) {
			String updateValue = character.getString(updateKey);
			for (Entry<Property, Property> item : variables.items) {
				if (item.getKey() instanceof SimpleProperty) {
					SimpleProperty keyProperty = (SimpleProperty) item.getKey();
					if (keyProperty.value.equals(updateKey)) {
						SimpleProperty valueProperty =
							(SimpleProperty) item.getValue();

						Object typedValue =
							castValue(valueProperty.obj, updateValue);

						valueProperty.value = typedValue;
						valueProperty.obj = typedValue;
						break;
					}
				}
			}
		}
	}

	private Object castValue (Object primitive, String val) {
		String cls = primitive.getClass().getSimpleName();

		if (cls.equals("int") || cls.equals("Integer")) {
			return Integer.parseInt(val);
		}

		if (cls.equals("double") || cls.equals("Double")) {
			return Double.parseDouble(val);
		}

		if (cls.equals("float") || cls.equals("Float")) {
			return Float.parseFloat(val);
		}

		return val;
	}

	private void updateSaveInfo (File saveDirectory, String saveName)
		throws IOException {

		File saveinfoXML = new File(saveDirectory, "saveinfo.xml");
		String contents = new String(
			EKUtils.removeBOM(FileUtils.readFileToByteArray(saveinfoXML)));

		ByteArrayOutputStream newContentsStream =
			new ByteArrayOutputStream(contents.length());

		try {
			Match xml = $(contents);
			xml.find("Simple[name='UserSaveName']").attr("value", saveName);
			xml.write(newContentsStream);
		} catch (DOMException e) {
			System.err.printf(
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

	private File createNewSave (String absolutePath) throws IOException {
		Environment environment = Environment.getInstance();
		File workingDirectory = environment.getWorkingDirectory();
		File oldSave = new File(absolutePath);
		String sessionID = oldSave.getName().split(" ")[0].replace("-", "");

		int gameID = getAvailableGameID(workingDirectory, sessionID);
		File newDirectory = createNewSaveDirectory(
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
}
