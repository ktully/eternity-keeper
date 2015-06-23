/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 Kim Mantas
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

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.me.mantas.eternity.save.SaveGameInfo.*;

public class SaveGameExtractor {
	private String savesLocation;
	private File workingDirectory;
	public AtomicInteger totalFiles = new AtomicInteger(0);
	public AtomicInteger currentCount = new AtomicInteger(0);

	public SaveGameExtractor (String savesLocation, File workingDirectory) {
		this.savesLocation = savesLocation;
		this.workingDirectory = workingDirectory;
	}

	private File unpackSave (File save) {
		String destinationPath =
			new File(workingDirectory, save.getName()).getAbsolutePath();

		try {
			ZipFile archive = new ZipFile(save);
			archive.extractAll(destinationPath);
			currentCount.getAndIncrement();
			return new File(destinationPath);
		} catch (ZipException e) {
			System.err.printf(
				"Unable to unzip '%s': %s%n"
				, save.getAbsolutePath()
				, e.getMessage());
		}

		return null;
	}

	private Optional<SaveGameInfo> extractInfo (File saveFolder) {
		File[] contents = saveFolder.listFiles();

		if (contents == null) {
			System.err.printf(
				"Unzip resulted in 0 files for '%s'.%n"
				, saveFolder.getAbsolutePath());

			return Optional.empty();
		}

		Set<String> requiredFiles =
			new HashSet<>(Arrays.asList(REQUIRED_FILES));

		Set<String> optionalFiles =
			new HashSet<>(Arrays.asList(OPTIONAL_FILES));

		Map<String, File> importantFiles = Arrays.stream(contents)
			.filter(f ->
				requiredFiles.contains(f.getName())
				|| optionalFiles.contains(f.getName()))
			.collect(Collectors.toMap(File::getName, Function.identity()));

		if (!importantFiles.keySet().containsAll(requiredFiles)) {
			System.err.printf(
				"All required files not present in extracted save game '%s'.%n"
				, saveFolder.getAbsolutePath());

			return Optional.empty();
		}

		try {
			return Optional.of(new SaveGameInfo(saveFolder, importantFiles));
		} catch (SaveFileInfoException e) {
			return Optional.empty();
		}
	}

	public Optional<SaveGameInfo[]> unpackAllSaves () {
		File savesDirectory = new File(savesLocation);

		if (!savesDirectory.exists()) {
			return Optional.empty();
		}

		File[] saves = savesDirectory.listFiles();
		if (saves == null) {
			return Optional.empty();
		}

		File[] saveFiles = Arrays.stream(saves)
			.filter(File::isFile)
			.toArray(File[]::new);

		totalFiles.set(saveFiles.length);
		currentCount.set(0);
		SaveGameInfo[] info = Arrays.stream(saveFiles)
			.<File>map(this::unpackSave)
			.<File>filter(a -> a != null)
			.<Optional<SaveGameInfo>>map(this::extractInfo)
			.<Optional<SaveGameInfo>>filter(Optional::isPresent)
			.<SaveGameInfo>map(Optional::get)
			.toArray(SaveGameInfo[]::new);

		return Optional.of(info);
	}
}
