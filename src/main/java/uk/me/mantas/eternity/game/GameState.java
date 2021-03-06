/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2016 the authors.
 * <p>
 * Eternity Keeper is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * Eternity Keeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.me.mantas.eternity.game;

import uk.me.mantas.eternity.game.UnityEngine.TextAsset;

public class GameState {
	public boolean RetroactiveSpellMasteryChecked;
	public TextAsset TrialOfIronReadme;
	public boolean IsDifficultyPotd;
	public boolean IsDifficultyStoryTime;
	public GameDifficulty Difficulty;
	public DifficultyScaling.Scaler ActiveScalers;
	public boolean TrialOfIron;
	public boolean ExpertMode;
	public boolean UiWorldMapTutorialFinished;
	public boolean HasNotifiedPX2Installation;
	public boolean CheatsEnabled;
	public boolean IgnoreSpellLimits;
	public boolean IgnoreInGrimoire;
	public boolean HasEnteredPX1;
	public boolean HasEnteredPX2;
	public int AutosaveCycleNumber;
	public MapData CurrentNextMap;
	public MapData CurrentMap;
	public boolean CouldAccessStashOnLastMap;
	public boolean CurrentMapIsStronghold;
}
