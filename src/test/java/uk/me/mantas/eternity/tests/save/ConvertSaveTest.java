package uk.me.mantas.eternity.tests.save;

import org.apache.commons.io.FileUtils;
import org.cef.callback.CefQueryCallback;
import org.joox.Match;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.PacketDeserializerFactory;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;
import uk.me.mantas.eternity.game.ComponentPersistencePacket;
import uk.me.mantas.eternity.game.CurrencyValue;
import uk.me.mantas.eternity.game.GameDifficulty;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.save.ChangesSaver;
import uk.me.mantas.eternity.serializer.SerializerFormat;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.joox.JOOX.$;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static uk.me.mantas.eternity.EKUtils.findComponent;
import static uk.me.mantas.eternity.EKUtils.unwrapPacket;

// TODO: move test code into ConvertSaves or similar class once proven
// TODO: add GUI or command-line driving
public class ConvertSaveTest extends TestHarness {

	@Test
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Ignore
	public void demoOfSavingAll ()
			throws NoSuchFieldException
			, IllegalAccessException
			, URISyntaxException
			, IOException {

		final Environment mockEnvironment = mockEnvironment();
		final File workingDirectory = EKUtils.createTempDir(PREFIX).get();
		final File settingsFile = new File(workingDirectory, "settings.json");

		FileUtils.writeStringToFile(settingsFile, "{}");
		when(mockEnvironment.directory().settingsFile()).thenReturn(settingsFile);
		when(mockEnvironment.factory().packetDeserializer())
				.thenReturn(new PacketDeserializerFactory());
		when(mockEnvironment.factory().sharpSerializer()).thenReturn(new SharpSerializerFactory());

		final Settings mockSettings = mockSettings();
		final JSONObject mockJSON = mock(JSONObject.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		String request = "{"
				+ "\"savedYet\":false"
				+ ",\"saveName\":\"TEST\""
				+ ",\"absolutePath\":\"%s\""
				+ ",\"saveData\":{"
				+ "\"characters\":[{"
				+ "\"GUID\":\"b1a7e809-0000-0000-0000-000000000000\""
				+ ", \"stats\":{\"BaseMight\":{"
				+ "\"type\":\"java.lang.Integer\",\"value\":\"30\"}}}, {"
				+ "\"GUID\":\"09517a0d-4fec-407c-a749-a531f3be64e0\""
				+ ", \"stats\":{\"BaseResolve\":{"
				+ "\"type\":\"java.lang.Integer\",\"value\":\"50\"}}}]"
				+ ", \"currency\":3.14159"
				+ ", \"globals\":{"
				+ "\"Global\":{\"GameState\":{\"Difficulty\":{"
				+ "\"type\":\"uk.me.mantas.eternity.game.GameDifficulty\""
				+ ", \"value\":\"StoryTime\"}}}"
				+ ", \"InGameGlobal\":{\"GlobalVariables\":{"
				+ "\"n_Eder_Cipher\":{\"type\":\"java.lang.Integer\",\"value\":\"50\"}"
				+ ",\"b_gramrfel_prisoner\":{"
				+ "\"type\":\"java.lang.Integer\""
				+ ",\"value\":\"-1\"}}}}}}";

		final String absolutePath =
				new File(
						getClass().getResource("/ChangesSaverTest/id 0 Encampment.savegame").toURI())
						.getAbsolutePath();

		mockSettings.json = mockJSON;
		request = String.format(request, absolutePath.replace("\\", "\\\\"));
		when(mockEnvironment.directory().working()).thenReturn(workingDirectory);

		doThrow(new JSONException("")).when(mockJSON).getString(anyString());

		final File saveDirectory = new File(workingDirectory, "id 0 Encampment.savegame");
		final ChangesSaver cls = new ChangesSaver(request, mockCallback);

		cls.run();
		verify(mockCallback).success("{\"success\":true}");
		verify(mockEnvironment.state()).previousSaveDirectory(saveDirectory);

		final byte[] saveinfoBytes =
				FileUtils.readFileToByteArray(new File(saveDirectory, "saveinfo.xml"));

		assertEquals(-17, saveinfoBytes[0]);
		final Match xml = $(new String(EKUtils.removeBOM(saveinfoBytes), "UTF-8"));
		assertEquals("TEST", xml.find("Simple[name='UserSaveName']").attr("value"));

		final File mobileObjectsFile = new File(saveDirectory, "MobileObjects.save");
		final SharpSerializer deserializer =
				new SharpSerializer(mobileObjectsFile.getAbsolutePath());
		final Optional<Property> objectCountProp = deserializer.deserialize();
		assertTrue(objectCountProp.isPresent());

		final int objectCount = (int) objectCountProp.get().obj;
		boolean mightUpdated = false;
		boolean resolveUpdated = false;
		boolean currencyUpdated = false;
		boolean difficultyUpdated = false;
		boolean globalVariablesUpdated = false;

		for (int i = 0; i < objectCount; i++) {
			final Optional<Property> property = deserializer.deserialize();
			assertTrue(property.isPresent());
			final ObjectPersistencePacket packet = unwrapPacket(property.get());

			if (!packet.ObjectID.equals("b1a7e809-0000-0000-0000-000000000000")
					&& !packet.ObjectID.equals("09517a0d-4fec-407c-a749-a531f3be64e0")
					&& !packet.ObjectName.equals("Global(Clone)")
					&& !packet.ObjectName.equals("InGameGlobal(Clone)")) {

				continue;
			}

			final Optional<ComponentPersistencePacket> stats =
					findComponent(packet.ComponentPackets, "CharacterStats");

			if (stats.isPresent()) {
				final Map<String, Object> vars = stats.get().Variables;

				if (packet.ObjectID.equals("b1a7e809-0000-0000-0000-000000000000")) {
					mightUpdated = (int) vars.get("BaseMight") == 30;
				}

				if (packet.ObjectID.equals("09517a0d-4fec-407c-a749-a531f3be64e0")) {
					resolveUpdated = (int) vars.get("BaseResolve") == 50;

					final ComponentPersistencePacket inventory =
							findComponent(packet.ComponentPackets, "PlayerInventory").get();

					final CurrencyValue currency =
							(CurrencyValue) inventory.Variables.get("currencyTotalValue");

					currencyUpdated = currency.v == 3.14159f;
				}
			}

			if (packet.ObjectName.equals("Global(Clone)")) {
				final ComponentPersistencePacket gameState =
						findComponent(packet.ComponentPackets, "GameState").get();
				final Map<String, Object> vars = gameState.Variables;
				difficultyUpdated = vars.get("Difficulty").equals(GameDifficulty.StoryTime);
			}

			if (packet.ObjectName.equals("InGameGlobal(Clone)")) {
				final ComponentPersistencePacket globalVariables =
						findComponent(packet.ComponentPackets, "GlobalVariables").get();
				@SuppressWarnings("unchecked")
				final Hashtable<String, Integer> data =
						(Hashtable<String, Integer>) globalVariables.Variables.get("m_data");
				globalVariablesUpdated =
						data.get("n_Eder_Cipher") == 50 && data.get("b_gramrfel_prisoner") == -1;
			}
		}

		assertTrue(
				mightUpdated
						&& resolveUpdated
						&& currencyUpdated
						&& difficultyUpdated
						&& globalVariablesUpdated);
	}


	// TODO: remove this, or maybe the whole class
	@Test
	@Ignore
	public void serializesWindowsStoreToSteamSaveFile () throws URISyntaxException, IOException {
		final File saveFile = new File(getClass().getResource("/SerializerTest/windowStoreSave/MobileObjects.save").toURI());
		final SharpSerializer deserializer = new SharpSerializer(saveFile.getAbsolutePath());
		final List<Property> deserialized = new ArrayList<>();
		final Optional<Property> objectCount = deserializer.deserialize();
		final int count = (int) objectCount.get().obj;

		for (int i = 0; i < count; i++) {
			final Optional<Property> obj = deserializer.deserialize();
			deserialized.add(obj.get());
		}

		final File saveOutputFile = Files.createTempFile(null, null).toFile();
		try {
			final SharpSerializer serializer =
					new SharpSerializer(saveOutputFile.getAbsolutePath(), SerializerFormat.UNITY_2017);

			serializer.serialize(objectCount.get());
			for (final Property obj : deserialized) {
				serializer.serialize(obj);
			}

			final byte[] actual = FileUtils.readFileToByteArray(saveOutputFile);

			final File expectedSaveFile = new File(getClass().getResource("/SerializerTest/windowStoreSaveConverted/MobileObjects.save").toURI());
			final byte[] expected = FileUtils.readFileToByteArray(expectedSaveFile);

			assertArrayEquals(expected, actual);
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			assertTrue(saveOutputFile.delete());
		}
	}
}
