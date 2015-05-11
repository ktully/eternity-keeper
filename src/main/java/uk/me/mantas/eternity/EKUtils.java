package uk.me.mantas.eternity;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class EKUtils {
	@SuppressWarnings("EmptyCatchBlock")
	public static Rectangle getDefaultWindowBounds () {
		final double multiplier = 2d / 3d;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		double w = screenSize.width * multiplier;
		double h = screenSize.height * multiplier;
		double x = screenSize.width / 2 - w / 2;
		double y = screenSize.height / 2 - h / 2;

		JSONObject settings = Settings.getInstance().json;
		try { w = settings.getDouble("width"); } catch (JSONException e) {}
		try { h = settings.getDouble("height"); } catch (JSONException e) {}
		try { x = settings.getDouble("x"); } catch (JSONException e) {}
		try { y = settings.getDouble("y"); } catch (JSONException e) {}

		return new Rectangle((int) x, (int) y, (int) w, (int) h);
	}

	public static Optional<File> createTempDir (String prefix) {
		try {
			return Optional.of(Files.createTempDirectory(prefix).toFile());
		} catch (IOException e) {
			System.err.printf(
				"Error creating temp directory: %s%n"
				, e.getMessage());
		} catch (IllegalArgumentException e) {
			System.err.printf(
				"Unable to create temp directory with unique name: %s%n"
				, e.getMessage());
		}

		return Optional.<File>empty();
	}
}
