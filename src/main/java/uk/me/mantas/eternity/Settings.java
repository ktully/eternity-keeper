package uk.me.mantas.eternity;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class Settings {
	private static Settings instance = null;
	private final File settingsFile;
	public JSONObject json = new JSONObject();

	private Settings () {
		settingsFile = Harness.getInstance().getSettingsFile();

		try {
			boolean fileCreated = settingsFile.createNewFile();
			if (fileCreated) {
				writeBlankJSON();
			} else {
				readSettingsJSON();
			}
		} catch (IOException e) {
			System.err.printf(
				"Error processing settings file: %s%n"
				, e.getMessage());
		}
	}

	private void readSettingsJSON () {
		try (Reader reader =
			     new BufferedReader(
				     new InputStreamReader(
					     new FileInputStream(settingsFile), "UTF-8"))) {

			json = new JSONObject(new JSONTokener(reader));
		} catch (IOException e) {
			System.err.printf(
				"Error reading file '%s': %s%n"
				, settingsFile.getAbsolutePath()
				, e.getMessage());
		}
	}

	private void writeBlankJSON () {
		try (Writer writer =
			     new BufferedWriter(
				     new OutputStreamWriter(
					     new FileOutputStream(settingsFile), "UTF-8"))) {

			writer.write("{}");
		} catch (IOException e) {
			System.err.printf(
				"Unable to write to '%s': %s%n"
				, settingsFile.getAbsolutePath()
				, e.getMessage());
		}
	}

	public void save () {
		try (Writer writer =
			     new BufferedWriter(
				     new OutputStreamWriter(
					     new FileOutputStream(settingsFile), "UTF-8"))) {

			writer.write(json.toString());
		} catch (IOException e) {
			System.err.printf(
				"Unable to save settings file: %s%n"
				, e.getMessage());
		}
	}

	public static Settings getInstance () {
		if (instance == null) {
			initialise();
		}

		return instance;
	}

	public static void initialise () {
		instance = new Settings();
	}
}
