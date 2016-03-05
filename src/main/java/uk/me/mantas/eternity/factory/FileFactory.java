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


package uk.me.mantas.eternity.factory;

// This class exists purely to help with writing unit tests. We want to no longer instantiate file
// objects through new File(); as this cannot be mocked and it is important to be able to mock this
// functionality to avoid our tests becoming too slow with loads of I/O.

import java.io.File;
import java.net.URI;

public class FileFactory {
	public File create (final String filepath) {
		return new File(filepath);
	}

	public File create (final String parent, final String child) {
		return new File(parent, child);
	}

	public File create (final File parent, final String child) {
		return new File(parent, child);
	}

	public File create (final URI uri) {
		return new File(uri);
	}
}
