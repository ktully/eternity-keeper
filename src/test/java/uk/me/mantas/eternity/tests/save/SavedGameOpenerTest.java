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


package uk.me.mantas.eternity.tests.save;

import org.cef.callback.CefQueryCallback;
import org.json.JSONObject;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.save.SavedGameOpener;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SavedGameOpenerTest extends TestHarness {
	private static final String EXTRACTED =
		"[{\"isCompanion\":true"
		+ ",\"stats\":{"
			+ "\"StaminaRechargeMult\":1,\"LoreBonus\":0,\"FinishingBlowDamageMult\":1"
			+ ",\"HostileAOEDamageMultiplier\":1,\"EngagementDistanceBonus\":0"
			+ ",\"ConstitutionBonus\":0,\"FortitudeBonus\":0,\"KnockdownDefenseBonus\":0"
			+ ",\"RangedAttackSpeedMultiplier\":1,\"AthleticsSkill\":0,\"ImmuneToEngagement\":false"
			+ ",\"BaseMight\":16,\"TrapDamageOrDurationMult\":1,\"WhileKnockeddownDefenseBonus\":0"
			+ ",\"VesselAccuracyBonus\":0,\"StunDurationMult\":1"
			+ ",\"NearestAllyWithSharedTargetAccuracyBonus\":0,\"SurvivalSkill\":0"
			+ ",\"WilderAccuracyBonus\":0,\"EnemyCritToHitPercent\":0"
			+ ",\"OverrideSurvivalSkillLevel\":0,\"AoERadiusMult\":1,\"BonusMeleeDamage\":0"
			+ ",\"NegMoveTickMult\":1,\"MeleeDTBypass\":0,\"PoisonDefenseBonus\":0"
			+ ",\"HostileEffectDurationMultiplier\":1,\"RangedAccuracyBonus\":30"
			+ ",\"DexterityBonus\":0,\"OverrideName\":\"Calisca\",\"KnockDownDurationMult\":1"
			+ ",\"MeleeAccuracyBonus\":30,\"AttackSpeedMultiplier\":1,\"Experience\":0"
			+ ",\"WeaponDamageMinMult\":1,\"m_LastLevelUpNotified\":0"
			+ ",\"MeleeAttackSpeedMultiplier\":1,\"MaxStamina\":42,\"m_detectTimer\":0.70819074"
			+ ",\"Level\":1,\"BonusRangedWeaponDamageMult\":1,\"StealthBonus\":0,\"DTBypass\":0"
			+ ",\"ArmorSpeedFactorAdj\":0,\"BaseResolve\":11,\"RangedMovingRecoveryReductionPct\":0"
			+ ",\"BonusMissToGrazePercent\":0,\"AthleticsBonus\":0,\"DeflectionBonus\":0"
			+ ",\"LoreSkill\":0,\"TwoHandedDeflectionBonus\":0"
			+ ",\"DistantEnemyWeaponAccuracyBonus\":0,\"DiseaseDefenseBonus\":0"
			+ ",\"MeleeDamageRangePctIncreaseToMin\":0,\"BaseFortitude\":20"
			+ ",\"EnemyHitToGrazePercent\":0,\"BonusMeleeDamageMult\":1,\"IntellectBonus\":0"
			+ ",\"HealthStaminaPerLevel\":14,\"WoundDelay\":0,\"ReflexBonus\":0,\"BaseWill\":20"
			+ ",\"ChanterPhraseRadiusMult\":1,\"EnemiesNeededToFlank\":2"
			+ ",\"BonusHitToCritPercent\":0,\"ImmuneToEngageStop\":false"
			+ ",\"m_redirectMeleeAttacks\":0,\"CraftingBonus\":0,\"BonusShieldDeflection\":0"
			+ ",\"PerceptionBonus\":0,\"BaseIntellect\":12,\"StaminaRechargeBonus\":0"
			+ ",\"AttackerToHitRollOverride\":-1,\"ExtraSimultaneousHitDefenseBonus\":0"
			+ ",\"PotionEffectiveness\":1,\"BaseDeflection\":25,\"UnconsciousnessDelayed\":0"
			+ ",\"BonusWeaponSets\":0,\"FortitudeWillGrazeToMissPercent\":0"
			+ ",\"DisengagementAccuracyBonus\":0,\"DeathPrevented\":0,\"BonusHealMult\":1"
			+ ",\"DisengagementDamageBonus\":0,\"StealthSkill\":0"
			+ ",\"BonusTwoHandedMeleeWeaponDamageMult\":1,\"DamageMinBonus\":0"
			+ ",\"BonusHitToCritPercentEnemyBelow10Percent\":0,\"PrimordialAccuracyBonus\":0"
			+ ",\"BonusGrazeToMissPercent\":0,\"RateOfFireMultiplier\":1"
			+ ",\"ClassHealthMultiplier\":5,\"CurrentGrimoireCooldown\":0,\"BonusDTFromArmor\":0"
			+ ",\"DisengagementDefenseBonus\":0,\"FocusGainMult\":1,\"DOTTickMult\":1"
			+ ",\"WillBonus\":0,\"WeaponSwitchCooldownBonus\":0,\"BaseConstitution\":14"
			+ ",\"ProneDurationMult\":1,\"BonusQuickSlots\":0,\"EvadeEverything\":false"
			+ ",\"FlankedAccuracyBonus\":0,\"BasePerception\":12,\"TrapAccuracyBonus\":0"
			+ ",\"BonusGrazeToHitPercent\":0,\"MechanicsBonus\":0,\"StalkersLinkDamageMult\":1"
			+ ",\"BaseDexterity\":10,\"DeflectReflexGrazeToMissPercent\":0,\"ResolveBonus\":0"
			+ ",\"BonusHitToGrazePercent\":0,\"CraftingSkill\":0,\"MaxHealth\":42"
			+ ",\"UnarmedAccuracyBonus\":0,\"SpellDefenseBonus\":0"
			+ ",\"RangedAttackDistanceMultiplier\":1,\"ImprovedFlanking\":0"
			+ ",\"WhileStunnedDefenseBonus\":0,\"BeastAccuracyBonus\":0,\"AOEDefenseBonus\":0"
			+ ",\"DistantEnemyBonus\":0,\"RangedDeflectionBonus\":0,\"ReloadSpeedMultiplier\":1"
			+ ",\"WildstrikeDamageMult\":1,\"ConcentrationBonus\":0"
			+ ",\"ShieldDeflectExtendToReflex\":0,\"StaminaBonus\":0,\"DiseaseTickMult\":1"
			+ ",\"GrimoireCooldownBonus\":0,\"SpellCastingDisabled\":0"
			+ ",\"EnemyReflexGrazeToMissPercent\":0,\"BonusGrazeToHitPercentMeleeOneHanded\":0"
			+ ",\"BonusMeleeWeaponDamageMult\":1,\"EngageableEnemyCount\":1"
			+ ",\"BonusRangedWeaponCloseEnemyDamageMult\":1,\"StunDefenseBonus\":0"
			+ ",\"InterruptMultiplier\":1,\"ZealousAuraRadiusMult\":1,\"VeilDeflectionBonus\":0"
			+ ",\"BonusUnarmedDamage\":0,\"PushDefenseBonus\":0,\"BonusCritToHitPercent\":0"
			+ ",\"RemainingSkillPoints\":0,\"MeleeAttackDistanceMultiplier\":1"
			+ ",\"m_fatigueAccrued\":3600,\"MechanicsSkill\":0,\"MaxStaminaMultiplier\":1"
			+ ",\"ExtraStraightBounces\":0,\"BonusArmorDTMultAtLowHealth\":1"
			+ ",\"CritHitDamageMultiplierBonus\":0,\"RangedDTBypass\":0"
			+ ",\"BonusUsesPerRestPastThree\":0,\"FrenzyDurationMult\":1,\"m_noiseTimer\":0"
			+ ",\"PoisonTickMult\":1,\"SurvivalBonus\":0,\"m_recoveryTimer\":0"
			+ ",\"DualWieldAttackSpeedMultiplier\":1,\"MightBonus\":0,\"BaseReflexes\":20"
			+ ",\"CritHitDamageMultiplierBonusEnemyBelow10Percent\":0"
		+ "},\"GUID\":\"b1a7e809-0000-0000-0000-000000000000\""
		+ ",\"name\":\"Calisca\""
		+ ",\"portrait\":\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVQI12P4DwABAQEAG7bu"
			+ "VgAAAABJRU5ErkJggg==\""
		+ ",\"isDead\":false"
		+ "},{\"isCompanion\":false,\"stats\":{"
			+ "\"StaminaRechargeMult\":1,\"LoreBonus\":0,\"FinishingBlowDamageMult\":1"
			+ ",\"HostileAOEDamageMultiplier\":1,\"EngagementDistanceBonus\":0"
			+ ",\"ConstitutionBonus\":0,\"FortitudeBonus\":0,\"KnockdownDefenseBonus\":0"
			+ ",\"RangedAttackSpeedMultiplier\":1,\"AthleticsSkill\":0,\"ImmuneToEngagement\":false"
			+ ",\"BaseMight\":16,\"TrapDamageOrDurationMult\":1,\"WhileKnockeddownDefenseBonus\":0"
			+ ",\"VesselAccuracyBonus\":0,\"StunDurationMult\":1"
			+ ",\"NearestAllyWithSharedTargetAccuracyBonus\":0,\"SurvivalSkill\":0"
			+ ",\"WilderAccuracyBonus\":0,\"EnemyCritToHitPercent\":0"
			+ ",\"OverrideSurvivalSkillLevel\":0,\"AoERadiusMult\":1,\"BonusMeleeDamage\":0"
			+ ",\"NegMoveTickMult\":1,\"MeleeDTBypass\":0,\"PoisonDefenseBonus\":0"
			+ ",\"HostileEffectDurationMultiplier\":1,\"RangedAccuracyBonus\":30"
			+ ",\"DexterityBonus\":0,\"OverrideName\":\"Elenor\",\"KnockDownDurationMult\":1"
			+ ",\"MeleeAccuracyBonus\":30,\"AttackSpeedMultiplier\":1,\"Experience\":0"
			+ ",\"WeaponDamageMinMult\":1,\"m_LastLevelUpNotified\":0"
			+ ",\"MeleeAttackSpeedMultiplier\":1,\"MaxStamina\":42,\"m_detectTimer\":0.41699225"
			+ ",\"Level\":1,\"BonusRangedWeaponDamageMult\":1,\"StealthBonus\":0,\"DTBypass\":0"
			+ ",\"ArmorSpeedFactorAdj\":0,\"BaseResolve\":10,\"RangedMovingRecoveryReductionPct\":0"
			+ ",\"BonusMissToGrazePercent\":0,\"AthleticsBonus\":0,\"DeflectionBonus\":0"
			+ ",\"LoreSkill\":0,\"TwoHandedDeflectionBonus\":0"
			+ ",\"DistantEnemyWeaponAccuracyBonus\":0,\"DiseaseDefenseBonus\":0"
			+ ",\"MeleeDamageRangePctIncreaseToMin\":0,\"BaseFortitude\":20"
			+ ",\"EnemyHitToGrazePercent\":0,\"BonusMeleeDamageMult\":1,\"IntellectBonus\":0"
			+ ",\"HealthStaminaPerLevel\":14,\"WoundDelay\":0,\"ReflexBonus\":0,\"BaseWill\":20"
			+ ",\"ChanterPhraseRadiusMult\":1,\"EnemiesNeededToFlank\":2"
			+ ",\"BonusHitToCritPercent\":0,\"ImmuneToEngageStop\":false"
			+ ",\"m_redirectMeleeAttacks\":0,\"CraftingBonus\":0,\"BonusShieldDeflection\":0"
			+ ",\"PerceptionBonus\":0,\"BaseIntellect\":10,\"StaminaRechargeBonus\":0"
			+ ",\"AttackerToHitRollOverride\":-1,\"ExtraSimultaneousHitDefenseBonus\":0"
			+ ",\"PotionEffectiveness\":1,\"BaseDeflection\":25,\"UnconsciousnessDelayed\":0"
			+ ",\"BonusWeaponSets\":0,\"FortitudeWillGrazeToMissPercent\":0"
			+ ",\"DisengagementAccuracyBonus\":0,\"DeathPrevented\":0,\"BonusHealMult\":1"
			+ ",\"DisengagementDamageBonus\":0,\"StealthSkill\":0"
			+ ",\"BonusTwoHandedMeleeWeaponDamageMult\":1,\"DamageMinBonus\":0"
			+ ",\"BonusHitToCritPercentEnemyBelow10Percent\":0,\"PrimordialAccuracyBonus\":0"
			+ ",\"BonusGrazeToMissPercent\":0,\"RateOfFireMultiplier\":1"
			+ ",\"ClassHealthMultiplier\":5,\"CurrentGrimoireCooldown\":0,\"BonusDTFromArmor\":0"
			+ ",\"DisengagementDefenseBonus\":0,\"FocusGainMult\":1,\"DOTTickMult\":1"
			+ ",\"WillBonus\":0,\"WeaponSwitchCooldownBonus\":0,\"BaseConstitution\":12"
			+ ",\"ProneDurationMult\":1,\"BonusQuickSlots\":0,\"EvadeEverything\":false"
			+ ",\"FlankedAccuracyBonus\":0,\"BasePerception\":14,\"TrapAccuracyBonus\":0"
			+ ",\"BonusGrazeToHitPercent\":0,\"MechanicsBonus\":0,\"StalkersLinkDamageMult\":1"
			+ ",\"BaseDexterity\":13,\"DeflectReflexGrazeToMissPercent\":0,\"ResolveBonus\":0"
			+ ",\"BonusHitToGrazePercent\":0,\"CraftingSkill\":5,\"MaxHealth\":42"
			+ ",\"UnarmedAccuracyBonus\":0,\"SpellDefenseBonus\":0"
			+ ",\"RangedAttackDistanceMultiplier\":1,\"ImprovedFlanking\":0"
			+ ",\"WhileStunnedDefenseBonus\":0,\"BeastAccuracyBonus\":0,\"AOEDefenseBonus\":0"
			+ ",\"DistantEnemyBonus\":0,\"RangedDeflectionBonus\":0,\"ReloadSpeedMultiplier\":1"
			+ ",\"WildstrikeDamageMult\":1,\"ConcentrationBonus\":0"
			+ ",\"ShieldDeflectExtendToReflex\":0,\"StaminaBonus\":0,\"DiseaseTickMult\":1"
			+ ",\"GrimoireCooldownBonus\":0,\"SpellCastingDisabled\":0"
			+ ",\"EnemyReflexGrazeToMissPercent\":0,\"BonusGrazeToHitPercentMeleeOneHanded\":0"
			+ ",\"BonusMeleeWeaponDamageMult\":1,\"EngageableEnemyCount\":1"
			+ ",\"BonusRangedWeaponCloseEnemyDamageMult\":1,\"StunDefenseBonus\":0"
			+ ",\"InterruptMultiplier\":1,\"ZealousAuraRadiusMult\":1,\"VeilDeflectionBonus\":0"
			+ ",\"BonusUnarmedDamage\":0,\"PushDefenseBonus\":0,\"BonusCritToHitPercent\":0"
			+ ",\"RemainingSkillPoints\":0,\"MeleeAttackDistanceMultiplier\":1"
			+ ",\"m_fatigueAccrued\":0,\"MechanicsSkill\":0,\"MaxStaminaMultiplier\":1"
			+ ",\"ExtraStraightBounces\":0,\"BonusArmorDTMultAtLowHealth\":1"
			+ ",\"CritHitDamageMultiplierBonus\":0,\"RangedDTBypass\":0"
			+ ",\"BonusUsesPerRestPastThree\":0,\"FrenzyDurationMult\":1,\"m_noiseTimer\":0"
			+ ",\"PoisonTickMult\":1,\"SurvivalBonus\":0,\"m_recoveryTimer\":-0.004099274"
			+ ",\"DualWieldAttackSpeedMultiplier\":1,\"MightBonus\":0,\"BaseReflexes\":20"
			+ ",\"CritHitDamageMultiplierBonusEnemyBelow10Percent\":0"
		+ "},\"GUID\":\"09517a0d-4fec-407c-a749-a531f3be64e0\""
		+ ",\"name\":\"Elenor\""
		+ ",\"portrait\":\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVQI12P4DwABAQEAG7bu"
			+ "VgAAAABJRU5ErkJggg==\""
		+ ",\"isDead\":false}]";

	@Test
	public void mobileObjectsFileNotExists () {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		SavedGameOpener cls = new SavedGameOpener("404", mockCallback);
		cls.run();

		verify(mockCallback).success("{\"error\":\"NOT_EXISTS\"}");
	}

	@Test
	public void deserializationError () throws IOException {
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		Optional<File> saveDir = EKUtils.createTempDir(PREFIX);
		assertTrue(saveDir.isPresent());

		Files.createFile(
			new File(saveDir.get(), "MobileObjects.save").toPath());

		SavedGameOpener cls =
			new SavedGameOpener(saveDir.get().getAbsolutePath(), mockCallback);

		cls.run();
		verify(mockCallback).success("{\"error\":\"DESERIALIZATION_ERR\"}");
	}

	@Test
	public void saveGameOpened ()
		throws URISyntaxException
		, NoSuchFieldException
		, IllegalAccessException {

		Settings mockSettings = mockSettings();
		JSONObject mockJSON = mock(JSONObject.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		File resources = new File(getClass().getResource("/").toURI());
		SavedGameOpener cls =
			new SavedGameOpener(resources.getAbsolutePath(), mockCallback);

		mockSettings.json = mockJSON;
		when(mockJSON.getString("gameLocation"))
			.thenReturn(
				new File(resources, "SavedGameOpenerTest").getAbsolutePath());

		cls.run();
		verify(mockCallback).success(EXTRACTED);
	}
}
