package uk.me.mantas.eternity.game;

public class StatusEffectTrigger {
	public enum TriggerType {
		None
		, OnMiss
		, OnStaminaPercentBelow
		, Timer
		, OnTargetOfWillAttack
		, OnMove
		, OnDamage
		, OnKill
		, WhileStaminaPercentBelow
		, WhileStaminaPercentAbove
		, OnWeaponChange
		, OnWeaponChangeToNonSummoned
	}
}
