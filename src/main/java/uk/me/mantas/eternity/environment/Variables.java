/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2015 Kim Mantas
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

package uk.me.mantas.eternity.environment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Variables {
	public enum Key {
		USERPROFILE
		, HOME
		, SYSTEMDRIVE
		, XDG_DATA_HOME
	}

	private final Map<Key, String> variables = new HashMap<>();

	Variables () {
		Arrays.stream(Key.class.getEnumConstants()).forEach((variable) -> {
			String value = System.getenv(variable.name());
			if (value == null || value.equals("")) {
				value = null;
			}

			variables.put(variable, value);
		});
	}

	public Optional<String> get (final Key k) {
		return Optional.ofNullable(variables.get(k));
	}

	public void set (final Key k, final String v) {
		variables.put(k, v);
	}
}
