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


package uk.me.mantas.eternity.tests.save;

import com.google.common.primitives.UnsignedInteger;
import org.apache.commons.io.FileUtils;
import org.cef.callback.CefQueryCallback;
import org.joox.Match;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.PacketDeserializerFactory;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;
import uk.me.mantas.eternity.game.*;
import uk.me.mantas.eternity.save.ChangesSaver;
import uk.me.mantas.eternity.serializer.PacketDeserializer;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.*;
import uk.me.mantas.eternity.tests.ExposedClass;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.joox.JOOX.$;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.me.mantas.eternity.EKUtils.findComponent;
import static uk.me.mantas.eternity.EKUtils.unwrapPacket;

public class ChangesSaverTest extends TestHarness {
	@Test
	public void errorParsingRequest () {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		ChangesSaver cls = new ChangesSaver("{}", mockCallback);

		cls.run();
		verify(mockCallback).failure(
			-1
			, "{\"error\":\"Error parsing JSON request.\"}");
	}

	@Test
	public void inconsistentClientServerState ()
		throws NoSuchFieldException
		, IllegalAccessException {

		Environment mockEnvironment = mockEnvironment();
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		String request = "{"
			+ "\"savedYet\":true"
			+ ",\"saveName\":\"\""
			+ ",\"absolutePath\":\"/. \""
			+ ",\"saveData\":{"
				+ "\"characters\":[]"
				+ ",\"currency\":0"
			+ "}"
		+ "}";

		ChangesSaver cls = new ChangesSaver(request, mockCallback);
		when(mockEnvironment.state().previousSaveDirectory()).thenReturn(null);
		when(mockEnvironment.state().previousSaveDirectory())
			.thenReturn(new File("/404"));

		cls.run();
		verify(mockCallback).failure(
			-1
			, "{\"error\":\"Unable to write new save file.\"}");
	}

	@Test
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public void changesSaved ()
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

	private enum Enum {ONE, TWO}

	@Test
	public void castValueTest () {
		final Logger mockLogger = interceptLogging(ChangesSaver.class);
		final ExposedClass exposedSaver = expose(ChangesSaver.class);
		final EternityDateTime dateTime = new EternityDateTime();
		final EternityTimeInterval timeInterval = new EternityTimeInterval();
		final Map<Object, Class> argMap = new LinkedHashMap<>();

		argMap.put(1, Object.class);
		argMap.put("2", String.class);
		final int intTest = exposedSaver.call("castValue", argMap);
		assertEquals(2, intTest);

		argMap.clear();
		argMap.put(1d, Object.class);
		argMap.put("3.14159", String.class);
		final double dblTest = exposedSaver.call("castValue", argMap);
		assertEquals(3.14159, dblTest, 1e-6);

		argMap.clear();
		argMap.put(true, Object.class);
		argMap.put("false", String.class);
		final boolean boolTest = exposedSaver.call("castValue", argMap);
		assertFalse(boolTest);

		argMap.clear();
		argMap.put(UnsignedInteger.valueOf(Integer.MAX_VALUE + 1L), Object.class);
		argMap.put("2147483648", String.class);
		final UnsignedInteger uintTest = exposedSaver.call("castValue", argMap);
		assertEquals(2147483648L, uintTest.longValue());

		argMap.clear();
		argMap.put(dateTime, Object.class);
		argMap.put("42", String.class);
		final int dateTimeTest = exposedSaver.call("castValue", argMap);
		assertEquals(42, dateTimeTest);

		argMap.clear();
		argMap.put(timeInterval, Object.class);
		argMap.put("42", String.class);
		final int timeIntervalTest = exposedSaver.call("castValue", argMap);
		assertEquals(42, timeIntervalTest);

		argMap.clear();
		argMap.put(Enum.ONE, Object.class);
		argMap.put("TWO", String.class);
		final Enum enumTest = exposedSaver.call("castValue", argMap);
		assertEquals(Enum.TWO, enumTest);

		argMap.clear();
		argMap.put(Enum.ONE, Object.class);
		argMap.put("THREE", String.class);
		final Enum enumFailTest = exposedSaver.call("castValue", argMap);
		assertEquals(Enum.ONE, enumFailTest);

		verify(mockLogger).error(
			"Client returned non-existent enum value '%s' for class %s.%n"
			, "THREE"
			, Enum.class.getName());
	}

	@Test
	public void updateValueTest () {
		final ExposedClass exposedSaver = expose(ChangesSaver.class);
		final PacketDeserializer mockDeserializer = mock(PacketDeserializer.class);
		final SimpleProperty mockSimpleProperty = mock(SimpleProperty.class);
		final ComplexProperty mockComplexProperty = mock(ComplexProperty.class);
		final ComplexProperty mockReferencedProperty = mock(ComplexProperty.class);
		final SimpleProperty mockDateTimeField = mock(SimpleProperty.class);
		final SimpleProperty mockTimeIntervalField = mock(SimpleProperty.class);
		final Map<Object, Class> argMap = new LinkedHashMap<>();

		when(mockDeserializer.followReference(any(ReferenceTargetProperty.class)))
			.thenReturn(Optional.of(mockReferencedProperty));

		argMap.put(mockDeserializer, PacketDeserializer.class);
		argMap.put(mockSimpleProperty, Property.class);
		argMap.put("42", String.class);
		mockSimpleProperty.obj = 1;
		exposedSaver.call("updateValue", argMap);
		assertEquals(42, mockSimpleProperty.value);

		argMap.clear();
		argMap.put(mockDeserializer, PacketDeserializer.class);
		argMap.put(mockComplexProperty, Property.class);
		argMap.put("100", String.class);
		mockComplexProperty.reference = new Reference(1);
		mockReferencedProperty.obj = new EternityDateTime();
		mockReferencedProperty.properties = new ArrayList<>();
		mockReferencedProperty.properties.add(mockDateTimeField);
		mockDateTimeField.name = "TotalSeconds";
		exposedSaver.call("updateValue", argMap);
		assertEquals(100, mockDateTimeField.value);

		argMap.clear();
		argMap.put(mockDeserializer, PacketDeserializer.class);
		argMap.put(mockComplexProperty, Property.class);
		argMap.put("2000", String.class);
		mockComplexProperty.reference = null;
		mockComplexProperty.obj = new EternityTimeInterval();
		mockComplexProperty.properties = new ArrayList<>();
		mockComplexProperty.properties.add(mockTimeIntervalField);
		mockTimeIntervalField.name = "SerializedSeconds";
		exposedSaver.call("updateValue", argMap);
		assertEquals(2000, mockTimeIntervalField.value);
	}
}
