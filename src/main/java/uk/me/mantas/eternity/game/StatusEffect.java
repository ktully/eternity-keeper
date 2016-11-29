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

import com.google.common.primitives.UnsignedInteger;
import uk.me.mantas.eternity.game.UnityEngine.GameObject;
import uk.me.mantas.eternity.game.UnityEngine.MonoBehaviour;
import uk.me.mantas.eternity.serializer.CSharpCollection;

import java.util.Map;
import java.util.UUID;

public class StatusEffect {
	public boolean m_is_checking_melee_path;
	public CSharpCollection ParamPrereqSerialized;
	public GameObject Source;
	public UUID SourceSerialized;
	public UnsignedInteger m_id;
	public int m_stackingKey;
	public Equippable.EquipmentSlot m_slot;
	public boolean m_applied;
	public float m_duration;
	public float m_durationOverride;
	public boolean m_needsDurationCalculated;
	public float m_timeActive;
	public boolean RemovingEffect;
	public float m_intervalTimer;
	public UnsignedInteger m_intervalCount;
	public GameObject Target;
	public UUID TargetSerialized;
	public boolean m_deleteOnClear;
	public boolean m_effect_is_on_main_target;
	public UnsignedInteger m_generalCounter;
	public int BundleId;
	public Map<GameObject, StatusEffect> UiAuraEffectsApplied;
	public float m_scale;
	public float m_durationScale;
	public float m_religiousScale;
	public UnsignedInteger m_suspensionCount;
	public boolean m_suppressed;
	public float m_timeApplied;
	public int m_numRestCycles;
	public CSharpCollection Spells;
	public CSharpCollection AbilitiesGrantedToTarget;
	public CSharpCollection Traps;
	public Team CachedTeam;
	public UUID ExtraObjectSerialized;
	public GenericAbility.AbilityType AbilityType;
	public UUID AbilityOriginSerialized;
	// TODO: Check this.
	//public Equippable EquipmentOrigin;
	public String EquipmentOrigin;
	public UUID EquipmentOriginSerialized;
	public Phrase PhraseOrigin;
	public UUID PhraseOriginSerialized;
	// TODO: Check this.
	//public Affliction AfflictionOrigin;
	public String AfflictionOrigin;
	public UUID AfflictionOriginSerialized;
	public String AfflictionKeyword;
	public MonoBehaviour Origin;
	public float m_damageToReapply;
	public float m_damageToAbsorb;
	public int m_triggerCount;
	public float DurationAfterBreak;
	public boolean TicksAfterBreak;
	public float IncreasePerTick;
	public float Interval;
	public float FriendlyRadius;
	public boolean IsAura;
	public boolean IsFromAura;
	public boolean m_forceStackable;
	public UUID OwnerSerialized;
	public boolean Stackable;
	public boolean Exclusive;
	public boolean IsSuspended;
	public boolean IsSuppressed;
	public boolean Applied;
	public boolean HasBeenApplied;
	public boolean Expired;
	public float TimeActive;
	public float TimeApplied;
	public Equippable.EquipmentSlot Slot;
	public StatusEffectParams Params;
	public float CurrentAppliedValue;
	public float CurrentAppliedValueForUi;
	public boolean HasTriggerActivation;
	public boolean IsTriggerActivated;
	public boolean AppliedTriggered;
	public float Duration;
	public float UnadjustedDurationAdd;
	public float TemporaryDurationAdjustment;
	public boolean LastsUntilCombatEnds;
	public boolean LastsUntilRest;
	public float TimeLeft;
	public float Scale;
	public String HitStrength;
	public NonstackingType NonstackingEffectType;
	public boolean IsDamageDealing;
	public boolean IsOverTime;
	public boolean IsDOT;
	public boolean IsScaledMultiplier;
	public boolean IgnoresValue;
	public boolean IsNegativeMovementEffect;
	public boolean IsRecoveryTimePausingEffect;
	public boolean IsPoisonEffect;
	public boolean IsDiseaseEffect;
	public String[] AppliedFXNames;
	public CSharpCollection ForcedAuraTargets;
	public boolean DeleteOnClear;
	public UnsignedInteger EffectID;
	public String BundleName;
	public StatusEffectTrigger.TriggerType TriggerType;
	public float TriggerValue;
	public float TriggerValueAdjustment;
	public float TriggerDurationAdjustment;
	public float TriggerRadiusAdjustment;
	public int TriggerMaxTriggerCount;
	public boolean TriggerRemoveEffectAtMax;
	public boolean TriggerResetTriggerOnEffectPulse;
	public boolean TriggerResetTriggerOnEffectEnd;

	public enum NonstackingType {
		ActiveBonus
		, ItemBonus
		, ActivePenalty
		, ItemPenalty
	}

	public enum ApplyType {
		ApplyOnTick
		, ApplyOverTime
		, ApplyAtEnd
	}

	public enum ModifiedStat
	{
		MaxHealth,
		MaxStamina,
		Health,
		Stamina,
		MeleeAccuracy,
		RangedAccuracy,
		Deflection,
		Fortitude,
		Reflex,
		Will,
		StaminaRechargeRate,
		AttackSpeed,
		Stealth,
		BonusDamage,
		DamageThreshhold,
		DamageMinimum,
		MovementRate,
		NonTargetable,
		NonMobile,
		KnockedDown,
		EngagedEnemyCount,
		EngagementRadius,
		EngagementAccuracy,
		EngagementDamage,
		Engageable,
		Damage,
		Stunned,
		BonusUnarmedDamage,
		MeleeAttackDistanceMult,
		RangedAttackDistanceMult,
		MeleeAttackAllOnPath,
		BonusMeleeDamageFromWounds,
		BonusDTFromArmor,
		MeleeMissThresholdDelta_DO_NOT_USE,
		MeleeDamageRangePctIncreaseToMin,
		CanStun,
		SneakAttackOnNearDead,
		MeleeCritThresholdDelta_DO_NOT_USE,
		RangedCritThresholdDelta_DO_NOT_USE,
		CanCripple,
		MarkedPrey,
		SuspendHostileEffects,
		BonusMeleeDamage,
		ImmuneToEngageStop,
		HealthLossPctMult_DO_NOT_USE,
		BonusDamageMult,
		FocusWhenHits,
		BeamDamageMult,
		DrainResolveForDeflection,
		ReapplyDamage,
		ReapplyDamageToNearbyEnemies,
		ReloadSpeed,
		DropTrap,
		StasisShield,
		SuspendBeneficialEffects,
		DamageBasedOnInverseHealth,
		Resolve,
		Might,
		Dexterity,
		Intellect,
		SummonWeapon,
		TransferStamina,
		StaminaRechargeRateMult,
		VesselAccuracy,
		BeastAccuracy,
		WilderAccuracy,
		StunDefense,
		KnockdownDefense,
		PoisonDefense,
		DiseaseDefense,
		DistantEnemyBonus,
		BonusDamageMultOnLowStaminaTarget,
		BonusCritChanceOnSameEnemy,
		BonusAccuracyForNearestAllyOnSameEnemy,
		EnemyCritToHitPercent,
		HostileEffectDurationMult,
		DeflectReflexGrazeToMissPercent,
		FortitudeWillGrazeToMissPercent,
		ExtraStraightBounces,
		DamageToDOT,
		BonusDamageMultIfTargetHasDOT,
		RedirectMeleeAttacks,
		HostileAOEDamageMultiplier,
		ImprovedFlanking,
		DTBypass,
		StealSpell,
		SwapFaction,
		AttackOnMeleeHit,
		MinorSpellReflection,
		Athletics,
		Lore,
		Mechanics,
		Survival,
		Crafting,
		PushDefense,
		WhileStunnedDefense,
		WhileKnockeddownDefense,
		BonusAccuracyOnSameEnemy,
		BonusDamageMultOnSameEnemy,
		Constitution,
		Perception,
		CritHitMultiplierBonus,
		BonusGrazeToHitPercent,
		CanStunOnCrit,
		BonusGrazeToMissPercent,
		BonusCritToHitPercent,
		BonusMissToGrazePercent,
		BonusHitToCritPercent,
		BonusHitToGrazePercent,
		BonusDamageProc,
		Confused,
		BonusMeleeWeaponDamageMult,
		BonusRangedWeaponDamageMult,
		RateOfFireMult,
		ApplyAttackEffects,
		EnemyReflexGrazeToMissPercent,
		StaminaPercent,
		EnemiesNeededToFlankAdj,
		ConcentrationBonus,
		DOTOnHit,
		SpellReflection,
		DisableSpellcasting,
		ResistAffliction,
		PreventDeath,
		AdjustDurationBeneficialEffects,
		DOTTickMult,
		AdjustDurationHostileEffects,
		ResistKeyword,
		TransferDT,
		TransferRandomAttribute,
		Disintegrate,
		BonusAccuracyOnSameEnemyAsExtraObject,
		Duplicate,
		GainStaminaWhenHits,
		CanKnockDownOnCrit,
		BonusAccuracyAtLowStamina,
		BonusDamageMultAtLowStamina,
		BonusDamageMultOnKDSFTarget,
		DamagePlusDOT,
		RangedGrazeReflection,
		EnemyHitToGrazePercent,
		StunDurationMult,
		KnockDownDurationMult,
		BonusArmorDTMultAtLowHealth,
		AccuracyByRace,
		DamageMultByRace,
		Fatigue,
		DUMMY_EFFECT_IncreasedWeaponReach,
		PrimordialAccuracy,
		StopAnimation,
		AddAfflictionImmunity,
		Invisible,
		WoundDelay,
		SpellDamageMult,
		FinishingBlowDamageMult,
		ZealousAuraAoEMult,
		DelayUnconsciousness,
		NegMoveTickMult,
		BonusDamageMultOnFlankedTarget,
		FocusGainMult,
		DisengagementDefense,
		SpellDefense,
		RangedDeflection,
		BonusUsesPerRestPastThree,
		PoisonTickMult,
		DiseaseTickMult,
		StalkersLinkDamageMult,
		DamageToStamina,
		ChanterPhraseAoEMult,
		BonusHealMult,
		IncomingCritDamageMult,
		SpellCastBonus,
		AoEMult,
		FrenzyDurationMult,
		ProneDurationMult,
		WildstrikeDamageMult,
		ReviveAndAddStamina,
		DamageToStaminaRegen,
		LaunchAttack,
		HidesHealthStamina,
		AllDefense,
		MaxStaminaMult,
		CallbackOnDamaged,
		ApplyFinishingBlowDamage,
		NoEffect,
		Accuracy,
		TransferDamageToStamina,
		CallbackAfterAttack,
		IncomingDamageMult,
		GivePlayerMoney,
		ArmorSpeedFactorAdj,
		DistantEnemyWeaponAccuracyBonus,
		BonusRangedWeaponCloseEnemyDamageMult,
		DualWieldAttackSpeedPercent,
		BonusShieldDeflection,
		ShieldDeflectionExtendToReflex,
		ApplyWounds,
		BonusDamageMultWithImplements,
		DamageAttackerOnImplementLaunch,
		RangedMovingRecoveryReductionPct,
		BonusGrazeToHitRatioMeleeOneHand,
		TwoHandedDeflectionBonus,
		BonusDamageByRacePercent,
		InterruptBonusPercent,
		BonusPotionEffectOrDurationPercent,
		ExtraSimultaneousHitDefenseBonus,
		BonusWeaponSets,
		BonusQuickSlots,
		MeleeAttackSpeedPercent,
		RangedAttackSpeedPercent,
		TrapAccuracy,
		BonusDamageByTypePercent,
		MeleeDTBypass,
		RangedDTBYpass,
		GrimoireCooldownBonus,
		WeaponSwitchCooldownBonus,
		ShortenAfflictionDuration,
		MaxFocus,
		TrapBonusDamageOrDurationPercent,
		BonusHitToCritPercentEnemyBelow10Percent,
		AllDefensesExceptDeflection,
		ApplyPulsedAOE,
		WeapMinDamageMult,
		BreakAllEngagement,
		VeilDeflection,
		BonusTwoHandedMeleeWeaponDamageMult,
		BonusMeleeDamageMult,
		BonusCritHitMultiplierEnemyBelow10Percent,
		UnarmedAccuracy,
		BonusDTFromWounds,
		TransferBeneficialTime,
		AccuracyWeaponByType,
		ExtraProjectilesByWeaponType,
		SummonConsumable,
		TransferDamageToCaster,
		TransferAttackSpeed,
		DamageToSummon,
		Destroy,
		LaunchAttackWithRollingBonus,
		ProhibitEnemyEngagementByLevel,
		DamageShield,
		HealthPercent,
		PostDtDamagePlusDot,
		RangedReflection,
		SingleWeaponSpeedFactorAdj,
		KeywordImmunity,
		CantUseFoodDrinkDrugs,
		AddDamageTypeImmunity,
		NegateNextRecovery,
		GenericMarker,
		DamageByKeywordCount,
		RemoveAllEffectsByKeyword,
		SummonSecondaryWeapon,
		GrantFocusToExtraObject,
		VerticalLaunch,
		EnemyGrazeToMissPercent,
		BonusHealingGivenMult,
		StaminaByAthletics,
		AttackOnHitWithMelee,
		AccuracyBonusForAttackersWithAffliction,
		RemoveAffliction,
		BonusArmorDtMult,
		GrantAbility,
		SetBaseAttribute,
		SetBaseDefense,
		MindwebEffect,
		PhraseRecitationLengthMult,
		DamageAlwaysMinimumAgainstCCD,
		DrugDurationMult,
		TransferStaminaReversed,
		TransferAttribute,
		RestoreSpiritshiftUses,
		ApplyAffliction,
		AccuracyByClass,
		DamageMultByClass,
		AfflictionShield,
		ConsumableDurationMult,
		DisableAbilityUse,
		ShortenAfflictionDurationOngoing,
		Push
	}
}
