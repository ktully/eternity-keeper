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

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joox.Match;
import org.w3c.dom.DOMException;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.joox.JOOX.$;

public class SaveGameInfo {
	private static final Logger logger = Logger.getLogger(SaveGameInfo.class);
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
	public String absolutePath;
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

		absolutePath = saveFolder.getAbsolutePath();

		try {
			extractGUIDAndSystemName(saveFolder);
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error(
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
			logger.error(
				"Error reading image files: %s%n"
				, e.getMessage());
		}

		return false;
	}

	private boolean parseSaveInfoXML (File saveInfoXML) {
		try {
			String contents = new String(
				EKUtils.removeBOM(FileUtils.readFileToByteArray(saveInfoXML))
				, "UTF-8");

			Match xml = $(contents);
			playerName = xml.find("Simple[name='PlayerName']").attr("value");
			sceneTitle = xml.find("Simple[name='SceneTitle']").attr("value");
			difficulty = xml.find("Simple[name='Difficulty']").attr("value");
			userSaveName = xml.find("Simple[name='UserSaveName']").attr("value");
			chapter = xml.find("Simple[name='Chapter']").attr("value", Integer.class);
			trialOfIron = xml.find("Simple[name='TrialOfIron']").attr("value", Boolean.class);
			String timestampText = xml.find("Simple[name='RealTimestamp']").attr("value");
			timestamp = dateFormatter.parseDateTime(timestampText);
			return true;
		} catch (DOMException e) {
			logger.error(
				"Error parsing %s: %s%n"
				, saveInfoXML.getAbsolutePath()
				, e.getMessage());
		} catch (IOException e) {
			logger.error(
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

	// TODO: refactor to update an opened saveinfo
	static void updateSaveInfo (File saveDirectory, String newUserSaveName)
			throws IOException {

		File saveinfoXML = new File(saveDirectory, "saveinfo.xml");
		String contents = new String(
				EKUtils.removeBOM(FileUtils.readFileToByteArray(saveinfoXML))
				, "UTF-8");

		ByteArrayOutputStream newContentsStream = new ByteArrayOutputStream(contents.length());
		try {
			Match xml = $(contents);
			xml.find("Simple[name='UserSaveName']").attr("value", newUserSaveName);
			xml.write(newContentsStream);
		} catch (DOMException e) {

			logger.error(
					"Error parsing copied saveinfo '%s': %s%n"
					, saveinfoXML.getAbsolutePath()
					, e.getMessage());
		}

		String newContents = newContentsStream.toString("UTF-8");
		byte[] newContentsBytes = newContents.getBytes();
		if (newContentsBytes[0] != -17) {
			newContentsBytes = EKUtils.addBOM(newContentsBytes);
		}

		FileUtils.writeByteArrayToFile(saveinfoXML, newContentsBytes, false);
	}
}
