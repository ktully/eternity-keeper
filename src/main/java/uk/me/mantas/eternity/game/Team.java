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

import uk.me.mantas.eternity.game.Faction.Relationship;
import uk.me.mantas.eternity.serializer.CSharpCollection;

import static uk.me.mantas.eternity.game.Reputation.ChangeStrength;
import static uk.me.mantas.eternity.game.UnityEngine.HideFlags;

public class Team {
	public String name;
	public HideFlags hideFlags;
	public Relationship DefaultRelationship;
	public FactionName GameFaction;
	public ChangeStrength InjuredReputationChange;
	public ChangeStrength MurderedReputationChange;
	public CSharpCollection FriendlyTeams;
	public CSharpCollection HostileTeams;
	public CSharpCollection NeutralTeams;
	public boolean RestoredTeam;
	public String ScriptTag;
	public String m_scriptTag;
	public String Tag;
}
