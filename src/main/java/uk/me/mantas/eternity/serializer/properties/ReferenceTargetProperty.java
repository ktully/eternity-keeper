package uk.me.mantas.eternity.serializer.properties;

public abstract class ReferenceTargetProperty extends Property {
	public Reference reference;

	protected ReferenceTargetProperty (String name, Class type) {
		super(name, type);
	}

	public void makeFlatCopyFrom (ReferenceTargetProperty source) {
		reference = source.reference;
	}
}
