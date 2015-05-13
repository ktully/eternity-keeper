package uk.me.mantas.eternity.save;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SaveGameExtractor {

	private static final String[] REQUIRED_FILES = {
		"0.png"
		, "screenshot.png"
		, "saveinfo.xml"
	};

	private static final String[] OPTIONAL_FILES = {
		"1.png"
		, "2.png"
		, "3.png"
		, "4.png"
		, "5.png"
	};

	private String savesLocation;
	private File workingDirectory;

	public SaveGameExtractor (String savesLocation, File workingDirectory)
		throws NoSavesFoundException {

		this.savesLocation = savesLocation;
		this.workingDirectory = workingDirectory;

		Optional<SaveGameInfo[]> saveGameInfo = unpackAllSaves();

		if (!saveGameInfo.isPresent()) {
			throw new NoSavesFoundException();
		}
	}

	private File unpackSave (File save) {
		String destinationPath =
			new File(workingDirectory, save.getName()).getAbsolutePath();

		try {
			ZipFile archive = new ZipFile(save);
			archive.extractAll(destinationPath);
			return new File(destinationPath);
		} catch (ZipException e) {
			System.err.printf(
				"Unable to unzip '%s': %s%n"
				, save.getAbsolutePath()
				, e.getMessage());
		}

		return null;
	}

	private SaveGameInfo extractInfo (File saveFolder) {
		File[] contents = saveFolder.listFiles();

		if (contents == null) {
			System.err.printf(
				"Unzip resulted in 0 files for '%s'.%n"
				, saveFolder.getAbsolutePath());

			return null;
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

			return null;
		}

		return null;
	}

	private Optional<SaveGameInfo[]> unpackAllSaves () {
		File savesDirectory = new File(savesLocation);

		if (!savesDirectory.exists()) {
			return Optional.empty();
		}

		File[] saves = savesDirectory.listFiles();
		if (saves == null || saves.length < 1) {
			return Optional.empty();
		}

		SaveGameInfo[] info = Arrays.stream(saves)
			.<File>map(this::unpackSave)
			.<File>filter(a -> a != null)
			.<SaveGameInfo>map(this::extractInfo)
			.<SaveGameInfo>filter(a -> a != null)
			.toArray(SaveGameInfo[]::new);

		return Optional.of(info);
	}

	public static class NoSavesFoundException extends Exception {
		public NoSavesFoundException () {
			super();
		}
	}
}
