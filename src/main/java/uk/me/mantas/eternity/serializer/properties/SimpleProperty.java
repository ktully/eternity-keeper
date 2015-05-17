package uk.me.mantas.eternity.serializer.properties;

public class SimpleProperty extends Property {
	public Object value;

	public SimpleProperty (String propertyName, Class propertyType) {
		super(propertyName, propertyType);
	}

	@Override
	public PropertyArt getPropertyArt () {
		return PropertyArt.Simple;
	}
}
