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

import uk.me.mantas.eternity.game.UnityEngine.GameObject;

public class Container extends OCL {
	public DatabaseString LabelName;
	public String ManualLabelName;
	public GameObject NotEmptyVFX;
	public boolean DeleteMeIfEmpty;
	public boolean AreaLootable;
	public FactionName StealingFactionID;
	public Reputation.ChangeStrength StealingFactionAdjustment;
	public boolean AttackThief;
	public boolean AlliesAttackThief;
	public boolean fireItemTakenEventOnlyOnce;
	public boolean IsEmpty;
	public boolean HasInteracted;
	public boolean PlayerHasBeenCaughtStealingFrom;
	public boolean CanAreaLootContainer;
	public GameObject User;
	public boolean IsOwned;
	public boolean IsUsable;
}
