package uk.me.mantas.eternity.game;

import java.util.UUID;

public class StrongholdAdventure {
	public Type AdventureType;
	public int Duration;
	public int NumRewards;
	public Reward[] RewardList;
	public Type SerializedAdventureType;
	public float SerializedOfferExpires;
	public int SerializedDuration;
	public int SerializedNumRewards;
	public Reward[] SerializedRewardList;
	public UUID SerializedAdventurer;
	public int DeferredXP;

	public static class Reward {
		public RewardType m_type = StrongholdAdventure.RewardType.AverageItem;
		public int m_minValue;
		public int m_maxValue;
		public RewardType RewardType;
		public int MinValue;
		public int MaxValue;
	}

	public enum Type {
		None
		, Minor
		, Average
		, Major
		, Grand
		, Legendary
		, Count
	}

	public enum RewardType {
		XP
		, Copper
		, MinorItem
		, AverageItem
		, MajorItem
		, GrandItem
		, MinorRepBonus
		, AverageRepBonus
		, MajorRepBonus
		, GrandRepBonus
	}
}
