package uk.me.mantas.eternity.serializer.properties;

import uk.me.mantas.eternity.serializer.TypePair;

public abstract class ReferenceTargetProperty extends Property {
	public Reference reference;

	protected ReferenceTargetProperty (String name, TypePair type) {
		super(name, type);
	}

	public void makeFlatCopyFrom (ReferenceTargetProperty source) {
		reference = source.reference;
		obj = source.obj;
	}
}
