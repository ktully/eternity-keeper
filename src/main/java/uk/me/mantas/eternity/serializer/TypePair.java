package uk.me.mantas.eternity.serializer;

public class TypePair {
	public Class type;
	public String cSharpType;

	public TypePair (Class type, String cSharpType) {
		this.type = type;
		this.cSharpType = cSharpType;
	}
}
