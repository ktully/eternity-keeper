package uk.me.mantas.eternity.serializer.properties;

import uk.me.mantas.eternity.serializer.TypePair;

import java.util.ArrayList;
import java.util.List;

import static java.util.Map.Entry;

public class DictionaryProperty extends ComplexProperty {
	public TypePair keyType;
	public TypePair valueType;
	public List<Entry<Property, Property>> items = new ArrayList<>();

	public DictionaryProperty (String name, TypePair type) {
		super(name, type);
	}

	@Override
	public PropertyArt getPropertyArt () {
		return PropertyArt.Dictionary;
	}

	@Override
	public void makeFlatCopyFrom (ReferenceTargetProperty source) {
		if (source instanceof DictionaryProperty) {
			super.makeFlatCopyFrom(source);
			keyType = ((DictionaryProperty) source).keyType;
			valueType = ((DictionaryProperty) source).valueType;
			items = ((DictionaryProperty) source).items;
		} else {
			System.err.printf(
				"Tried to make DictionaryProperty flat copy of %s!%n"
				, source.getClass().getSimpleName());
		}
	}
}
