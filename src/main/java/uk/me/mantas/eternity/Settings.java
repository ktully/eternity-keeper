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


package uk.me.mantas.eternity;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class Settings {
	private static Settings instance = null;
	private final File settingsFile;
	public JSONObject json = new JSONObject();

	private Settings () {
		settingsFile = Environment.getInstance().getSettingsFile();

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

	public static void clear () {
		instance = null;
	}
}
