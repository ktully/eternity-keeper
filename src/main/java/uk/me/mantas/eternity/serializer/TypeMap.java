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


package uk.me.mantas.eternity.serializer;

import com.google.common.primitives.UnsignedInteger;
import uk.me.mantas.eternity.game.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class TypeMap {

	// map Unity or C sharp object types to Java containers
	// note: primitives are handled separately
	public static final Map<String, Class> map = new HashMap<String, Class>() {{
		put("System.Boolean", boolean.class);
		put("System.Guid", UUID.class);
		put("System.Int32", int.class);
		put("System.Object", Object.class);
		put("System.Single", float.class);
		put("System.String", String.class);
		put("System.UInt32", UnsignedInteger.class);

		put("System.Boolean[]", Boolean[].class);
		put("System.Byte[]", Byte[].class);
		put("System.Int32[]", Integer[].class);
		put("System.Int32[][]", Integer[][].class);
		put("System.Single[]", Float[].class);

		put("System.Collections.Generic.Dictionary", HashMap.class);
		put("System.Collections.Generic.List", CSharpCollection.class);
		put("System.Collections.Generic.SortedList", SortedList.class);
		put("System.Collections.Hashtable", Hashtable.class);

		put("UnityEngine.Color", UnityEngine.Colour.class);
		put("UnityEngine.Vector2", UnityEngine.Vector2.class);
		put("UnityEngine.Vector3", UnityEngine.Vector3.class);
		put("UnityEngine.Vector3[]", UnityEngine.Vector3[].class);
		put("UnityEngine.Vector3[][]", UnityEngine.Vector3[][].class);

		put("AbilityMod", AbilityMod.class);
		put("Affliction", Affliction.class);
		put("AppearancePiece", AppearancePiece.class);
		put("CharacterDatabaseString", CharacterDatabaseString.class);
		put("ComponentPersistencePacket", ComponentPersistencePacket.class);
		put("Container", Container.class);
		put("CurrencyValue", CurrencyValue.class);
		put("Disposition", Disposition.class);
		put("EternityDateTime", EternityDateTime.class);
		put("EternityTimeInterval", EternityTimeInterval.class);
		put("ExperienceSerializerPacket", ExperienceSerializerPacket.class);
		put("GameDifficulty", GameDifficulty.class);
		put("GameState", GameState.class);
		put("Gender", Gender.class);
		put("GenericSpell", GenericSpell.class);
		put("GenericTalent", GenericTalent.class);
		put("InventoryItem", InventoryItem.class);
		put("Item", Item.class);
		put("ItemMod", ItemMod.class);
		put("KeyControl", KeyControl.class);
		put("ObjectPersistencePacket", ObjectPersistencePacket.class);
		put("PrerequisiteData", PrerequisiteData.class);
		put("StatusEffect", StatusEffect.class);
		put("Team", Team.class);

		put("AchievementTracker+TrackedAchievementStat"
			, AchievementTracker.TrackedAchievementStat.class);

		put("ActivationTimers+Timer", ActivationTimers.Timer.class);

		put("AIController+AISummonType", AIController.AISummonType.class);
		put("AIController+AggressionType", AIController.AggressionType.class);

		put("CharacterStats+Background", CharacterStats.Background.class);
		put("CharacterStats+Class", CharacterStats.Class.class);
		put("CharacterStats+Culture", CharacterStats.Culture.class);
		put("CharacterStats+FatigueLevel", CharacterStats.FatigueLevel.class);
		put("CharacterStats+NoiseLevelType", CharacterStats.NoiseLevelType.class);
		put("CharacterStats+Race", CharacterStats.Race.class);
		put("CharacterStats+Subrace", CharacterStats.Subrace.class);

		put("CompanionNames+Companions", CompanionNames.Companions.class);

		put("DifficultyScaling+Scaler", DifficultyScaling.Scaler.class);

		put("GenericAbility+AbilityType", GenericAbility.AbilityType.class);

		put("Grimoire+SpellChapter", Grimoire.SpellChapter.class);
		put("Grimoire+SpellChapter[]", Grimoire.SpellChapter[].class);

		put("MapData+VisibilityType", MapData.VisibilityType.class);
		put("MapData+VisibilityType[]", MapData.VisibilityType[].class);

		put("NotesPage", NotesPage.class);
		put("NotesPage+NoteEntry", NotesPage.NoteEntry.class);

		put("NPCAppearance+Sex", NPCAppearance.Sex.class);
		put("NPCAppearance+Race", NPCAppearance.Race.class);
		put("NPCAppearance+Subrace", NPCAppearance.Subrace.class);

		put("OCL+State", OCL.State.class);

		put("PartyMemberAI+DetectionObject", PartyMemberAI.DetectionObject.class);

		// TODO: This is a little weird.
		//put("Phrase[]", Phrase[].class);
		put("Phrase", Phrase.class);
		put("Phrase[]", String[].class);

		put("QuestManager+QuestTimestamps", QuestManager.QuestTimestamps.class);

		put("Religion+Deity", Religion.Deity.class);
		put("Religion+PaladinOrder", Religion.PaladinOrder.class);

		put("Reputation", Reputation.class);
		put("Reputation[]", Reputation[].class);

		put("Stealth+DetectionObject", Stealth.DetectionObject.class);

		put("StrongholdEvent", StrongholdEvent.class);
		put("StrongholdAdventure", StrongholdAdventure.class);
		put("StrongholdAdventure+Reward", StrongholdAdventure.Reward.class);
		put("StrongholdAdventureCompletion", StrongholdAdventureCompletion.class);
		put("StrongholdHireling", StrongholdHireling.class);
		put("StrongholdGuestHireling", StrongholdGuestHireling.class);
		put("StrongholdPrisonerData", StrongholdPrisonerData.class);
		put("StrongholdUpgrade+Type", StrongholdUpgrade.Type.class);
		put("StrongholdVisitorArrival", StrongholdVisitorArrival.class);
		put("StrongholdVisitorSerializeData", StrongholdVisitorSerializeData.class);
	}};

	// 	Convert to backwards compatible Unity type path
	//
	//	Windows Store and Xbox version of game use a newer version of Unity, whose saves don't work with Steam or GoG
	//  This fix allows transferring new format saves to Steam and GoG
	private static final String MODULE_PREFIX = ", ";
	public static final String NEW_TYPE_MODULE = "UnityEngine.CoreModule";
	private static final String OLD_TYPE_MODULE = "UnityEngine";

	private static final Pattern newPattern = Pattern.compile(MODULE_PREFIX + NEW_TYPE_MODULE);

	public static boolean usesNewUnityModule(String type) {
		return type.contains(NEW_TYPE_MODULE);
	}

	public static String getBackwardsCompatibleType(String type) {
		if (usesNewUnityModule(type)) {
			// TODO: use a cache for performance
			type = newPattern.matcher(type).replaceFirst(MODULE_PREFIX + OLD_TYPE_MODULE);
		}

		return type;
	}

	//	I've used a reqex to work with unknown types, but an equivalent map for the types I know of is as follows:
	//
	//public static final Map<String, String> backwardsCompatible = new HashMap<String, String>() {{
	//	put("UnityEngine.Color, UnityEngine.CoreModule", "UnityEngine.Color, UnityEngine");
	//	put("UnityEngine.Vector2, UnityEngine.CoreModule", "UnityEngine.Vector2, UnityEngine");
	//	put("UnityEngine.Vector3[][], UnityEngine.CoreModule", "UnityEngine.Vector3[][], UnityEngine");
	//	put("UnityEngine.Vector3[], UnityEngine.CoreModule", "UnityEngine.Vector3[], UnityEngine");
	//	put("UnityEngine.Vector3, UnityEngine.CoreModule", "UnityEngine.Vector3, UnityEngine");
	//}};
}
