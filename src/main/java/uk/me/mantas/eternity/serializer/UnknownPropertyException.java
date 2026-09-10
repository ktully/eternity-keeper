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


package uk.me.mantas.eternity.serializer;

// Thrown when a save file contains a property whose type we can't work out, because the class we
// are deserializing into has no field of that name and the file itself doesn't record the type.
// Since we don't know how many bytes the value occupies we can't skip over it, so every subsequent
// read would be reading from the wrong offset. Failing here keeps us from reporting a pile of
// nonsense errors from further down the stream. Almost always this means the game has been patched
// and one of the classes in uk.me.mantas.eternity.game needs the new field adding to it.

public class UnknownPropertyException extends RuntimeException {
	public final String propertyName;

	public UnknownPropertyException (final String propertyName) {
		super(String.format(
			"Unable to determine the type of property '%s', so the rest of the file can't be "
			+ "read. The save was probably written by a newer version of the game than this "
			+ "version of Eternity Keeper knows about."
			, propertyName));

		this.propertyName = propertyName;
	}
}
