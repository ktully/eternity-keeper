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

public class ItemMod {
	public enum EnchantCategory {
		None
		, Quality
		, Lashing
		, Slaying
		, Proofing
		, Attributes
		, Unique
	}

	public enum TargetMode {
		Self
		, Enemy
	}

	public enum TriggerMode {
		OnUI
		, OnScoringCriticalHit
		, OnBeingCriticallyHit
		, OnUnconscious
		, AsSecondaryAttack
		, OnSpiritShift
	}
}
