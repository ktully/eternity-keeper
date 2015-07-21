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

import org.apache.commons.io.FileUtils;
import uk.me.mantas.eternity.factory.ComponentDeserializerFactory;
import uk.me.mantas.eternity.factory.FileFactory;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static uk.me.mantas.eternity.handlers.DownloadUpdate.UpdateDownloader;
import static uk.me.mantas.eternity.handlers.ListSavedGames.SaveInfoLister;

// Pretty messy class that we use to manage the global state environment of the app as it runs. We
// can imagine this part as the server state while the UI code handles the client state.

// The main advantage of this is it allows us a fairly bare-bones method of dependency injection.
// The Environment can change depending on whether the app is in a user-interaction environment or
// if it is in a test environment.

public class Environment {
	// Factories.
	private final SharpSerializerFactory sharpSerializerFactory = new SharpSerializerFactory();
	public SharpSerializerFactory sharpSerializer () { return sharpSerializerFactory; }
	private final FileFactory fileFactory = new FileFactory();
	public FileFactory fileFactory () { return fileFactory; }

	private final ComponentDeserializerFactory componentDeserializer =
		new ComponentDeserializerFactory();

	public ComponentDeserializerFactory componentDeserializer () { return componentDeserializer; }

	// Application state and configuration.
	private static final Logger logger = Logger.getLogger(Environment.class);
	public static final String PILLARS_DATA_DIR = "PillarsOfEternity_Data";
	public boolean closing = false;

	private static Environment instance = null;
	private static final long SHUTDOWN_TIMEOUT_SECONDS = 20;
	private ExecutorService workers =
		Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors());

	private String companionPortraitPath = "data/art/gui/portraits/companion/portrait_%s_lg.png";
	private SaveInfoLister currentSaveLister = null;
	private UpdateDownloader currentUpdateDownloader = null;
	private Map<EnvKey, String> environmentVariables = new HashMap<>();
	private File previousSaveDirectory = null;
	private File settingsFile = new File(".", "settings.json");
	private File jarDirectory = new File("jar");
	private File uiDirectory = new File("src");
	private File workingDirectory = new File(
		System.getProperty("java.io.tmpdir")
		, "EK-unpacked-saves");

	public List<String> possibleInstallationLocations =
		new ArrayList<String>()	{{
			add("Program Files\\GOG Games\\Pillars of Eternity");
			add("Program Files (x86)\\GOG Games\\Pillars of Eternity");
			add("Program Files\\Steam\\SteamApps\\common\\Pillars of Eternity");
			add("Program Files (x86)\\Steam\\SteamApps\\"
				+ "common\\Pillars of Eternity");
		}};

	// For some reason, certain companions are referred to in the game code differently to their
	// actual name.
	public static final Map<String, String> companionNameMap =
		new HashMap<String, String>() {{
			put("GM", "Grieving Mother");
			put("GGP", "Durance");
		}};

	public String getCompanionPortraitPath () {
		return companionPortraitPath;
	}

	public File getJarDirectory () {
		return jarDirectory;
	}

	public void setJarDirectory (File jarDirectory) {
		this.jarDirectory = jarDirectory;
	}

	public File getUiDirectory () {
		return uiDirectory;
	}

	public void setUiDirectory (File uiDirectory) {
		this.uiDirectory = uiDirectory;
	}

	public File getSettingsFile () {
		return settingsFile;
	}

	public void setSettingsFile (File settingsFile) {
		this.settingsFile = settingsFile;
	}

	public ExecutorService getWorkers () {
		return workers;
	}

	public File getWorkingDirectory () {
		return workingDirectory;
	}

	public void setWorkingDirectory (File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public SaveInfoLister getCurrentSaveLister () {
		return currentSaveLister;
	}

	public void setCurrentSaveLister (SaveInfoLister currentSaveLister) {
		this.currentSaveLister = currentSaveLister;
	}

	public UpdateDownloader getCurrentUpdateDownloader () {
		return currentUpdateDownloader;
	}

	public void setCurrentUpdateDownloader (UpdateDownloader currentUpdateDownloader) {
		this.currentUpdateDownloader = currentUpdateDownloader;
	}

	public void deleteWorkingDirectory () {
		try {
			FileUtils.deleteDirectory(getWorkingDirectory());
		} catch (IOException e) {
			logger.error(
				"Unable to delete working directory at '%s': %s%n"
				, getWorkingDirectory().getAbsolutePath()
				, e.getMessage());
		}
	}

	public void emptyWorkingDirectory () {
		deleteWorkingDirectory();
		createWorkingDirectory();
	}

	private void createWorkingDirectory () {
		if (!getWorkingDirectory().mkdir()) {
			logger.error(
				"Unable to create working directory in '%s'.%n"
				, getWorkingDirectory().getAbsolutePath());
		}
	}

	public File getPreviousSaveDirectory () {
		return previousSaveDirectory;
	}

	public void setPreviousSaveDirectory (File previousSaveDirectory) {
		this.previousSaveDirectory = previousSaveDirectory;
	}

	public enum EnvKey {
		USERPROFILE
		, HOME
		, SYSTEMDRIVE
	}

	private Environment () {
		createWorkingDirectory();
		mapEnvironmentVariables();
	}

	private void mapEnvironmentVariables () {
		// A list of all the environment variables we care about:
		EnvKey[] variables = new EnvKey[]{
			EnvKey.USERPROFILE
			, EnvKey.HOME
			, EnvKey.SYSTEMDRIVE};

		Arrays.stream(variables).forEach((variable) -> {
			String value = System.getenv(variable.name());
			if (value == null || value.equals("")) {
				value = null;
			}

			environmentVariables.put(variable, value);
		});
	}

	public Optional<String> getEnvVar (EnvKey key) {
		return Optional.ofNullable(environmentVariables.get(key));
	}

	public void setEnvVar (EnvKey key, String value) {
		environmentVariables.put(key, value);
	}

	public void setEnvVars (Map<EnvKey, String> newVariables) {
		environmentVariables.putAll(newVariables);
	}

	public static Environment getInstance () {
		if (instance == null) {
			initialise();
		}

		return instance;
	}

	public static void initialise () {
		if (instance != null) {
			joinAllWorkers();
		}

		instance = new Environment();
	}

	public static void joinAllWorkers () {
		ExecutorService workers = getInstance().getWorkers();
		workers.shutdown();

		try {
			if (!workers.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				workers.shutdownNow();
				if (!workers.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
					logger.error("Thread pool did not terminate!%n");
				}
			}
		} catch (InterruptedException e) {
			workers.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
