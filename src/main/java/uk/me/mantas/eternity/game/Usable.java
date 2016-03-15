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

public class Usable {
	public GameObject[] AdditionalInteractionObjects;
	public GameObject InteractionObject;
	public Usable.UseAnimation Animation;
	public float UsableRadius;
	public float ArrivalRadius;
	public boolean IsUsable;
	public boolean IsVisible;
	public boolean IsRevealed;
	public boolean HasInteractionObject;

	public enum UseAnimation {
		None
		, High
		, Low
	}
}
