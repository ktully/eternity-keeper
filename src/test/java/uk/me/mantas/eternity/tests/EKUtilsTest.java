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

import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;

import static org.junit.Assert.assertArrayEquals;

public class EKUtilsTest {
	@Test
	public void removeBOMTest () {
		byte[] data = new byte[]{-17, -69, -65, 100, 97, 116, 97};
		byte[] actual = EKUtils.removeBOM(data);
		byte[] expected = new byte[]{100, 97, 116, 97};
		assertArrayEquals(expected, actual);
	}

	@Test
	public void addBOMTest () {
		byte[] actual = EKUtils.addBOM(new byte[]{});
		byte[] expected = new byte[]{-17, -69, -65};
		assertArrayEquals(expected, actual);
	}
}
