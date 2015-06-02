package uk.me.mantas.eternity.tests.save;

import org.apache.commons.io.FileUtils;
import org.cef.callback.CefQueryCallback;
import org.joox.Match;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.game.ComponentPersistencePacket;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.save.ChangesSaver;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.tests.TestHarness;
import uk.me.mantas.eternity.tests.TestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.joox.JOOX.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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

		Environment mockEnvironment = TestUtils.mockEnvironment();
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		String request = "{"
			+ "\"savedYet\":true"
			+ ",\"saveName\":\"\""
			+ ",\"absolutePath\":\"/. \""
			+ ",\"characterData\":[]"
		+ "}";

		ChangesSaver cls = new ChangesSaver(request, mockCallback);
		when(mockEnvironment.getPreviousSaveDirectory()).thenReturn(null);
		when(mockEnvironment.getWorkingDirectory())
			.thenReturn(new File("/404"));

		cls.run();
		verify(mockCallback).failure(
			-1
			, "{\"error\":\"Unable to write new save file.\"}");
	}

	@Test
	public void changesSaved ()
		throws NoSuchFieldException
		, IllegalAccessException
		, URISyntaxException
		, IOException {

		Environment mockEnvironment = TestUtils.mockEnvironment();

		File workingDirectory = EKUtils.createTempDir(PREFIX).get();
		File settingsFile = new File(workingDirectory, "settings.json");
		FileUtils.writeStringToFile(settingsFile, "{}");
		when(mockEnvironment.getSettingsFile()).thenReturn(settingsFile);

		Settings mockSettings = TestUtils.mockSettings();
		JSONObject mockJSON = mock(JSONObject.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		String request = "{"
			+ "\"savedYet\":false"
			+ ",\"saveName\":\"TEST\""
			+ ",\"absolutePath\":\"%s\""
			+ ",\"characterData\":[{"
				+ "\"GUID\":\"b1a7e809-0000-0000-0000-000000000000\""
				+ ", \"stats\":{\"BaseMight\":30}}, {"
				+ "\"GUID\":\"09517a0d-4fec-407c-a749-a531f3be64e0\""
				+ ", \"stats\":{\"BaseResolve\":50}}]}";

		String absolutePath = new File(
			getClass()
				.getResource("/ChangesSaverTest/id 0 Encampment.savegame")
				.toURI())
			.getAbsolutePath();

		mockSettings.json = mockJSON;
		request = String.format(request, absolutePath.replace("\\", "\\\\"));
		when(mockEnvironment.getWorkingDirectory())
			.thenReturn(workingDirectory);

		doThrow(new JSONException(""))
			.when(mockJSON).getString(anyString());

		File saveDirectory = new File(
			workingDirectory
			, "id 0 Encampment.savegame");

		ChangesSaver cls = new ChangesSaver(request, mockCallback);

		cls.run();
		verify(mockCallback).success("{\"success\":true}");
		verify(mockEnvironment).setPreviousSaveDirectory(saveDirectory);

		String saveinfo =
			FileUtils.readFileToString(new File(saveDirectory, "saveinfo.xml"));

		assertEquals(0, saveinfo.indexOf("\uFEFF"));
		saveinfo = saveinfo.replace("\uFEFF", "");

		Match xml = $(saveinfo);
		assertEquals(
			"TEST"
			, xml.find("Simple[name='UserSaveName']").attr("value"));

		File mobileObjectsFile = new File(saveDirectory, "MobileObjects.save");
		SharpSerializer deserializer =
			new SharpSerializer(mobileObjectsFile.getAbsolutePath());

		Optional<Property> objectCountProp = deserializer.deserialize();
		assertTrue(objectCountProp.isPresent());

		int objectCount = (int) objectCountProp.get().obj;
		boolean mightUpdated = false;
		boolean resolveUpdated = false;

		for (int i = 0; i < objectCount; i++) {
			Optional<Property> property = deserializer.deserialize();
			assertTrue(property.isPresent());
			ObjectPersistencePacket packet =
				(ObjectPersistencePacket) property.get().obj;

			if (!packet.ObjectID.equals("b1a7e809-0000-0000-0000-000000000000")
				&& !packet.ObjectID
					.equals("09517a0d-4fec-407c-a749-a531f3be64e0")) {

				continue;
			}

			ComponentPersistencePacket stats =
				Arrays.stream(packet.ComponentPackets)
					.filter(c -> c.TypeString.equals("CharacterStats"))
					.findFirst()
					.get();

			Map<String, Object> vars = stats.Variables;

			if (packet.ObjectID
				.equals("b1a7e809-0000-0000-0000-000000000000")) {

				mightUpdated = (int) vars.get("BaseMight") == 30;
			}

			if (packet.ObjectID
				.equals("09517a0d-4fec-407c-a749-a531f3be64e0")) {

				resolveUpdated = (int) vars.get("BaseResolve") == 50;
			}
		}

		assertTrue(mightUpdated && resolveUpdated);
	}
}
