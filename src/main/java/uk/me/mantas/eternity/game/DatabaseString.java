package uk.me.mantas.eternity.game;

import com.google.common.primitives.UnsignedInteger;

public class DatabaseString {
	public UnsignedInteger PackageSerialized;
	public int StringIDSerialized;
	public StringTableType StringTableSerialized;

	public enum StringTableType {
		Abilities(6)
		, Afflictions(0x13)
		, AreaNotifications(9)
		, BackerContent(20)
		, Backstory(0x3ae)
		, Characters(4)
		, Cyclopedia(900)
		, Debug(12)
		, Factions(14)
		, Gui(1)
		, Interactables(10)
		, ItemMods(0x10)
		, Items(5)
		, LoadingTips(15)
		, Maps(0x11)
		, Recipes(13)
		, Stronghold(0x3aa)
		, Tutorial(7)
		, Unassigned(0);

		public int n;
		StringTableType (int n) {
			this.n = n;
		}
	}
}
