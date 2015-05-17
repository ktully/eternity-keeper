package uk.me.mantas.eternity.serializer.properties;

public class NullProperty extends Property {
	public NullProperty (String name) {
		super(name, null);
	}

	@Override
	public PropertyArt getPropertyArt () {
		return PropertyArt.Null;
	}
}
