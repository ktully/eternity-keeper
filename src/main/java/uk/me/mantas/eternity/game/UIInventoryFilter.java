/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2016 the authors.
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

public class UIInventoryFilter {
	public UIInventoryFilter.ItemFilterType FilterType;
	public UISprite SelectedSprite;
	public UISprite ButtonSprite;
	public boolean Selected;

	public enum ItemFilterType {
		NONE(0)
		, WEAPONS(1)
		, ARMOR(2)
		, DEPRECATED_AMMO(4)
		, CLOTHING(8)
		, CONSUMABLES(16)
		, INGREDIENTS(32)
		, QUEST(64)
		, MISC(128);

		public int n;
		ItemFilterType (int n) {
			this.n = n;
		}
	}
}
