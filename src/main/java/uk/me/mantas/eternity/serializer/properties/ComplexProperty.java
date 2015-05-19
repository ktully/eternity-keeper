package uk.me.mantas.eternity.serializer.properties;

import uk.me.mantas.eternity.serializer.TypePair;

import java.util.ArrayList;
import java.util.List;

public class ComplexProperty extends ReferenceTargetProperty {
	public List properties = new ArrayList<>();

	public ComplexProperty (String name, TypePair type) {
		super(name, type);
	}

	@Override
	public PropertyArt getPropertyArt () {
		return PropertyArt.Complex;
	}

	@Override
	public void makeFlatCopyFrom (ReferenceTargetProperty source) {
		if (source instanceof ComplexProperty) {
			super.makeFlatCopyFrom(source);
			properties = ((ComplexProperty) source).properties;
		} else {
			System.err.printf(
				"Tried to make ComplexProperty flat copy of %s!%n"
				, source.getClass().getSimpleName());
		}
	}
}
