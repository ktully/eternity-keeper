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


package uk.me.mantas.eternity.save;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import uk.me.mantas.eternity.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.me.mantas.eternity.save.SaveGameInfo.*;

public class SaveGameExtractor {
	private static final Logger logger = Logger.getLogger(SaveGameExtractor.class);
	private final String savesLocation;
	private final File workingDirectory;
	public final AtomicInteger totalFiles = new AtomicInteger(0);
	public final AtomicInteger currentCount = new AtomicInteger(0);

	public SaveGameExtractor (final String savesLocation, final File workingDirectory) {
		this.savesLocation = savesLocation;
		this.workingDirectory = workingDirectory;
	}

	private File unpackSave (final File save) {
		final String destinationPath = new File(workingDirectory, save.getName()).getAbsolutePath();

		try {
			final ZipFile archive = new ZipFile(save);
			archive.extractAll(destinationPath);
			currentCount.getAndIncrement();
			return new File(destinationPath);
		} catch (final ZipException e) {
			logger.error("Unable to unzip '%s': %s%n", save.getAbsolutePath(), e.getMessage());
		}

		return null;
	}

	static Optional<SaveGameInfo> extractInfo (final File saveFolder) {
		final File[] contents = saveFolder.listFiles();

		if (contents == null) {
			logger.error("Unzip resulted in 0 files for '%s'.%n", saveFolder.getAbsolutePath());
			return Optional.empty();
		}

		final Set<String> requiredFiles = new HashSet<>(Arrays.asList(REQUIRED_FILES));
		final Set<String> optionalFiles = new HashSet<>(Arrays.asList(OPTIONAL_FILES));
		final Map<String, File> importantFiles = Arrays.stream(contents)
			.filter(f -> requiredFiles.contains(f.getName()) || optionalFiles.contains(f.getName()))
			.collect(Collectors.toMap(File::getName, Function.identity()));

		if (!importantFiles.keySet().containsAll(requiredFiles)) {
			logger.error(
				"All required files not present in extracted save game '%s'.%n"
				, saveFolder.getAbsolutePath());

			return Optional.empty();
		}

		try {
			return Optional.of(new SaveGameInfo(saveFolder, importantFiles));
		} catch (final SaveFileInfoException e) {
			return Optional.empty();
		}
	}

	public Optional<SaveGameInfo[]> unpackAllSaves () {
		final File savesDirectory = new File(savesLocation);
		if (!savesDirectory.exists()) {
			return Optional.empty();
		}

		final File[] saves = savesDirectory.listFiles();
		if (saves == null) {
			return Optional.empty();
		}

		final File[] saveFiles = Arrays.stream(saves).filter(File::isFile).toArray(File[]::new);
		totalFiles.set(saveFiles.length);
		currentCount.set(0);

		Arrays.sort(saveFiles); // Just for determinism in the tests.
		final SaveGameInfo[] info =
			Arrays.stream(saveFiles)
				.map(this::unpackSave)
				.filter(a -> a != null)
				.map(SaveGameExtractor::extractInfo)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toArray(SaveGameInfo[]::new);

		return Optional.of(info);
	}
}
