package uk.me.mantas.eternity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Environment {
	private static Environment instance = null;
	private static final long SHUTDOWN_TIMEOUT_SECONDS = 60;
	private ExecutorService workers =
		Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors());

	private Map<EnvKey, String> environmentVariables = new HashMap<>();
	private File settingsFile = new File(".", "settings.json");
	private File workingDirectory = new File(
		System.getProperty("java.io.tmpdir")
		, "EK-unpacked-saves");

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

	public void deleteWorkingDirectory () {
		try {
			FileUtils.deleteDirectory(getWorkingDirectory());
		} catch (IOException e) {
			System.err.printf(
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
			System.err.printf(
				"Unable to create working directory in '%s'.%n"
				, getWorkingDirectory().getAbsolutePath());
		}
	}

	public enum EnvKey {
		USERPROFILE
	}

	private Environment () {
		createWorkingDirectory();
		mapEnvironmentVariables();
	}

	private void mapEnvironmentVariables () {
		// A list of all the environment variables we care about:
		EnvKey[] variables = new EnvKey[]{EnvKey.USERPROFILE};

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
			if (!workers.awaitTermination(
					SHUTDOWN_TIMEOUT_SECONDS
					, TimeUnit.SECONDS)) {

				workers.shutdownNow();
				if (!workers.awaitTermination(
						SHUTDOWN_TIMEOUT_SECONDS
						, TimeUnit.SECONDS)) {

					System.err.printf("Thread pool did not terminate!%n");
				}
			}
		} catch (InterruptedException e) {
			workers.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
