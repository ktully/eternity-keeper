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

package uk.me.mantas.eternity.tests.serializer;

import com.google.common.io.RecursiveDeleteOption;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.me.mantas.eternity.serializer.Serializer;
import uk.me.mantas.eternity.serializer.SerializerFormat;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class SerializerTest extends TestHarness {

	@Test
	public void serializesSaveFile () throws URISyntaxException, IOException {
		final File saveFile = new File(getClass().getResource("/MobileObjects.save").toURI());
		final File saveOutputFile = Files.createTempFile(PREFIX, null).toFile();

		try {
			reserializeFile(saveFile, saveOutputFile, SerializerFormat.PRESERVE);

			assertFileContentsEquals(saveFile, saveOutputFile);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void serializesWindowsStoreToSteamSaveFile () throws URISyntaxException, IOException {
		final File saveFile = new File(getClass().getResource("/SerializerTest/windowStoreSave/MobileObjects.save").toURI());
		final File saveOutputFile = Files.createTempFile(PREFIX, null).toFile();

		try {
			reserializeFile(saveFile, saveOutputFile, SerializerFormat.UNITY_2017);

			final File expectedSaveFile = new File(getClass().getResource("/SerializerTest/windowStoreSaveConverted/MobileObjects.save").toURI());
			assertFileContentsEquals(expectedSaveFile, saveOutputFile);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void convertWindowsStoreToSteamSaveFiles () throws URISyntaxException, IOException {
		final File inputDir = new File(getClass().getResource("/SerializerTest/windowStoreSave/").toURI());
		final List<File> inputFiles = Arrays.asList(inputDir.listFiles());

		final Path outputDirPath = Files.createTempDirectory(PREFIX);

		try {
			for (File inputFile : inputFiles) {
				final String inputFilename = inputFile.getName();
				final Path outputFilePath = outputDirPath.resolve(inputFilename);

				assertTrue(outputFilePath.toFile().createNewFile());

				if (inputFilename.endsWith(".save") || inputFilename.endsWith(".lvl")) {
					reserializeFile(inputFile, outputFilePath.toFile(), SerializerFormat.UNITY_2017);
				} else {
					final byte[] input = FileUtils.readFileToByteArray(inputFile);
					Files.write(outputFilePath, input);
				}
			}

			// TODO: optimize performance - convert in parallel threads

			// check the result
			// TODO: factor out to compare directory contents or similar. Or does guava have this already?
			final List<File> outputFiles = Arrays.asList(outputDirPath.toFile().listFiles());

			final File expectedDir = new File(getClass().getResource("/SerializerTest/windowStoreSaveConverted/").toURI());
			final List<String> expectedFilenames = Arrays.asList(expectedDir.list());

			assertEquals("number of files", expectedFilenames.size(), outputFiles.size());

			for (File outputFile : outputFiles) {
				String outputFilename = outputFile.getName();
				assertTrue(expectedFilenames.contains(outputFilename));

				final byte[] actual = FileUtils.readFileToByteArray(outputFile);

				final File expectedFile = expectedDir.toPath().resolve(outputFilename).toFile();
				final byte[] expected = FileUtils.readFileToByteArray(expectedFile);
				assertArrayEquals(outputFilename + " contents", expected, actual);
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void reserializeFile(File input, File output, SerializerFormat outputFormat) throws IOException {
		try {
			final SharpSerializer deserializer = new SharpSerializer(input.getAbsolutePath());
			final List<Property> deserialized = new ArrayList<>();
			final Optional<Property> objectCount = deserializer.deserialize();
			final int count = (int) objectCount.get().obj;

			for (int i = 0; i < count; i++) {
				final Optional<Property> obj = deserializer.deserialize();
				deserialized.add(obj.get());
			}

			final SharpSerializer serializer =
					new SharpSerializer(output.getAbsolutePath(), outputFormat);

			serializer.serialize(objectCount.get());
			for (final Property obj : deserialized) {
				serializer.serialize(obj);
			}
		} catch (final Exception e) {
			throw e;
		}
	}

	public static void assertFileContentsEquals(File expectedFile, File actualFile) throws IOException {
		try {
			final byte[] actual = FileUtils.readFileToByteArray(actualFile);
			final byte[] expected = FileUtils.readFileToByteArray(expectedFile);

			assertArrayEquals(expected, actual);
		} catch (final Exception e) {
			throw e;
		}
	}
}
