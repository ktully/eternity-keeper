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

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.UnsignedInteger;
import org.apache.commons.io.FileUtils;
import org.cef.callback.CefQueryCallback;
import org.jooq.lambda.tuple.Tuple2;
import org.json.JSONObject;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.PacketDeserializerFactory;
import uk.me.mantas.eternity.game.*;
import uk.me.mantas.eternity.save.SavedGameOpener;
import uk.me.mantas.eternity.serializer.PacketDeserializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.tests.ExposedClass;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SavedGameOpenerTest extends TestHarness {
	private static final String DESERIALIZATION_ERR = "{\"error\":\"DESERIALIZATION_ERR\"}";

	@Test
	public void mobileObjectsFileNotExists () {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		SavedGameOpener cls = new SavedGameOpener("404", mockCallback);
		cls.run();

		verify(mockCallback).success("{\"error\":\"NOT_EXISTS\"}");
	}

	@Test
	public void deserializationError () throws IOException {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		Optional<File> saveDir = EKUtils.createTempDir(PREFIX);
		assertTrue(saveDir.isPresent());

		Files.createFile(
			new File(saveDir.get(), "MobileObjects.save").toPath());

		SavedGameOpener cls =
			new SavedGameOpener(saveDir.get().getAbsolutePath(), mockCallback);

		cls.run();
		verify(mockCallback).success("{\"error\":\"DESERIALIZATION_ERR\"}");
	}

	@Test
	public void saveGameOpened () throws URISyntaxException, IOException {
		final File resources = new File(getClass().getResource("/").toURI());
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final SavedGameOpener cls =
			new SavedGameOpener(resources.getAbsolutePath(), mockCallback);

		final Settings mockSettings = mockSettings();
		final JSONObject mockJSON = mock(JSONObject.class);

		// (don't remove settings mock; SavedGameOpener needs this singleton to load correct portraits)
		mockSettings.json = mockJSON;
		when(mockJSON.getString("gameLocation")).thenReturn(
			new File(resources, "SavedGameOpenerTest").getAbsolutePath());

		cls.run(); // we expect this to invoke success() on the mockCallback with JSON as below

		final File extractedFile =
			new File(getClass().getResource("/SavedGameOpenerExtracted.json").toURI());
		final String extractedJSONString = FileUtils.readFileToString(extractedFile);

		verify(mockCallback).success(
			argThat(new EquivalentJSON(extractedJSONString)));
	}

	private class EquivalentJSON implements ArgumentMatcher<String> {
		JSONObject expected = null;
		JSONObject actual = null;

		public EquivalentJSON(String expected) {
			this.expected = new JSONObject(expected);
		}

		public boolean matches(String actual) {
			this.actual = new JSONObject(actual);
			// ignores whitespace & ordering of object keys/children
			// (order of array entries is still significant)

			return expected.similar(this.actual);
		}

		public String toString() {
			// this removes whitespace
			// BUT may still have insignificant object key ordering differences	from actual
			// TODO: to aid troubleshooting tests, output expected using the ordering from actual (where available)
			return expected.toString();
		}
	}

	private PacketDeserializer mockDeserializer (final Environment mockEnvironment)
		throws FileNotFoundException {

		final PacketDeserializerFactory mockFactory = mock(PacketDeserializerFactory.class);
		final PacketDeserializer mockDeserializer = mock(PacketDeserializer.class);

		when(mockEnvironment.factory().packetDeserializer()).thenReturn(mockFactory);
		when(mockFactory.forFile(any(File.class))).thenReturn(mockDeserializer);

		return mockDeserializer;
	}

	@Test
	public void deserializeTestDeserializationError () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final PacketDeserializer mockDeserializer = mockDeserializer(mockEnvironment);
		final File mockMobileObjectsFile = mock(File.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		when(mockDeserializer.deserialize()).thenReturn(Optional.empty());
		when(mockMobileObjectsFile.getAbsolutePath()).thenReturn("404");

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		exposedOpener.call("deserialize", mockMobileObjectsFile);

		verify(mockCallback).success(DESERIALIZATION_ERR);
	}

	@Test
	public void deserializeTestThrowsException () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final PacketDeserializer mockDeserializer = mockDeserializer(mockEnvironment);
		final File mockMobileObjectsFile = mock(File.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		when(mockMobileObjectsFile.getAbsolutePath()).thenReturn("404");
		doThrow(new FileNotFoundException()).when(mockDeserializer).deserialize();

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		exposedOpener.call("deserialize", mockMobileObjectsFile);

		verify(mockCallback).success(DESERIALIZATION_ERR);
	}

	@Test
	public void extractGlobalsTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final Property startsWithGlobal = mock(Property.class);
		final Property startsWithInGameGlobal = mock(Property.class);
		final Property notFound = mock(Property.class);

		final List<Property> gameObjects =
			ImmutableList.of(startsWithGlobal, startsWithInGameGlobal, notFound);

		final ObjectPersistencePacket startsWithGlobalPacket = new ObjectPersistencePacket();
		startsWithGlobalPacket.ObjectName = "Global(Clone)";
		startsWithGlobal.obj = startsWithGlobalPacket;

		final ObjectPersistencePacket startsWithInGameGlobalPacket = new ObjectPersistencePacket();
		startsWithInGameGlobalPacket.ObjectName = "InGameGlobal(Clone)";
		startsWithInGameGlobal.obj = startsWithInGameGlobalPacket;

		final ObjectPersistencePacket notFoundPacket = new ObjectPersistencePacket();
		notFoundPacket.ObjectName = "SecondWind(Clone)";
		notFound.obj = notFoundPacket;

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		final Map<Object, Class> argMap = new HashMap<>();
		argMap.put(gameObjects, List.class);

		final Map<String, Property> globals = exposedOpener.call("extractGlobals", argMap);
		assertEquals(2, globals.size());
		assertSame(startsWithGlobal, globals.get("Global"));
		assertSame(startsWithInGameGlobal, globals.get("InGameGlobal"));
	}

	@Test
	public void globalsToJSONTest () {
		final Environment mockEnvironment = mockEnvironment();
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final Property global = mock(Property.class);
		final ComponentPersistencePacket found = new ComponentPersistencePacket();
		final ComponentPersistencePacket notFound = new ComponentPersistencePacket();

		when(mockEnvironment.config().usefulGlobals()).thenReturn(ImmutableList.of("Found"));
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		packet.ComponentPackets = new ComponentPersistencePacket[] {found, notFound};
		global.obj = packet;

		found.TypeString = "Found";
		notFound.TypeString = "NotFound";

		found.Variables = new HashMap<>();
		found.Variables.put("SupportedType", 1);
		found.Variables.put("UnsupportedType", new CurrencyValue());

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		assertEquals(
			"{\"Found\":{\"SupportedType\":{\"type\":\"java.lang.Integer\",\"value\":1}}}"
			, exposedOpener.call("globalsToJSON", global).toString());
	}

	@Test
	public void extractCharactersTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final Property noObjectID = mock(Property.class);
		final Property startsWithCompanion = mock(Property.class);
		final Property startsWithLowercaseCompanion = mock(Property.class);
		final Property startsWithPlayer = mock(Property.class);

		final List<Property> gameObjects = new ArrayList<Property>(){{
			add(noObjectID);
			add(startsWithCompanion);
			add(startsWithLowercaseCompanion);
			add(startsWithPlayer);
		}};

		final ObjectPersistencePacket noObjectIDPacket = new ObjectPersistencePacket();
		noObjectIDPacket.ObjectName = "";
		noObjectIDPacket.ObjectID = null;
		noObjectID.obj = noObjectIDPacket;

		final ObjectPersistencePacket startsWithCompanionPacket = new ObjectPersistencePacket();
		startsWithCompanionPacket.ObjectName = "Companion_A";
		startsWithCompanionPacket.ObjectID = "Key_A";
		startsWithCompanion.obj = startsWithCompanionPacket;

		final ObjectPersistencePacket startsWithLowercaseCompanionPacket =
			new ObjectPersistencePacket();
		startsWithLowercaseCompanionPacket.ObjectName = "companion_C";
		startsWithLowercaseCompanionPacket.ObjectID = "Key_C";
		startsWithLowercaseCompanion.obj = startsWithLowercaseCompanionPacket;

		final ObjectPersistencePacket startsWithPlayerPacket = new ObjectPersistencePacket();
		startsWithPlayerPacket.ObjectName = "Player_B";
		startsWithPlayerPacket.ObjectID = "Key_B";
		startsWithPlayer.obj = startsWithPlayerPacket;

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		final Map<Object, Class> argMap = new HashMap<Object, Class>(){{
			put(gameObjects, List.class);
		}};

		final Map<String, Property> characters = exposedOpener.call("extractCharacters", argMap);
		assertEquals(3, characters.size());
		assertSame(startsWithCompanion, characters.get("Key_A"));
		assertSame(startsWithLowercaseCompanion, characters.get("Key_C"));
		assertSame(startsWithPlayer, characters.get("Key_B"));
	}

	@Test
	public void extractPortraitTestSelectsFirst () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket firstComponent = new ComponentPersistencePacket();
		final ComponentPersistencePacket secondComponent = new ComponentPersistencePacket();

		firstComponent.TypeString = "Portrait";
		secondComponent.TypeString = "Portrait";
		firstComponent.Variables = new HashMap<>();
		secondComponent.Variables = new HashMap<String, Object>() {{
			put("m_textureLargePath", "png");
		}};

		packet.ComponentPackets = new ComponentPersistencePacket[]{
			firstComponent
			, secondComponent
		};

		mockSettings();

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = exposedOpener.call("extractPortrait", packet, false);

		assertEquals("", result);
	}

	@Test
	public void extractPortraitTestNoGameLocation () {
		final Settings mockSettings = mockSettings();
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();

		component.TypeString = "Portrait";
		component.Variables = new HashMap<String, Object>(){{
			put("m_textureLargePath", "png");
		}};

		mockSettings.json = new JSONObject();
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = exposedOpener.call("extractPortrait", packet, false);

		assertEquals("", result);
	}

	@Test
	public void extractPortraitTestPortraitNotFound () throws URISyntaxException {
		final Settings mockSettings = mockSettings();
		final Logger mockLogger = interceptLogging(SavedGameOpener.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();
		final File testResources = new File(getClass().getResource("/").toURI());

		component.TypeString = "Portrait";
		component.Variables = new HashMap<String, Object>(){{
			put("m_textureLargePath", "../404");
		}};

		mockSettings.json = new JSONObject().put("gameLocation", testResources.getAbsolutePath());
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = exposedOpener.call("extractPortrait", packet, false);

		final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(mockLogger).error(anyString(), argument.capture());
		assertEquals("", result);
		assertEquals(new File(testResources, "404").getAbsolutePath(), argument.getValue());
	}

	@Test
	public void extractPortraitTestUnmappedCompanion () throws URISyntaxException {
		final Settings mockSettings = mockSettings();
		final Logger mockLogger = interceptLogging(SavedGameOpener.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();
		final File testResources = new File(getClass().getResource("/").toURI());
		final String companionPortraitPath =
			Environment.getInstance().config().companionPortraitPath();
		final String substitutedPath = String.format(companionPortraitPath, "unmapped");
		final File portraitFile =
			Paths.get(testResources.toURI())
				.resolve(Environment.getInstance().config().pillarsDataDirectory())
				.resolve(substitutedPath)
				.toFile();

		mockSettings.json = new JSONObject().put("gameLocation", testResources.getAbsolutePath());
		packet.ObjectName = "Companion_Unmapped";
		component.TypeString = "Portrait";
		component.Variables = new HashMap<>();
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = exposedOpener.call("extractPortrait", packet, true);

		final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(mockLogger).error(anyString(), argument.capture());
		assertEquals("", result);
		assertEquals(portraitFile.getAbsolutePath(), argument.getValue());
	}

	@Test
	public void extractPortraitTestMappedCompanion () throws URISyntaxException {
		final Settings mockSettings = mockSettings();
		final Logger mockLogger = interceptLogging(SavedGameOpener.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();
		final File testResources = new File(getClass().getResource("/").toURI());
		final String companionPortraitPath =
			Environment.getInstance().config().companionPortraitPath();
		final String substitutedPath = String.format(companionPortraitPath, "grieving_mother");
		final File portraitFile =
			Paths.get(testResources.toURI())
				.resolve(Environment.getInstance().config().pillarsDataDirectory())
				.resolve(substitutedPath)
				.toFile();

		mockSettings.json = new JSONObject().put("gameLocation", testResources.getAbsolutePath());
		packet.ObjectName = "Companion_Grieving_Mother";
		component.TypeString = "Portrait";
		component.Variables = new HashMap<>();
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = exposedOpener.call("extractPortrait", packet, true);

		final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(mockLogger).error(anyString(), argument.capture());
		assertEquals("", result);
		assertEquals(portraitFile.getAbsolutePath(), argument.getValue());
	}

	@Test
	public void detectDeadTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();

		component.TypeString = "Health";
		component.Variables = new HashMap<>();
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		boolean result = exposedOpener.call("detectDead", packet);
		assertFalse(result);

		component.Variables = new HashMap<String, Object>(){{
			put("CurrentHealth", 1f);
		}};

		result = exposedOpener.call("detectDead", packet);
		assertFalse(result);

		component.Variables = new HashMap<String, Object>(){{
			put("CurrentHealth", 0f);
		}};

		result = exposedOpener.call("detectDead", packet);
		assertTrue(result);
	}

	@Test
	public void extractNameTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		packet.ObjectName = "NoUnderscore";
		String result = exposedOpener.call("extractName", packet);
		assertEquals("", result);

		packet.ObjectName = "Player_HasUnderscore";
		result = exposedOpener.call("extractName", packet);
		assertEquals("HasUnderscore", result);

		packet.ObjectName = "Player_HasBracket(Clone)_1";
		result = exposedOpener.call("extractName", packet);
		assertEquals("HasBracket", result);
	}

	@Test
	public void extractCharacterStatsTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket notStatsComponent = new ComponentPersistencePacket();
		final ComponentPersistencePacket statsComponent = new ComponentPersistencePacket();
		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		notStatsComponent.TypeString = "NotCharacterStats";
		statsComponent.TypeString = "CharacterStats";
		statsComponent.Variables = new HashMap<String, Object>(){{
			put("Integer", 1);
			put("Float", 1f);
			put("String", "1");
			put("UnsignedInteger", UnsignedInteger.valueOf(1L));
			put("NotSupported", new byte[0]);
		}};

		packet.ComponentPackets = new ComponentPersistencePacket[]{ notStatsComponent };
		Optional<Map<String, Object>> result = exposedOpener.call("extractCharacterStats", packet);
		assertFalse(result.isPresent());
		packet.ComponentPackets = new ComponentPersistencePacket[]{
			notStatsComponent
			, null
			, statsComponent
		};

		result = exposedOpener.call("extractCharacterStats", packet);
		assertTrue(result.isPresent());
		assertEquals(
			"{\"type\":\"java.lang.Integer\",\"value\":1}"
			, result.get().get("Integer").toString());
		assertEquals(
			"{\"type\":\"java.lang.String\",\"value\":\"1\"}"
			, result.get().get("String").toString());
		assertEquals(
			"{\"type\":\"java.lang.Float\",\"value\":1}"
			, result.get().get("Float").toString());
		assertEquals(
			"{\"type\":\"com.google.common.primitives.UnsignedInteger\",\"value\":1}"
			, result.get().get("UnsignedInteger").toString());
		assertNull(result.get().get("NotSupported"));
	}

	@Test
	public void detectCompanionTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		packet.ObjectName = "Companion_Calisca";
		boolean result = exposedOpener.call("detectCompanion", packet);
		assertTrue(result);

		packet.ObjectName = "companion_Heodan";
		result = exposedOpener.call("detectCompanion", packet);
		assertTrue(result);

		packet.ObjectName = "Companion_Generic_(Clone)_1";
		result = exposedOpener.call("detectCompanion", packet);
		assertFalse(result);

		packet.ObjectName = "Player_Fyorl";
		result = exposedOpener.call("detectCompanion", packet);
		assertFalse(result);
	}

	private Tuple2<Logger, ExposedClass> setupExtractCurrency () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final Logger mockLogger = interceptLogging(SavedGameOpener.class);
		final SavedGameOpener opener = new SavedGameOpener("", mockCallback);
		final ExposedClass exposedOpener = expose(opener);
		return new Tuple2<>(mockLogger, exposedOpener);
	}

	@Test
	public void extractCurrencyTestNoPlayerPacket () {
		final Tuple2<Logger, ExposedClass> setup = setupExtractCurrency();
		final Map<Object, Class> argMap = new HashMap<Object, Class>() {{
			put(new ArrayList<Property>(), List.class);
		}};

		assertEquals(0f, setup.v2().call("extractCurrency", argMap), 0f);
		verify(setup.v1()).error("Unable to find player mobile object.%n");
	}

	@Test
	public void extractCurrencyTestNoPlayerInventoryComponent () {
		final Tuple2<Logger, ExposedClass> setup = setupExtractCurrency();
		final Property property = mock(Property.class);
		final ObjectPersistencePacket playerPacket = mock(ObjectPersistencePacket.class);
		final List<Property> gameObjects = new ArrayList<Property>() {{add(property);}};
		final Map<Object, Class> argMap = new HashMap<Object, Class>() {{
			put(gameObjects, List.class);
		}};

		property.obj = playerPacket;
		playerPacket.ObjectName = "Player_Elenor";
		playerPacket.ComponentPackets = new ComponentPersistencePacket[0];

		assertEquals(0f, setup.v2().call("extractCurrency", argMap), 0f);
		verify(setup.v1()).error("Unable to find PlayerInventory component.");
	}

	@Test
	public void extractCurrencyTestNoCurrencyTotalValue () {
		final Tuple2<Logger, ExposedClass> setup = setupExtractCurrency();
		final Property property = mock(Property.class);
		final ObjectPersistencePacket playerPacket = mock(ObjectPersistencePacket.class);
		final ComponentPersistencePacket inventoryComponent =
			mock(ComponentPersistencePacket.class);

		final List<Property> gameObjects = new ArrayList<Property>() {{add(property);}};
		final Map<Object, Class> argMap = new HashMap<Object, Class>() {{
			put(gameObjects, List.class);
		}};

		property.obj = playerPacket;
		playerPacket.ObjectName = "Player_Elenor";
		playerPacket.ComponentPackets = new ComponentPersistencePacket[] {inventoryComponent};
		inventoryComponent.TypeString = "PlayerInventory";
		inventoryComponent.Variables = new HashMap<>();

		assertEquals(0f, setup.v2().call("extractCurrency", argMap), 0f);
		verify(setup.v1()).error("Unable to find currencyTotalValue in PlayerInventory component.");
	}

	@Test
	public void extractCurrencyTest () {
		final Tuple2<Logger, ExposedClass> setup = setupExtractCurrency();
		final Property property = mock(Property.class);
		final ObjectPersistencePacket playerPacket = mock(ObjectPersistencePacket.class);
		final CurrencyValue currencyValue = new CurrencyValue();
		final ComponentPersistencePacket inventoryComponent =
			mock(ComponentPersistencePacket.class);

		final List<Property> gameObjects = new ArrayList<Property>() {{add(property);}};
		final Map<Object, Class> argMap = new HashMap<Object, Class>() {{
			put(gameObjects, List.class);
		}};

		currencyValue.v = 3.14159f;
		property.obj = playerPacket;
		playerPacket.ObjectName = "Player_Elenor";
		playerPacket.ComponentPackets = new ComponentPersistencePacket[] {inventoryComponent};
		inventoryComponent.TypeString = "PlayerInventory";
		inventoryComponent.Variables = new HashMap<String, Object>() {{
			put("currencyTotalValue", currencyValue);
		}};

		assertEquals(3.14159f, setup.v2().call("extractCurrency", argMap), 1e-6f);
	}

	private enum Enum {ONE, TWO}

	@Test
	public void recordTypeTest () {
		final ExposedClass exposedOpener = expose(SavedGameOpener.class);
		final EternityDateTime dateTime = new EternityDateTime();
		final EternityTimeInterval timeInterval = new EternityTimeInterval();
		final Map<Object, Class> argMap = new HashMap<>();

		dateTime.TotalSeconds = 42;
		timeInterval.SerializedSeconds = 42;

		argMap.put(1, Object.class);
		final JSONObject intTest = exposedOpener.call("recordType", argMap);
		assertEquals("{\"type\":\"java.lang.Integer\",\"value\":1}", intTest.toString());

		argMap.clear();
		argMap.put(1d, Object.class);
		final JSONObject dblTest = exposedOpener.call("recordType", argMap);
		assertEquals("{\"type\":\"java.lang.Double\",\"value\":1}", dblTest.toString());

		argMap.clear();
		argMap.put(true, Object.class);
		final JSONObject boolTest = exposedOpener.call("recordType", argMap);
		assertEquals("{\"type\":\"java.lang.Boolean\",\"value\":true}", boolTest.toString());

		argMap.clear();
		argMap.put("str", Object.class);
		final JSONObject strTest = exposedOpener.call("recordType", argMap);
		assertEquals("{\"type\":\"java.lang.String\",\"value\":\"str\"}", strTest.toString());

		argMap.clear();
		argMap.put(UnsignedInteger.valueOf(Integer.MAX_VALUE + 1L), Object.class);
		final JSONObject uintTest = exposedOpener.call("recordType", argMap);
		assertEquals(
			"{\"type\":\"com.google.common.primitives.UnsignedInteger\",\"value\":2147483648}"
			, uintTest.toString());

		argMap.clear();
		argMap.put(Enum.ONE, Object.class);
		final JSONObject enumTest = exposedOpener.call("recordType", argMap);
		assertEquals(
			"{\"type\":\"uk.me.mantas.eternity.tests.save.SavedGameOpenerTest$Enum\""
			+ ",\"value\":\"ONE\"}"
			, enumTest.toString());

		argMap.clear();
		argMap.put(dateTime, Object.class);
		final JSONObject dateTimeTest = exposedOpener.call("recordType", argMap);
		assertEquals(
			"{\"type\":\"uk.me.mantas.eternity.game.EternityDateTime\",\"value\":42}"
			, dateTimeTest.toString());

		argMap.clear();
		argMap.put(timeInterval, Object.class);
		final JSONObject timeIntervalTest = exposedOpener.call("recordType", argMap);
		assertEquals(
			"{\"type\":\"uk.me.mantas.eternity.game.EternityTimeInterval\",\"value\":42}"
			, timeIntervalTest.toString());
	}

	@Test
	public void globalsToJSONGlobalVariablesTest () {
		final ExposedClass exposedOpener = expose(SavedGameOpener.class);
		final Property globalProperty = mock(Property.class);
		final ObjectPersistencePacket global = new ObjectPersistencePacket();
		final ComponentPersistencePacket packet = new ComponentPersistencePacket();
		final Hashtable<String, Integer> table = new Hashtable<>();

		globalProperty.obj = global;
		global.ComponentPackets = new ComponentPersistencePacket[]{packet};
		packet.TypeString = "GlobalVariables";
		packet.Variables = new HashMap<>();
		packet.Variables.put("m_data", table);
		table.put("A", 1);
		table.put("B", 2);

		final JSONObject result = exposedOpener.call("globalsToJSON", globalProperty);
		assertEquals(
			"{\"GlobalVariables\":{"
				+ "\"A\":{\"type\":\"java.lang.Integer\",\"value\":1}"
				+ ",\"B\":{\"type\":\"java.lang.Integer\",\"value\":2}}}"
			, result.toString());
	}
}
