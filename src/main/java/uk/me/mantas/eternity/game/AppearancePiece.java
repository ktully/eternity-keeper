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
