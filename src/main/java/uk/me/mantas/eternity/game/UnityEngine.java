package uk.me.mantas.eternity.game;

public class UnityEngine {
	public static class Colour {
		public float r;
		public float g;
		public float b;
		public float a;
	}

	public static class Vector3 {
		public float x;
		public float y;
		public float z;
	}

	public static class Vector2 {
		public float x;
		public float y;
	}

	// Need to work out a better way of doing this:
	public enum HideFlags {
		None
		, HideInHierarchy
		, HideInInspector
		, Pad1
		, DontSave
		, Pad2, Pad3, Pad4
		, NotEditable
		, Pad5, Pad6, Pad7, Pad8
		, HideAndDontSave
	}

	public static class GameObject {

	}

	public static class MonoBehaviour {

	}
}
