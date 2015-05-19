package uk.me.mantas.eternity.serializer.properties;

import uk.me.mantas.eternity.serializer.TypePair;

public class SimpleProperty extends Property {
	public Object value;

	public SimpleProperty (String propertyName, TypePair propertyType) {
		super(propertyName, propertyType);
	}

	@Override
	public PropertyArt getPropertyArt () {
		return PropertyArt.Simple;
	}
}
