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

import uk.me.mantas.eternity.game.UnityEngine.Texture2D;

public class Item {
	public UIInventoryFilter.ItemFilterType FilterType;
	public Item.UIDragDropSoundType InventorySoundType;
	public int MaxStackSize;
	public CurrencyValue Value;
	public DatabaseString DisplayName;
	public DatabaseString DescriptionText;
	public Item.UIEquipSoundType InventoryEquipSound;
	public boolean IsQuestItem;
	public boolean IsIngredient;
	public boolean FullValueSell;
	public boolean NeverDropAsLoot;
	public boolean Unique;
	public Texture2D IconTexture;
	public Texture2D IconLargeTexture;
	public Container DroppedItemContainer;
	public boolean IsRedirectingIngredient;
	public String Name;
	public boolean Renders;
	public Item Prefab;
	public boolean IsPrefab;
	public BaseInventory StoredInventory;
	public Item.ItemLocation Location;

	public enum UIDragDropSoundType	{
		Armor_Plate,
		Armor_Chain,
		Armor_Leather,
		Armor_Cloth,
		Consumable_Potion,
		Consumable_Food,
		Consumable_Drugs,
		Loot_Scroll,
		Loot_Book,
		Loot_Generic_Jewelry,
		Loot_Keys,
		Loot_Gems,
		Loot_CreatureParts,
		Loot_Generic,
		Special_CampingSupplies,
		Special_LockPick,
		Special_GrapplingHookRope,
		Special_Torches,
		Weapon_SwordSmall,
		Weapon_SwordLarge,
		Weapon_StaffClub,
		Weapon_Wand,
		Weapon_Bow,
		Weapon_Crossbow,
		Weapon_Guns,
		Weapon_Mace,
		Weapon_Flail,
		Weapon_Dagger,
		Weapon_Shield,
		Loot_Ring,
		Loot_Necklace,
		Loot_Figurine,
		Consumable_Drink,
		Abydon_Hammer,
	}

	public enum UIEquipSoundType {
		Default,
		Armor_Plate,
		Armor_Chain,
		Armor_Leather,
		Armor_Cloth,
		Weapon_SwordSmall,
		Weapon_SwordLarge,
		Weapon_StaffClub,
		Weapon_Wand,
		Weapon_Bow,
		Weapon_Crossbow,
		Weapon_Guns,
		Weapon_Mace,
		Weapon_Flail,
		Jewelry_Necklace,
		Jewelry_Rings,
		Misc_Grimoire,
		Misc_Pet,
		Weapon_Dagger,
		Weapon_Shield,
		Abydon_Hammer,
	}

	public enum ItemLocation {
		Stored
		, Equipped
		, Prefab
		, Dragged
	}

	public static class UIEquippedItem {
		public Item item;
	}

	public static class StoreItem {}
}
