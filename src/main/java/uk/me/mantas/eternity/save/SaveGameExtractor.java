package uk.me.mantas.eternity.save;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.me.mantas.eternity.save.SaveGameInfo.*;

public class SaveGameExtractor {
	private String savesLocation;
	private File workingDirectory;

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
		if (saves == null || saves.length < 1) {
			return Optional.empty();
		}

		SaveGameInfo[] info = Arrays.stream(saves)
			.<File>filter(File::isFile)
			.<File>map(this::unpackSave)
			.<File>filter(a -> a != null)
			.<Optional<SaveGameInfo>>map(this::extractInfo)
			.<Optional<SaveGameInfo>>filter(Optional::isPresent)
			.<SaveGameInfo>map(Optional::get)
			.toArray(SaveGameInfo[]::new);

		return Optional.of(info);
	}
}
