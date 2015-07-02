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

import com.google.common.primitives.UnsignedInteger;
import org.cef.callback.CefQueryCallback;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Environment;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.Settings;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;
import uk.me.mantas.eternity.game.ComponentPersistencePacket;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.save.SavedGameOpener;
import uk.me.mantas.eternity.serializer.SharpSerializer;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.tests.ExposedClass;
import uk.me.mantas.eternity.tests.TestHarness;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;
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

	private static final String DESERIALIZATION_ERR = "{\"error\":\"DESERIALIZATION_ERR\"}";

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
	public void saveGameOpened () throws URISyntaxException {
		Settings mockSettings = mockSettings();
		JSONObject mockJSON = mock(JSONObject.class);
		CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		File resources = new File(getClass().getResource("/").toURI());
		SavedGameOpener cls =
			new SavedGameOpener(resources.getAbsolutePath(), mockCallback);

		mockSettings.json = mockJSON;
		when(mockJSON.getString("gameLocation")).thenReturn(
			new File(resources, "SavedGameOpenerTest").getAbsolutePath());

		cls.run();
		verify(mockCallback).success(EXTRACTED);
	}

	@Test
	public void deserializeTestDeserializationError () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializerFactory mockFactory = mock(SharpSerializerFactory.class);
		final SharpSerializer mockSerializer = mock(SharpSerializer.class);
		final File mockMobileObjectsFile = mock(File.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		when(mockEnvironment.sharpSerializer()).thenReturn(mockFactory);
		when(mockFactory.forFile(anyString())).thenReturn(mockSerializer);
		when(mockSerializer.deserialize()).thenReturn(Optional.empty());
		when(mockMobileObjectsFile.getAbsolutePath()).thenReturn("404");

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		exposedOpener.call("deserialize", mockMobileObjectsFile);

		verify(mockCallback).success(DESERIALIZATION_ERR);
	}

	@Test
	public void deserializeTestThrowsException () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializerFactory mockFactory = mock(SharpSerializerFactory.class);
		final File mockMobileObjectsFile = mock(File.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);

		when(mockEnvironment.sharpSerializer()).thenReturn(mockFactory);
		when(mockMobileObjectsFile.getAbsolutePath()).thenReturn("404");
		doThrow(new FileNotFoundException()).when(mockFactory).forFile(anyString());

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		exposedOpener.call("deserialize", mockMobileObjectsFile);

		verify(mockCallback).success(DESERIALIZATION_ERR);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deserializeTestFiltersFailedProperties () throws FileNotFoundException {
		final Environment mockEnvironment = mockEnvironment();
		final SharpSerializerFactory mockFactory = mock(SharpSerializerFactory.class);
		final SharpSerializer mockSerializer = mock(SharpSerializer.class);
		final File mockMobileObjectsFile = mock(File.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final Property mockObjectCount = mock(Property.class);
		final Property mockDeserializedProperty = mock(Property.class);

		mockObjectCount.obj = 2;
		when(mockEnvironment.sharpSerializer()).thenReturn(mockFactory);
		when(mockFactory.forFile(anyString())).thenReturn(mockSerializer);
		when(mockMobileObjectsFile.getAbsolutePath()).thenReturn("404");

		when(mockSerializer.deserialize())
			.thenReturn(Optional.of(mockObjectCount))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(mockDeserializedProperty));

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final List<Property> deserialzed =
			(List<Property>) exposedOpener.call("deserialize", mockMobileObjectsFile);

		assertEquals(1, deserialzed.size());
		assertSame(mockDeserializedProperty, deserialzed.get(0));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void extractCharactersTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final Property notObjPersistencePacket = mock(Property.class);
		final Property noObjectName = mock(Property.class);
		final Property noObjectID = mock(Property.class);
		final Property startsWithCompanion = mock(Property.class);
		final Property startsWithPlayer = mock(Property.class);

		final List<Property> gameObjects = new ArrayList<Property>(){{
			add(notObjPersistencePacket);
			add(noObjectName);
			add(noObjectID);
			add(startsWithCompanion);
			add(startsWithPlayer);
		}};

		notObjPersistencePacket.obj = new Object();

		final ObjectPersistencePacket noObjectNamePacket = new ObjectPersistencePacket();
		noObjectNamePacket.ObjectName = null;
		noObjectName.obj = noObjectNamePacket;

		final ObjectPersistencePacket noObjectIDPacket = new ObjectPersistencePacket();
		noObjectIDPacket.ObjectName = "";
		noObjectIDPacket.ObjectID = null;
		noObjectID.obj = noObjectIDPacket;

		final ObjectPersistencePacket startsWithCompanionPacket = new ObjectPersistencePacket();
		startsWithCompanionPacket.ObjectName = "Companion_A";
		startsWithCompanionPacket.ObjectID = "Key_A";
		startsWithCompanion.obj = startsWithCompanionPacket;

		final ObjectPersistencePacket startsWithPlayerPacket = new ObjectPersistencePacket();
		startsWithPlayerPacket.ObjectName = "Player_B";
		startsWithPlayerPacket.ObjectID = "Key_B";
		startsWithPlayer.obj = startsWithPlayerPacket;

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		final Map<Object, Class> argMap = new HashMap<Object, Class>(){{
			put(gameObjects, List.class);
		}};

		final Map<String, Property> characters =
			(Map<String, Property>) exposedOpener.call("extractCharacters", argMap);

		assertEquals(2, characters.size());
		assertSame(startsWithCompanion, characters.get("Key_A"));
		assertSame(startsWithPlayer, characters.get("Key_B"));
	}

	@Test
	public void extractPortraitTestSelectsFirst () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket firstComponent = new ComponentPersistencePacket();
		final ComponentPersistencePacket secondComponent = new ComponentPersistencePacket();

		firstComponent.TypeString = "Portrait";
		secondComponent.TypeString = "Portrait";
		firstComponent.Variables = new HashMap<>();
		secondComponent.Variables = new HashMap<String, Object>() {{
			put("m_textureLargePath", "png");
		}};

		packet.ComponentPackets = new ComponentPersistencePacket[]{
			firstComponent
			, secondComponent
		};

		mockSettings();

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = (String) exposedOpener.call("extractPortrait", packet, false);

		assertEquals("", result);
	}

	@Test
	public void extractPortraitTestNoGameLocation () {
		final Settings mockSettings = mockSettings();
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();

		component.TypeString = "Portrait";
		component.Variables = new HashMap<String, Object>(){{
			put("m_textureLargePath", "png");
		}};

		mockSettings.json = new JSONObject();
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = (String) exposedOpener.call("extractPortrait", packet, false);

		assertEquals("", result);
	}

	@Test
	public void extractPortraitTestPortraitNotFound () throws URISyntaxException {
		final Settings mockSettings = mockSettings();
		final Logger mockLogger = interceptLogging(SavedGameOpener.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();
		final File testResources = new File(getClass().getResource("/").toURI());

		component.TypeString = "Portrait";
		component.Variables = new HashMap<String, Object>(){{
			put("m_textureLargePath", "../404");
		}};

		mockSettings.json = new JSONObject().put("gameLocation", testResources.getAbsolutePath());
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = (String) exposedOpener.call("extractPortrait", packet, false);

		final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(mockLogger).error(anyString(), argument.capture());
		assertEquals("", result);
		assertEquals(new File(testResources, "404").getAbsolutePath(), argument.getValue());
	}

	@Test
	public void extractPortraitTestUnmappedCompanion () throws URISyntaxException {
		final Settings mockSettings = mockSettings();
		final Logger mockLogger = interceptLogging(SavedGameOpener.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();
		final File testResources = new File(getClass().getResource("/").toURI());
		final String companionPortraitPath = Environment.getInstance().getCompanionPortraitPath();
		final String substitutedPath = String.format(companionPortraitPath, "unmapped");
		final File portraitFile =
			Paths.get(testResources.toURI())
				.resolve(Environment.PILLARS_DATA_DIR)
				.resolve(substitutedPath)
				.toFile();

		mockSettings.json = new JSONObject().put("gameLocation", testResources.getAbsolutePath());
		packet.ObjectName = "Companion_Unmapped";
		component.TypeString = "Portrait";
		component.Variables = new HashMap<>();
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = (String) exposedOpener.call("extractPortrait", packet, true);

		final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(mockLogger).error(anyString(), argument.capture());
		assertEquals("", result);
		assertEquals(portraitFile.getAbsolutePath(), argument.getValue());
	}

	@Test
	public void extractPortraitTestMappedCompanion () throws URISyntaxException {
		final Settings mockSettings = mockSettings();
		final Logger mockLogger = interceptLogging(SavedGameOpener.class);
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();
		final File testResources = new File(getClass().getResource("/").toURI());
		final String companionPortraitPath = Environment.getInstance().getCompanionPortraitPath();
		final String substitutedPath = String.format(companionPortraitPath, "grieving_mother");
		final File portraitFile =
			Paths.get(testResources.toURI())
				.resolve(Environment.PILLARS_DATA_DIR)
				.resolve(substitutedPath)
				.toFile();

		mockSettings.json = new JSONObject().put("gameLocation", testResources.getAbsolutePath());
		packet.ObjectName = "Companion_GM";
		component.TypeString = "Portrait";
		component.Variables = new HashMap<>();
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		final String result = (String) exposedOpener.call("extractPortrait", packet, true);

		final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(mockLogger).error(anyString(), argument.capture());
		assertEquals("", result);
		assertEquals(portraitFile.getAbsolutePath(), argument.getValue());
	}

	@Test
	public void detectDeadTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket component = new ComponentPersistencePacket();

		component.TypeString = "Health";
		component.Variables = new HashMap<>();
		packet.ComponentPackets = new ComponentPersistencePacket[]{ component };

		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);
		boolean result = (boolean) exposedOpener.call("detectDead", packet);
		assertFalse(result);

		component.Variables = new HashMap<String, Object>(){{
			put("CurrentHealth", 1f);
		}};

		result = (boolean) exposedOpener.call("detectDead", packet);
		assertFalse(result);

		component.Variables = new HashMap<String, Object>(){{
			put("CurrentHealth", 0f);
		}};

		result = (boolean) exposedOpener.call("detectDead", packet);
		assertTrue(result);
	}

	@Test
	public void extractNameTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		packet.ObjectName = "NoUnderscore";
		String result = (String) exposedOpener.call("extractName", packet);
		assertEquals("", result);

		packet.ObjectName = "Player_HasUnderscore";
		result = (String) exposedOpener.call("extractName", packet);
		assertEquals("HasUnderscore", result);

		packet.ObjectName = "Player_HasBracket(Clone)_1";
		result = (String) exposedOpener.call("extractName", packet);
		assertEquals("HasBracket", result);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void extractCharacterStatsTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final ComponentPersistencePacket notStatsComponent = new ComponentPersistencePacket();
		final ComponentPersistencePacket statsComponent = new ComponentPersistencePacket();
		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		notStatsComponent.TypeString = "NotCharacterStats";
		statsComponent.TypeString = "CharacterStats";
		statsComponent.Variables = new HashMap<String, Object>(){{
			put("Integer", 1);
			put("Float", 1f);
			put("String", "1");
			put("UnsignedInteger", UnsignedInteger.valueOf(1L));
		}};

		packet.ComponentPackets = new ComponentPersistencePacket[]{ notStatsComponent };

		Optional<Map<String, Object>> result =
			(Optional<Map<String, Object>>) exposedOpener.call("extractCharacterStats", packet);

		assertFalse(result.isPresent());

		packet.ComponentPackets = new ComponentPersistencePacket[]{
			notStatsComponent
			, null
			, statsComponent
		};

		result =
			(Optional<Map<String, Object>>) exposedOpener.call("extractCharacterStats", packet);

		assertTrue(result.isPresent());
		assertEquals(1, result.get().get("Integer"));
		assertEquals(1f, (float) result.get().get("Float"), 1e-6);
		assertEquals("1", result.get().get("String"));
		assertNull(result.get().get("UnsignedInteger"));
	}

	@Test
	public void detectCompanionTest () {
		final CefQueryCallback mockCallback = mock(CefQueryCallback.class);
		final ObjectPersistencePacket packet = new ObjectPersistencePacket();
		final SavedGameOpener savedGameOpener = new SavedGameOpener("404", mockCallback);
		final ExposedClass exposedOpener = expose(savedGameOpener);

		packet.ObjectName = "Companion_Calisca";
		boolean result = (boolean) exposedOpener.call("detectCompanion", packet);
		assertTrue(result);

		packet.ObjectName = "Companion_Generic_(Clone)_1";
		result = (boolean) exposedOpener.call("detectCompanion", packet);
		assertFalse(result);

		packet.ObjectName = "Player_Fyorl";
		result = (boolean) exposedOpener.call("detectCompanion", packet);
		assertFalse(result);
	}
}
