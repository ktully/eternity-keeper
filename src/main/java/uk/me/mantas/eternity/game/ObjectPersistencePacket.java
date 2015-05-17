package uk.me.mantas.eternity.game;

import java.util.UUID;

import static uk.me.mantas.eternity.game.UnityEngine.Vector3;

public class ObjectPersistencePacket {
	public String ObjectName;
	public String LevelName;
	public String PrefabResource;
	public boolean Mobile;
	public boolean Global;
	public UUID GUID;
	public String ObjectID;
	public Vector3 Location;
	public Vector3 Rotation;
	public boolean Packed;
	public boolean LoadManually;
	public String Parent;
	public ComponentPersistencePacket[] ComponentPackets;
}
