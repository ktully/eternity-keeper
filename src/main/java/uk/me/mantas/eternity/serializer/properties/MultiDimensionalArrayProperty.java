package uk.me.mantas.eternity.serializer.properties;

import uk.me.mantas.eternity.serializer.TypePair;

import java.util.ArrayList;
import java.util.List;

public class MultiDimensionalArrayProperty extends ReferenceTargetProperty {
	public TypePair elementType;
	public List<MultiDimensionalArrayItem> items = new ArrayList<>();
	public List<ArrayDimension> dimensions = new ArrayList<>();

	public MultiDimensionalArrayProperty (String name, TypePair type) {
		super(name, type);
	}

	@Override
	public void makeFlatCopyFrom (ReferenceTargetProperty source) {
		if (source instanceof MultiDimensionalArrayProperty) {
			super.makeFlatCopyFrom(source);
			elementType = ((MultiDimensionalArrayProperty) source).elementType;
			dimensions = ((MultiDimensionalArrayProperty) source).dimensions;
			items = ((MultiDimensionalArrayProperty) source).items;
		} else {
			System.err.printf(
				"Tried to make MultiDimensionalArray flat copy of '%s'!%n"
				, source.getClass().getSimpleName());
		}
	}

	@Override
	public PropertyArt getPropertyArt () {
		return PropertyArt.MultiDimensionalArray;
	}

	public static class MultiDimensionalArrayItem {
		public int[] indexes;
		public Property value;

		public MultiDimensionalArrayItem (int[] indexes, Property value) {
			this.indexes = indexes;
			this.value = value;
		}
	}

	public static class ArrayDimension {
		public int lowerBound;
		public int length;

		public ArrayDimension () {}

		public ArrayDimension (int lowerBound, int length) {
			this.lowerBound = lowerBound;
			this.length = length;
		}
	}
}
