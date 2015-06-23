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


package uk.me.mantas.eternity.tests;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Settings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public abstract class TestHarness {
	protected static String PREFIX = "EK-";

	@Before
	public void setup () throws NoSuchFieldException, IllegalAccessException {
		Environment.initialise();
		Settings.clear();
	}

	@After
	public void cleanup () {
		File temp = new File(System.getProperty("java.io.tmpdir"));
		File[] files = temp.listFiles();

		if (files != null) {
			Arrays.stream(files)
				.filter((file) -> file.getName().startsWith(PREFIX))
				.forEach((file) -> {
					if (file.isDirectory()) {
						try {
							FileUtils.deleteDirectory(file);
						} catch (IOException e) {
							System.err.printf(
								"Unable to delete temporary directory '%s': "
									+ "%s%n"
								, file.getAbsoluteFile()
								, e.getMessage());
						}
					} else {
						assertTrue(file.delete());
					}
				});
		}
	}
}
