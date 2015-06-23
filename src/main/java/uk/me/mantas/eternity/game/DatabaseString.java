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


package uk.me.mantas.eternity.game;

import com.google.common.primitives.UnsignedInteger;

public class DatabaseString {
	public UnsignedInteger PackageSerialized;
	public int StringIDSerialized;
	public StringTableType StringTableSerialized;

	public enum StringTableType {
		Abilities(6)
		, Afflictions(0x13)
		, AreaNotifications(9)
		, BackerContent(20)
		, Backstory(0x3ae)
		, Characters(4)
		, Cyclopedia(900)
		, Debug(12)
		, Factions(14)
		, Gui(1)
		, Interactables(10)
		, ItemMods(0x10)
		, Items(5)
		, LoadingTips(15)
		, Maps(0x11)
		, Recipes(13)
		, Stronghold(0x3aa)
		, Tutorial(7)
		, Unassigned(0);

		public int n;
		StringTableType (int n) {
			this.n = n;
		}
	}
}
