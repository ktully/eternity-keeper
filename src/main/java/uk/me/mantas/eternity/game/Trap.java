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


package uk.me.mantas.eternity.game;

import uk.me.mantas.eternity.game.UnityEngine.GameObject;

public class Trap {
	public float TriggerDelay;
	public int MaxHitCount;
	public int TrapDifficulty;
	public float DisarmRadius;
	public boolean DestroyWhenDisarmed;
	public GenericAbility AbilityOverride;
	public GenericAbility AbilityOverrides;
	public boolean OneRandomAbility;
	public CharacterStats[] SourceOverride;
	public GameObject[] TargetOverride;
	public boolean SuppressTriggerBark;
	public float SelfDestructiveTime;
	public Item DisarmItem;
	public boolean AllowMultiple;
	public InteractablesDatabaseString DisplayName;
	public boolean FromItem;
	public boolean TrapCanPulse;
	public boolean ActivatesForAnyone;
	public boolean ButNotForCaster;
	public float TriggerRadius;
	public GameObject Owner;
	public boolean Visible;
	public boolean Disarmed;
	public boolean CanDisarm;
	public boolean IsPlayerOwnedTrap;
	public boolean IsWallTrap;
	public int TrapID;
	public float UsableRadius;
	public float ArrivalRadius;
	public boolean IsUsable;
}
