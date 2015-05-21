package uk.me.mantas.eternity.serializer;

public class TypePair {
	public Class type;
	public String cSharpType;

	public TypePair (Class type, String cSharpType) {
		this.type = type;
		this.cSharpType = cSharpType;
	}

	@Override
	public boolean equals (Object obj) {
		if (!(obj instanceof TypePair)) {
			return false;
		}

		TypePair other = (TypePair) obj;
		return other.type == type && other.cSharpType.equals(cSharpType);
	}
}
