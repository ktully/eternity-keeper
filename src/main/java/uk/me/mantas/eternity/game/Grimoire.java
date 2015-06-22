package uk.me.mantas.eternity.game;

import uk.me.mantas.eternity.serializer.CSharpCollection;

public class Grimoire {
	public SpellChapter[] Spells = new SpellChapter[6];
	public String PrimaryOwnerName;
	public SpellChapter[] SerializedSpells;
	public CSharpCollection SerializedSpellNames;

	public static class SpellChapter {
		public GenericSpell[] SpellData = new GenericSpell[4];
		public CSharpCollection SerializedData;
	}
}
