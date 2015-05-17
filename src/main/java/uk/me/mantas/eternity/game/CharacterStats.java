package uk.me.mantas.eternity.game;

public class CharacterStats {
	public enum Race {
		Undefined
		, Human
		, Elf
		, Dwarf
		, Godlike
		, Orlan
		, Undead_DO_NOT_USE
		, Aumaua
		, Faunal_DO_NOT_USE
		, Giant_DO_NOT_USE
		, Beast
		, Primordial
		, Spirit
		, Vessel
		, Wilder
		, Count
	}

	public enum Subrace {
		Undefined
		, Meadow_Human
		, Ocean_Human
		, Savannah_Human
		, Wood_Elf
		, Snow_Elf
		, Mountain_Dwarf
		, Boreal_Dwarf
		, Death_Godlike
		, Fire_Godlike
		, Nature_Godlike
		, Moon_Godlike
		, Hearth_Orlan
		, Wild_Orlan
		, Coastal_Aumaua
		, Island_Aumaua
		, Avian_Godlike
	}

	public enum Culture {
		Undefined
		, Aedyr
		, DeadfireArchipelago
		, IxamitlPlains
		, OldVailia
		, Ruatai
		, TheLivingLands
		, TheWhiteThatWends
		, TheDyrwood
		, TheVailianRepublics
		, Nassitaq
		, EirGlanfath
		, Count
	}

	public enum Class {
		Undefined
		, Fighter
		, Rogue
		, Priest
		, Wizard
		, Barbarian
		, Ranger
		, Druid
		, Paladin
		, Monk
		, Cipher
		, Chanter
		, Troll
		, Ogre
		, Wolf
		, Spider
		, Ooze
		, Stelgaer
		, Imp
		, DankSpore
		, SwampLurker
		, Eoten
		, Xaurip
		, Vithrack
		, WillOWisp
		, Delemgan
		, Pwgra
		, Wurm
		, Skuldr
		, Drake
		, SkyDragon
		, AdraDragon
		, Blight
		, Animat
		, FleshConstruct
		, Shadow
		, Phantom
		, CeanGwla
		, Skeleton
		, Revenant
		, Gul
		, Dargul
		, Fampyr
		, Wicht
		, Beetle
		, AnimalCompanion
		, WeakEnemy
		, HeraldOfWoedica
		, PriestOfWoedica
		, Count
	}

	public enum Background {
		Undefined
		, Aristocrat
		, Artist
		, Colonist
		, Dissident
		, Drifter
		, Explorer
		, Hunter
		, Laborer
		, Mercenary
		, Merchant
		, Mystic
		, Philosopher
		, Priest
		, Raider
		, Slave
		, Scholar
		, Scientist
		, Farmer
		, Soldier
		, Midwife
		, Gentry
		, Count
	}

	public enum NoiseLevelType {
		Quiet
		, Loud
		, ExtremelyLoud
	}
}
