/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2016 the authors.
 * <p>
 * Eternity Keeper is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * Eternity Keeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.me.mantas.eternity;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import se.softhouse.jargo.Argument;
import se.softhouse.jargo.ArgumentException;
import se.softhouse.jargo.CommandLineParser;
import se.softhouse.jargo.ParsedArguments;
import uk.me.mantas.eternity.environment.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.google.common.collect.Range.atLeast;
import static se.softhouse.jargo.Arguments.*;

public class TestEnvironment {
	private static final class Args {
		private static final int DEFAULT_NUM_SAVES = 3;
		private static final String DEFAULT_WORKSPACE = "EK-TEST-ENV";

		private static final Argument<?> helpArgument = helpArgument("-h", "--help");
		private static final Argument<File> gameLocation =
			fileArgument()
				.required()
				.metaDescription("<game location>")
				.description("Your Pillars of Eternity install location.")
				.build();
		private static final Argument<File> saveLocation =
			fileArgument()
				.required()
				.metaDescription("<save location>")
				.description("Your save game location.")
				.build();
		private static final Argument<Integer> numSaves =
			integerArgument("-n", "--num-saves")
				.defaultValue(DEFAULT_NUM_SAVES)
				.description("The maximum number of saves to copy from your "
					+ "save directory to the test environment.")
				.limitTo(atLeast(1))
				.build();
		private static final Argument<String> workspace =
			stringArgument("-w", "--workspace")
				.defaultValue(DEFAULT_WORKSPACE)
				.description("The name of the directory to create as the test environment. "
					+ "This will get cleaned on every run of this script so change it if you "
					+ "want to preserve a previous environment.")
				.build();
		private static final Argument<Boolean> forceUpdate =
			optionArgument("-u", "--force-update")
				.description("Supplying this flag will create or rename the jar in the project's "
					+ "jar directory in order to force the editor to detect a new update.")
				.build();
	}

	public static void main (final String[] args) {
		try {
			final ParsedArguments arguments =
				CommandLineParser
					.withArguments(
						Args.gameLocation
						, Args.saveLocation
						, Args.numSaves
						, Args.workspace
						, Args.forceUpdate)
					.andArguments(Args.helpArgument)
					.parse(args);

			new TestEnvironment(arguments);
		} catch (final ArgumentException e) {
			System.out.printf("%s%n", e.getMessageAndUsage());
			System.exit(1);
		}
	}

	public TestEnvironment (final ParsedArguments args) {
		final File gameLocation = args.get(Args.gameLocation);
		final File saveLocation = args.get(Args.saveLocation);
		final Integer numSaves = args.get(Args.numSaves);
		final String workspacePrefix = args.get(Args.workspace);
		final Boolean forceUpdate = args.get(Args.forceUpdate);

		assert gameLocation != null
			&& saveLocation != null
			&& numSaves != null
			&& workspacePrefix != null
			&& forceUpdate != null;

		final File workspace = new File(System.getProperty("java.io.tmpdir"), workspacePrefix);

		if (workspace.exists()) {
			try {
				System.out.printf("Deleting old workspace...%n");
				FileUtils.deleteDirectory(workspace);
			} catch (final IOException e) {
				System.err.printf(
					"Unable to clean old workspace directory '%s': %s%n"
					, workspace.getAbsolutePath()
					, e.getMessage());
				System.exit(1);
			}
		}

		final File gameWorkspace = new File(workspace, "poe");
		final File saveWorkspace = new File(workspace, "save");

		final File portraitsLocation =
			gameLocation.toPath()
				.resolve("PillarsOfEternity_Data")
				.resolve("data")
				.resolve("art")
				.resolve("gui")
				.resolve("portraits")
				.toFile();

		final File portraitsWorkspace =
			gameWorkspace.toPath()
				.resolve("PillarsOfEternity_Data")
				.resolve("data")
				.resolve("art")
				.resolve("gui")
				.resolve("portraits")
				.toFile();

		if (!workspace.mkdirs() || !saveWorkspace.mkdir() || !portraitsWorkspace.mkdirs()) {
			System.err.printf(
				"Unable to create workspace directory '%s'.%n"
				, workspace.getAbsolutePath());
			System.exit(1);
		}

		try {
			System.out.printf("Copying portraits...%n");
			FileUtils.copyDirectory(portraitsLocation, portraitsWorkspace);
		} catch (final IOException e) {
			System.err.printf("Failed: %s%n", e.getMessage());
			System.exit(1);
		}

		System.out.printf("Copying saves...%n");

		final File[] saveGames = saveLocation.listFiles((dir, name) -> {
			return name.endsWith(".savegame");
		});

		Arrays.stream(saveGames).limit(numSaves).forEach(f -> {
			try {
				FileUtils.copyFileToDirectory(f, saveWorkspace);
			} catch (final IOException e) {
				System.err.printf(
					"Unable to copy '%s' to '%s': %s%n"
					, f.getAbsolutePath()
					, saveWorkspace.getAbsolutePath()
					, e.getMessage());
			}
		});

		final Settings settings = Settings.getInstance();
		settings.json.put("gameLocation", gameWorkspace.getAbsolutePath());
		settings.json.put("savesLocation", saveWorkspace.getAbsolutePath());
		settings.save();

		if (forceUpdate) {
			System.out.printf("Renaming jar...%n");
			final Environment environment = Environment.getInstance();
			Environment.joinAllWorkers();

			final File jarDirectory = environment.directory().jar();
			final File[] jars = jarDirectory.listFiles();
			if (jars == null || jars.length > 1) {
				System.err.printf(
					"Expected only one file in '%s', your project's environment is corrupt.%n"
					, jarDirectory.getAbsolutePath());
				System.exit(1);
			}

			final DateTime epoch = new DateTime(0).withZone(DateTimeZone.UTC);
			final DateTimeFormatter formatter =
				DateTimeFormat.forPattern("yyyyMMddHHmmss").withZoneUTC();
			final String newJarName = String.format("%s.jar", formatter.print(epoch));
			final File newJar = new File(jarDirectory, newJarName);

			if (jars.length < 1) {
				try {
					if (!newJar.createNewFile()) {
						throw new IOException("createNewFile() failed");
					}
				} catch (final IOException e) {
					System.err.printf(
						"Unable to create dummy jar file '%s': %s%n"
						, newJar.getAbsolutePath()
						, e.getMessage());
					System.exit(1);
				}
			} else {
				final File oldJar = jars[0];
				if (!oldJar.renameTo(newJar)) {
					System.err.printf(
						"Unable to rename %s to %s.%n"
						, oldJar.getAbsolutePath()
						, newJarName);
				}
			}
		}
	}
}
