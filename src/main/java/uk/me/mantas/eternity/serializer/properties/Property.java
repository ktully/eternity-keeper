package uk.me.mantas.eternity.serializer.properties;

public abstract class Property {
	public String name;
	public Class type;
	public Property parent;

	Property (String name, Class type) {
		this.name = name;
		this.type = type;
	}

	public abstract PropertyArt getPropertyArt ();

	public static Property createInstance (
		PropertyArt art
		, String propertyName
		, Class propertyType) {

		switch (art) {
			case Collection:
				return new CollectionProperty(propertyName, propertyType);

			case Complex:
				return new ComplexProperty(propertyName, propertyType);

			case Dictionary:
				return new DictionaryProperty(propertyName, propertyType);

			case Null:
				return new NullProperty(propertyName);

			case Reference:
				return null;

			case Simple:
				return new SimpleProperty(propertyName, propertyType);

			case SingleDimensionalArray:
				return new SingleDimensionalArrayProperty(
					propertyName
					, propertyType);

			default:
				System.err.printf(
					"Unimplemented Property.createInstance for "
					+ "property of type '%s'!%n", art.name());

				return null;
		}
	}

	public enum PropertyArt {
		Unknown
		, Simple
		, Complex
		, Collection
		, Dictionary
		, SingleDimensionalArray
		, MultiDimensionalArray
		, Null
		, Reference
	}
}
