package uk.me.mantas.eternity.game;

public class OCL extends Usable {
	public float UseRadius = 2f;
	public boolean RemoveUsedKey = true;
	public int LockDifficulty = 1;
	public State StartingState;
	public float ArrivalDistance;
	public Item Key;
	public Item LockPickItem;
	public boolean MustHaveKey;
	public float UsableRadius;
	public float ArrivalRadius;
	public boolean IsUsable;
	public State CurrentState;

	public enum State {
		Closed
		, Open
		, Locked
		, Sealed
		, SealedOpen
	}
}
