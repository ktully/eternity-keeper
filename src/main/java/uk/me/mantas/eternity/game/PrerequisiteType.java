package uk.me.mantas.eternity.game;

public enum PrerequisiteType {
	StatusEffectCount
	, SummonsCount
	, EnemiesInAttackRange
	, AlliesInFriendlyRadius
	, MainTargetOnly
	, ExcludeMainTarget
	, NoAlliesInFriendlyRadius
	, ClosestAllyWithSameTarget
	, CasterIsChanting
	, CasterPhraseCount
	, Friendly
	, Hostile
	, StaminaPercentBelow
	, AlwaysFalse
	, UsingRangedWeapon
	, Vessel
	, UsingMeleeWeapon
	, StaminaAmountAtLeast
	, StaminaAmountBelow
	, FocusBelowMax
	, IsRace
	, HasGrimoire
	, StaminaPercentAbove
	, EquipmentUnlocked
	, IsDragonOrDrake
	, Count
}
