package uk.me.mantas.eternity.game;

import java.util.UUID;

public class StrongholdEvent {
	public float Time;
	public Type EventType;
	public int RandomValue1;
	public int RandomValue2;
	public StrongholdUpgrade.Type EventUpgradeTypeSerialized;
	public StrongholdVisitorSerializeData EventVisitorNameSerialized;
	public String EventAttackNameSerialized;
	public UUID SerializedCompanion;
	public UUID SerializedAbandonedCompanion;

	public enum Type {
		CollectTaxes
		, SpawnAdventure
		, BuildUpgrade
		, SpawnIngredients
		, PayHirelings
		, RandomEvent
		, Kidnapped
		, Escorting
		, SupplicantEffectsWearOff
		, Attack
		, VisitorKilled
		, Count
	}
}
