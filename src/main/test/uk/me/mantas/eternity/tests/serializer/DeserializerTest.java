package uk.me.mantas.eternity.tests.serializer;

import org.junit.Test;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeserializerTest {
	@Test
	public void deserializesSaveFile ()
		throws URISyntaxException, FileNotFoundException {

		File saveFile =
			new File(
				this.getClass().getResource("/MobileObjects.save").toURI());

		SharpSerializer deserializer =
			new SharpSerializer(saveFile.getAbsolutePath());

		List<Object> deserialized = new ArrayList<>();

		Optional<Property> objectCount = deserializer.deserialize();
		assertTrue(objectCount.isPresent());

		int count = (int) objectCount.get().obj;
		assertEquals(17, count);

		for (int i = 0; i < count; i++) {
			Optional<Property> obj = deserializer.deserialize();
			assertTrue(obj.isPresent());
			deserialized.add(obj.get());
		}

		assertEquals(17, deserialized.size());
	}
}
