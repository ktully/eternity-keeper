/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 Kim Mantas
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
