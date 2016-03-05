/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2015 the authors.
 * <p>
 * Eternity Keeper is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * Eternity Keeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.me.mantas.eternity;

import org.slf4j.LoggerFactory;

public class Logger {
	private final Class cls;
	private final org.slf4j.Logger slfLogger;

	private Logger (final Class cls) {
		this.cls = cls;
		slfLogger = LoggerFactory.getLogger(cls);
	}

	public void error (final String format, final Object... varargs) {
		final String className = cls.getSimpleName();
		final int lineNumber = getCallerLineNumber();

		String displayClassName = String.format("%-20s", className);
		if (displayClassName.length() > 20) {
			displayClassName = displayClassName.substring(0, 21);
		}

		final String displayLineNumber = String.format("%-3d", lineNumber);

		slfLogger.error(
			displayClassName + "  "
			+ displayLineNumber + "  "
			+ "ERROR  "
			+ String.format(format, varargs));
	}

	private int getCallerLineNumber () {
		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		final int callerIndex = Math.min(3, stackTrace.length - 1);
		return stackTrace[callerIndex].getLineNumber();
	}

	public static Logger getLogger (final Class cls) {
		return new Logger(cls);
	}
}
