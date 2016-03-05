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

public class UnityEngine {
	public static class Colour {
		public float r;
		public float g;
		public float b;
		public float a;
	}

	public static class Vector3 {
		public float x;
		public float y;
		public float z;
	}

	public static class Vector2 {
		public float x;
		public float y;
	}

	// Need to work out a better way of doing this:
	public enum HideFlags {
		None
		, HideInHierarchy
		, HideInInspector
		, Pad1
		, DontSave
		, Pad2, Pad3, Pad4
		, NotEditable
		, Pad5, Pad6, Pad7, Pad8
		, HideAndDontSave
	}

	public static class GameObject {

	}

	public static class MonoBehaviour {

	}
}
