/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 Kim Mantas
 *
 *  Eternity Keeper is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  Eternity Keeper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
