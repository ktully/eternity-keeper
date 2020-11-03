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
import uk.me.mantas.eternity.EKUtils;
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
	public void serializesLevelFile () throws URISyntaxException, IOException {
		final File saveFile = new File(getClass().getResource("/SerializerTest/windowStoreSave/AR_0701_Encampment.lvl").toURI());
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

		final Optional<File> outputDir = EKUtils.createTempDir(PREFIX);
		assertTrue(outputDir.isPresent());
		final Path outputDirPath = outputDir.get().toPath();

		try {
			for (File inputFile : inputFiles) {
				final String inputFilename = inputFile.getName();
				final File outputFile = new File(outputDir.get(), inputFilename);

				if (inputFilename.endsWith(".save") || inputFilename.endsWith(".lvl")) {
					assertTrue(outputFile.createNewFile());
					reserializeFile(inputFile, outputFile, SerializerFormat.UNITY_2017);
				} else {
					Files.copy(inputFile.toPath(), outputFile.toPath());
				}
			}

			// TODO: optimize performance - convert in parallel threads

			// check the result
			// TODO: factor out to EKUtils.compareDirectoryContents() or similar. Or does guava have this already?
			final List<File> outputFiles = Arrays.asList(outputDirPath.toFile().listFiles());

			final File expectedDir = new File(getClass().getResource("/SerializerTest/windowStoreSaveConverted/").toURI());
			final List<String> expectedFilenames = Arrays.asList(expectedDir.list());

			assertEquals("number of files", expectedFilenames.size(), outputFiles.size());

			for (File outputFile : outputFiles) {
				String outputFilename = outputFile.getName();
				assertTrue(expectedFilenames.contains(outputFilename));

				final File expectedFile = new File(expectedDir, outputFilename);
				assertFileContentsEquals(expectedFile, outputFile);
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

			assertArrayEquals(actualFile.getName() + " contents", expected, actual);
		} catch (final Exception e) {
			throw e;
		}
	}
}
