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

import static uk.me.mantas.eternity.game.UnityEngine.Texture2D;

public class Phrase {
	public DatabaseString DisplayName;
	public DatabaseString Description;
	public CharacterStats.DefenseType DefendedBy;
	public AttackBase.EffectAttachType ChanterVisualEffectAttach;
	public Texture2D Icon;
	public String Tag;
	public int Level;
	public int VocalizationNumber;
	public Phrase.PhraseData[] m_phraseData;
	public StatusEffectParams[] StatusEffects;
	public Affliction[] Affliction;
	public GameObject OnChanterVisualEffect;
	public CharacterStats PhraseOwner;
	public float Recitation;
	public float BaseRecitation;
	public float Linger;
	public float BaseLinger;
	public float Duration;

	public static class PhraseData {
		public boolean IsHostile;
		public AttackBase.EffectAttachType VisualEffectAttach;
		public StatusEffect.ApplyType Apply;
		public StatusEffect.ModifiedStat AffectsStat;
		public DamagePacket.DamageType DmgType;
		public float Value;
		public float ExtraValue;
		public StatusEffectParams.IntervalRateType IntervalRate;
		public GameObject OnStartVisualEffect;
		public GameObject OnAppliedVisualEffect;
		public GameObject OnStopVisualEffect;
		public Texture2D Icon;
		public Trap TrapPrefab;
		public Affliction AfflictionPrefab;
	}

	public static class EffectData {
		public GameObject m_target;
		public UnsignedInteger m_effectID;
		public float m_durationMult;
	}

	public static class HitData {
		public GameObject m_target;
		public HitType m_hitResult;
	}
}
