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


package uk.me.mantas.eternity.environment;

import org.cef.OS;
import uk.me.mantas.eternity.Logger;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Environment {
	private static final Logger logger = Logger.getLogger(Environment.class);
	private static Environment instance = null;
	private static final long SHUTDOWN_TIMEOUT_SECONDS = 20;
	private ExecutorService workers =
		Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final Factories factories = new Factories();
	public Factories factory () { return factories; }

	private final Directories directories = new Directories();
	public Directories directory () { return directories; }

	private final State state = new State();
	public State state () { return state; }

	private final Configuration configuration = new Configuration();
	public Configuration config () { return configuration; }

	private final Variables variables = new Variables();
	public Variables variables () { return variables; }

	private final ClassFinder classFinder = new ClassFinder();
	public ClassFinder classFinder () { return classFinder; }

	private Environment () {}

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

	public ExecutorService workers () {
		return workers;
	}

	public boolean isWindows () {
		// This method just exists so we can mock it in tests.
		return OS.isWindows();
	}

	public static String detectPlatform () {
		if (OS.isWindows()) {
			if (System.getenv("ProgramFiles(x86)") == null) {
				return "win32";
			} else {
				return "win64";
			}
		} else {
			return "linux64";
		}
	}

	public Optional<Long> detectExeSize () {
		final File exe = new File("eternity.exe");
		if (!exe.exists() || !exe.isFile()) {
			return Optional.empty();
		}

		return Optional.of(exe.length());
	}

	public static void joinAllWorkers () {
		final ExecutorService workers = getInstance().workers();
		workers.shutdown();

		try {
			if (!workers.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				workers.shutdownNow();
				if (!workers.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
					logger.error("Thread pool did not terminate!%n");
				}
			}
		} catch (final InterruptedException e) {
			workers.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
