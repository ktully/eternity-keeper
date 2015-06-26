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

import org.json.JSONException;
import org.json.JSONObject;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EKUtils {
	private static final Logger logger = Logger.getLogger(EKUtils.class);

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
			logger.error(
				"Error creating temp directory: %s%n"
				, e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error(
				"Unable to create temp directory with unique name: %s%n"
				, e.getMessage());
		}

		return Optional.<File>empty();
	}

	public static byte[] removeBOM (byte[] bytes) {
		if (bytes.length < 3) {
			return bytes;
		}

		if (bytes[0] == -17 && bytes[1] == -69 && bytes[2] == -65) {
			byte[] newBytes = new byte[bytes.length - 3];
			System.arraycopy(bytes, 3, newBytes, 0, bytes.length - 3);
			return newBytes;
		}

		return bytes;
	}

	public static byte[] addBOM (byte[] bytes) {
		byte[] newBytes = new byte[bytes.length + 3];
		newBytes[0] = -17; newBytes[1] = -69; newBytes[2] = -65;
		System.arraycopy(bytes, 0, newBytes, 3, bytes.length);

		return newBytes;
	}

	public static String removeExtension (String s) {
		if (s == null || s.length() < 1 || !s.contains(".")) {
			return s;
		}

		return s.substring(0, s.lastIndexOf("."));
	}

	public static Optional<String> getExtension (String s) {
		if (s == null || s.length() < 1 || !s.contains(".")) {
			return Optional.empty();
		}

		return Optional.of(s.substring(s.lastIndexOf(".") + 1, s.length()));
	}

	public static long getTimestampOfLatestJar (File[] jars) {
		java.util.List<Long> timestamps =
			Arrays.stream(jars)
				.map(File::getName)
				.map(EKUtils::removeExtension)
				.map(Long::parseLong)
				.collect(Collectors.toList());

		return Collections.max(timestamps);
	}

	public static List<Property> deserializeFile (File file)
		throws FileNotFoundException {

		List<Property> deserialized = new ArrayList<>();

		SharpSerializer deserializer =
			new SharpSerializer(file.getAbsolutePath());

		Optional<Property> objCountProp = deserializer.deserialize();
		if (!objCountProp.isPresent()) {
			return deserialized;
		}

		int count = (int) objCountProp.get().obj;
		for (int i = 0; i < count; i++) {
			Optional<Property> property = deserializer.deserialize();
			if (property.isPresent()) {
				deserialized.add(property.get());
			}
		}

		return deserialized;
	}
}
