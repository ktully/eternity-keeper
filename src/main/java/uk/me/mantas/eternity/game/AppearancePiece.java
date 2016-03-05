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

public class AppearancePiece {
	public ArmorType armorType;
	public BodyPiece bodyPiece;
	public static String CHARACTER_PATH = "Art/Character/";
	public static char EXTRA_MESH = 'E';
	public static char HIDE_MESH = 'H';
	public boolean hideBoots;
	public boolean hideFacialHair;
	public boolean hideGloves;
	public boolean hideHair;
	public boolean hideHead;
	public int materialVariation = 1;
	public int modelVariation = 1;
	public static char PARTIAL_MESH = 'P';
	public boolean partialFacialHair;
	public boolean partialHair;
	public static char SHOW_MESH = 'S';
	public String specialOverride;

	public enum ArmorType {
		None
		, Cloth
		, Padded
		, Hide
		, Leather
		, Scale
		, Mail
		, Brigandine
		, Plate
		, BreastPlate
	}

	public enum BodyPiece {
		None
		, Body
		, Boots
		, Cape
		, Gloves
		, Helm
		, Hair
		, Head
		, FacialHair
	}
}
