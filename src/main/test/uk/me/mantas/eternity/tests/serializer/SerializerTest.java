package uk.me.mantas.eternity.tests.serializer;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.me.mantas.eternity.serializer.SharpSerializer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class SerializerTest {
	@Test
	public void serializesSaveFile ()
		throws URISyntaxException, IOException {

		File saveFile =
			new File(
				this.getClass().getResource("/MobileObjects.save").toURI());

		SharpSerializer deserializer =
			new SharpSerializer(saveFile.getAbsolutePath());

		List<Object> deserialized = new ArrayList<>();

		Optional<Object> objectCount = deserializer.deserialize();
		int count = (int) objectCount.get();

		for (int i = 0; i < count; i++) {
			Optional<Object> obj = deserializer.deserialize();
			deserialized.add(obj.get());
		}

		File saveOutputFile = Files.createTempFile(null, null).toFile();

		try {
			SharpSerializer serializer =
				new SharpSerializer(saveOutputFile.getAbsolutePath());

			serializer.serialize(deserialized.size());
			for (Object obj : deserialized) {
				serializer.serialize(obj);
			}

			byte[] actual = FileUtils.readFileToByteArray(saveOutputFile);
			byte[] expected = FileUtils.readFileToByteArray(saveFile);

			assertArrayEquals(expected, actual);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			assertTrue(saveOutputFile.delete());
		}
	}
}
