package uk.me.mantas.eternity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Harness {
	private static Harness instance = null;
	private Map<EnvKey, String> environmentVariables = new HashMap<>();

	public enum EnvKey {
		USERPROFILE
	}

	private Harness () {
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
		String value = environmentVariables.get(key);
		if (value == null) {
			return Optional.<String>empty();
		}

		return Optional.of(value);
	}

	public void setEnvVar (EnvKey key, String value) {
		environmentVariables.put(key, value);
	}

	public void setEnvVars (Map<EnvKey, String> newVariables) {
		environmentVariables.putAll(newVariables);
	}

	public static Harness getInstance () {
		if (instance == null) {
			initialise();
		}

		return instance;
	}

	public static void initialise () {
		instance = new Harness();
	}
}
