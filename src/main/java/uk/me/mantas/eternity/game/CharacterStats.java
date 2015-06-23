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
