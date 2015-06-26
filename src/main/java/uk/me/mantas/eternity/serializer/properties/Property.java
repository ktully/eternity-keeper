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

import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.serializer.TypePair;

import java.util.List;
import java.util.Optional;

public abstract class Property {
	private static final Logger logger = Logger.getLogger(Property.class);
	public String name;
	public TypePair type;
	public Property parent;
	public Object obj;

	Property (String name, TypePair type) {
		this.name = name;
		this.type = type;
	}

	public abstract PropertyArt getPropertyArt ();

	public static Property createInstance (
		PropertyArt art
		, String propertyName
		, TypePair propertyType) {

		switch (art) {
			case Collection:
				return new CollectionProperty(propertyName, propertyType);

			case Complex:
				return new ComplexProperty(propertyName, propertyType);

			case Dictionary:
				return new DictionaryProperty(propertyName, propertyType);

			case Null:
				return new NullProperty(propertyName);

			case Reference:
				return null;

			case Simple:
				return new SimpleProperty(propertyName, propertyType);

			case SingleDimensionalArray:
				return new SingleDimensionalArrayProperty(
					propertyName
					, propertyType);

			default:
				logger.error(
					"Unimplemented Property.createInstance for "
					+ "property of type '%s'!%n", art.name());

				return null;
		}
	}

	public static boolean update (
		Property property
		, String propertyName
		, Object value) {

		Optional<Property> subProperty = find(property, propertyName);
		if (!subProperty.isPresent()) {
			logger.error(
				"Unable to locate sub-property with name '%s'.%n"
				, propertyName);

			return false;
		}

		if (!(subProperty.get() instanceof SimpleProperty)) {
			logger.error(
				"Sub-property '%s' was not a SimpleProperty.%n"
				, propertyName);

			return false;
		}

		SimpleProperty simpleSubProperty = (SimpleProperty) subProperty.get();
		simpleSubProperty.value = value;
		simpleSubProperty.obj = value;

		return true;
	}

	@SuppressWarnings("unchecked")
	public static Optional<Property> find (Property haystack, String needle) {
		if (!(haystack instanceof ComplexProperty)) {
			return Optional.empty();
		}

		ComplexProperty complexHaystack = (ComplexProperty) haystack;
		for (Property subProperty :
			(List<Property>) complexHaystack.properties) {

			if (subProperty.name.equals(needle)) {
				return Optional.of(subProperty);
			}
		}

		return Optional.empty();
	}

	public enum PropertyArt {
		Unknown
		, Simple
		, Complex
		, Collection
		, Dictionary
		, SingleDimensionalArray
		, MultiDimensionalArray
		, Null
		, Reference
	}
}
