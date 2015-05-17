package uk.me.mantas.eternity.game;

public class ItemMod {
	public enum EnchantCategory {
		None
		, Quality
		, Lashing
		, Slaying
		, Proofing
		, Attributes
		, Unique
	}

	public enum TargetMode {
		Self
		, Enemy
	}

	public enum TriggerMode {
		OnUI
		, OnScoringCriticalHit
		, OnBeingCriticallyHit
		, OnUnconscious
		, AsSecondaryAttack
		, OnSpiritShift
	}
}
