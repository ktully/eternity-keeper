package uk.me.mantas.eternity.save;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public class SaveGameExtractor {

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

		Optional<File> saveInfoXML = Arrays.stream(contents)
			.filter(f -> f.getName().equals("saveinfo.xml"))
			.findFirst();

		if (!saveInfoXML.isPresent()) {
			System.err.printf(
				"No saveinfo.xml present in extracted save game '%s'.%n"
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
