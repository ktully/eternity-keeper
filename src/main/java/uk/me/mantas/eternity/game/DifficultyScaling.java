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

package uk.me.mantas.eternity.game;

public class DifficultyScaling {
	public enum Scaler {
		NONE(0)
		, PX1_HIGH_LEVEL(1)
		, ACT4_HIGH_LEVEL(2)
		, PX1_HIGH_LEVEL_AND_ACT4_HIGH_LEVEL(3)
		, ELMSHORE_HIGH_LEVEL(4)
		, PX1_HIGH_LEVEL_AND_ELMSHORE_HIGH_LEVEL(5)
		, PX2_HIGH_LEVEL(8)
		, PX2_HIGH_LEVEL_AND_ACT4_HIGH_LEVEL(10)
		, PX2_HIGH_LEVEL_AND_ELMSHORE_HIGH_LEVEL(12);

		public int n;
		Scaler (int n) {
			this.n = n;
		}
	}
}
