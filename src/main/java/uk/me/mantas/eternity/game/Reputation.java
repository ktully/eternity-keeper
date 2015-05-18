package uk.me.mantas.eternity.game;

public class Reputation {
	public FactionDatabaseString SerializedName;
	public TitleStringSet TitleStrings;
	public String EditorName = "";
	public FactionName FactionID;
	public FactionDatabaseString Name;
	public int Negative;
	public int Positive;
	public int Scale = 50;

	public enum Axis {
		Positive
		, Negative
	}

	public enum ChangeStrength {
		None
		, VeryMinor
		, Minor
		, Pad1
		, Average
		, Pad2
		, Major
		, Pad3
		, VeryMajor
	}

	public enum RankType {
		Default
		, Good
		, Bad
		, Mixed
	}
}
