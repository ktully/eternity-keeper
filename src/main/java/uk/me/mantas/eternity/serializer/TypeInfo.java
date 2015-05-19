package uk.me.mantas.eternity.serializer;

public class TypeInfo {
	public boolean isSimple = false;
	public boolean isArray = false;
	public boolean isEnumerable = false;
	public boolean isCollection = false;
	public boolean isDictionary = false;
	public Class type;
	public Class elementType;
	public Class keyType;
	public int dimensionCount;
}
