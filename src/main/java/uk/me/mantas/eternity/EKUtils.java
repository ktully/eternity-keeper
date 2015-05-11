package uk.me.mantas.eternity;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class EKUtils {
	public static Dimension getBestWindowSize () {
		final double multiplier = 2d / 3d;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		return new Dimension(
			(int) (screenSize.width * multiplier)
			, (int) (screenSize.height * multiplier));
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
