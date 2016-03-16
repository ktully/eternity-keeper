package uk.me.mantas.eternity.environment;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import uk.me.mantas.eternity.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ClassFinder {
	private static final Logger logger = Logger.getLogger(ClassFinder.class);
	private final Optional<ClassPath> classPath;

	ClassFinder () {
		classPath = getClassPath();
	}

	private Optional<ClassPath> getClassPath () {
		try {
			return Optional.of(ClassPath.from(getClass().getClassLoader()));
		} catch (final IOException e) {
			logger.error("Unable to scan classpath: %s", e.getMessage());
			return Optional.empty();
		}
	}

	public Set<ClassInfo> allClassesInPackage (final String pkg) {
		if (classPath.isPresent()) {
			return classPath.get().getTopLevelClasses(pkg);
		} else {
			return new HashSet<>();
		}
	}
}
