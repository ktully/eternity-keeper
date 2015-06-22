package uk.me.mantas.eternity.game;

import uk.me.mantas.eternity.game.UnityEngine.GameObject;

import java.util.UUID;

public class StatusEffectParams {
	public boolean Persistent;
	public boolean IsHostile;
	public int MaxRestCycles;
	public AttackBase.EffectAttachType VisualEffectAttach;
	public StatusEffectTrigger TriggerAdjustment;
	public String Tag;
	public PrerequisiteData[] ApplicationPrerequisites;
	public StatusEffect.ApplyType Apply;
	public StatusEffect.ModifiedStat AffectsStat;
	public DamagePacket.DamageType DmgType;
	public float Value;
	public float ExtraValue;
	public float Duration;
	public boolean LastsUntilCombatEnds;
	public boolean LastsUntilRest;
	public StatusEffectParams.IntervalRateType IntervalRate;
	public boolean OneHitUse;
	public boolean ChecksReligion;
	public GameObject OnStartVisualEffect;
	public GameObject OnAppliedVisualEffect;
	public GameObject OnStopVisualEffect;
	public GameObject OnAuraVisualEffect;
	public GameObject OnTriggerVisualEffect;
	public Trap TrapPrefab;
	public Equippable EquippablePrefab;
	public AttackBase AttackPrefab;
	public CharacterStats.Race RaceType;
	public String Keyword;
	public boolean DontHideFromLog;
	public boolean IgnoreAbilityDeactivation;
	public boolean m_deserializeInitialized;
	public GenericAbility OnDamagedCallbackAbility;
	public boolean IsOverTime;
	public UUID TrapPrefabSerialized;
	public Trap TrapSerialized2;
	public UUID EquippablePrefabSerialized;
	public Equippable EquippableSerialized2;
	public UUID AttackPrefabSerialized;
	public AttackBase AttackPrefabSerialized2;
	// TODO: Check this.
	//public Affliction AfflictionPrefabSerialized;
	public String AfflictionPrefabSerialized;
	public UUID OnDamageCallbackAbilitySerialized;
	public String Description;
	public boolean IsCleanedUp;
	public float MergedValue;

	public enum IntervalRateType {
		None
		, Damage
		, Hazard
		, Footstep
	}
}
