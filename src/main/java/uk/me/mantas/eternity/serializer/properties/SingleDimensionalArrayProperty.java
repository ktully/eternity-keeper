package uk.me.mantas.eternity.serializer.properties;

import java.util.ArrayList;
import java.util.List;

public class SingleDimensionalArrayProperty extends ReferenceTargetProperty {
	public Class elementType;
	public int lowerBound;
	public List items = new ArrayList<>();

	public SingleDimensionalArrayProperty (String name, Class type) {
		super(name, type);
	}

	@Override
	public PropertyArt getPropertyArt () {
		return PropertyArt.SingleDimensionalArray;
	}

	@Override
	public void makeFlatCopyFrom (ReferenceTargetProperty source) {
		if (source instanceof SingleDimensionalArrayProperty) {
			super.makeFlatCopyFrom(source);
			lowerBound = ((SingleDimensionalArrayProperty) source).lowerBound;
			elementType = ((SingleDimensionalArrayProperty) source).elementType;
			items = ((SingleDimensionalArrayProperty) source).items;
		} else {
			System.err.printf(
				"Tried to make SingleDimensionalArrayProperty "
				+ "flat copy of %s!%n"
				, source.getClass().getSimpleName());
		}
	}
}
