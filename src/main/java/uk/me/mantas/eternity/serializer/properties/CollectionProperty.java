package uk.me.mantas.eternity.serializer.properties;

import uk.me.mantas.eternity.serializer.TypePair;

import java.util.ArrayList;
import java.util.List;

public class CollectionProperty extends ComplexProperty {
	public List<Property> items = new ArrayList<>();
	public TypePair elementType;

	public CollectionProperty (String name, TypePair type) {
		super(name, type);
	}

	@Override
	public PropertyArt getPropertyArt () {
		return PropertyArt.Collection;
	}

	@Override
	public void makeFlatCopyFrom (ReferenceTargetProperty source) {
		if (source instanceof CollectionProperty) {
			super.makeFlatCopyFrom(source);
			elementType = ((CollectionProperty) source).elementType;
			items = ((CollectionProperty) source).items;
		} else {
			System.err.printf(
				"Tried to make CollectionProperty flat copy of %s!%n"
				, source.getClass().getSimpleName());
		}
	}
}
