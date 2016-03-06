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

public class AchievementTracker {
	public enum TrackedAchievementStat {
		CompletedGame
		, CompletedAct1
		, CompletedAct2
		, CompletedAct3
		, NumPrimaryCompanionsGained
		, NumAdventuresCreated
		, NumPartyMemberKnockouts
		, ExpertModeOn
		, TrialOfIronOn
		, PathOfTheDamnedOn
		, NumUniqueEnchantmentsCreated
		, NumUniqueFoodItemsCreated
		, NumUniqueScrollsCreated
		, NumUniquePotionsCreated
		, NumTrapItemsUsed
		, NumRestsUsed
		, NumEnemiesKilled
		, NumStrongholdUpgrades
		, NumDragonsKilled
		, NumUniqueMapsVisited
		, NumDispositionsAtLevel
		, NumGodsAppeased
		, BackedGame
		, NumLevelsOfOdNua
		, NumUniquePX1MapsVisited
		, NumPX1BountysCompleted
		, NumPX1PrimaryCompanionsGained
		, NumPX1DragonsKilled
		, NumSoulboundWeaponsFullyUnlocked
		, PX1DoorOfDurgansBatteryOpened
		, PX1RestartedWhiteForge
		, PX1CompletedSiegeofCragholdt
		, NumUniqueStrongholdAdventureTypesCompleted
		, DefendedPositionAsStrongholdMaster
		, NumPX2PrimaryCompanionsGained
		, NumWeaponOrShieldsLegendaryEnchanted
		, PX2DefeatedMenaceOfMowrghekIen
		, PX2ReachedReliquaryInAbbey
		, PX2StoppedThreatSeenInDreams
		, Count
	}
}
