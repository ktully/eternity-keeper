package uk.me.mantas.eternity.save;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joox.Match;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.joox.JOOX.$;

public class SaveGameInfo {
	private static final DateTimeFormatter dateFormatter =
		DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss").withZoneUTC();

	public static final String[] REQUIRED_FILES = {
		"0.png"
		, "screenshot.png"
		, "saveinfo.xml"
	};

	public static final String[] OPTIONAL_FILES = {
		"1.png"
		, "2.png"
		, "3.png"
		, "4.png"
		, "5.png"
	};

	public String guid;
	public String systemName;
	public String playerName;
	public String sceneTitle;
	public int chapter;
	public boolean trialOfIron;
	public DateTime timestamp;
	public String userSaveName;
	public String difficulty;

	public String screenshot;
	public List<String> portraits = new ArrayList<>();

	public SaveGameInfo (File saveFolder, Map<String, File> infoFiles)
		throws SaveFileInfoException {

		try {
			extractGUIDAndSystemName(saveFolder);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.printf(
				"Save folder name was malformed: %s%n"
				, saveFolder.getName());

			throw new SaveFileInfoException();
		}

		boolean success = parseSaveInfoXML(infoFiles.get("saveinfo.xml"));
		success = success && encodeImageData(infoFiles);

		if (!success) {
			throw new SaveFileInfoException();
		}
	}

	private void extractGUIDAndSystemName (File saveFolder)
		throws ArrayIndexOutOfBoundsException {

		String[] nameComponents =
			saveFolder.getName().replace(".savegame", "").split(" ");

		guid = nameComponents[0];
		systemName = nameComponents[1];

		if (nameComponents.length > 2) {
			systemName =
				Arrays.stream(nameComponents, 2, nameComponents.length)
					.collect(Collectors.joining(" "));
		}
	}

	private boolean encodeImageData (Map<String, File> infoFiles) {
		try {
			byte[] screenshotData =
				FileUtils.readFileToByteArray(infoFiles.get("screenshot.png"));

			screenshot = Base64.getEncoder().encodeToString(screenshotData);

			for (int i = 0; i < 6; i++) {
				File imageFile = infoFiles.get(String.format("%d.png", i));
				if (imageFile == null) {
					break;
				}

				byte[] imageData = FileUtils.readFileToByteArray(imageFile);
				portraits.add(Base64.getEncoder().encodeToString(imageData));
			}

			return screenshot.length() > 0 && portraits.size() > 0;
		} catch (IOException e) {
			System.err.printf(
				"Error reading image files: %s%n"
				, e.getMessage());
		}

		return false;
	}

	private boolean parseSaveInfoXML (File saveInfoXML) {
		try {
			Match xml = $(saveInfoXML);

			playerName = xml.find("Simple[name='PlayerName']").attr("value");
			sceneTitle = xml.find("Simple[name='SceneTitle']").attr("value");
			difficulty = xml.find("Simple[name='Difficulty']").attr("value");

			userSaveName =
				xml.find("Simple[name='UserSaveName']").attr("value");

			chapter =
				xml.find("Simple[name='Chapter']").attr("value", Integer.class);

			trialOfIron =
				xml.find("Simple[name='TrialOfIron']")
					.attr("value", Boolean.class);

			String timestampText =
				xml.find("Simple[name='RealTimestamp']").attr("value");

			timestamp = dateFormatter.parseDateTime(timestampText);

			return true;
		} catch (SAXException e) {
			System.err.printf(
				"Error parsing %s: %s%n"
				, saveInfoXML.getAbsolutePath()
				, e.getMessage());
		} catch (IOException e) {
			System.err.printf(
				"Error reading %s: %s%n"
				, saveInfoXML.getAbsolutePath()
				, e.getMessage());
		}

		return false;
	}

	public static class SaveFileInfoException extends Exception {
		SaveFileInfoException () {
			super();
		}
	}
}
