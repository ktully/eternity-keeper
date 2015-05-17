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
