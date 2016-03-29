/**
 *  Eternity Keeper, a Pillars of Eternity save game editor.
 *  Copyright (C) 2015 the authors.
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
		final Property property
		, final String propertyName
		, final Object value) {

		final Optional<Property> subProperty = find(property, propertyName);
		if (!subProperty.isPresent()) {
			logger.error("Unable to locate sub-property with name '%s'.%n", propertyName);
			return false;
		}

		return update(subProperty.get(), value);
	}

	public static boolean update (final Property property, final Object value) {
		if (!(property instanceof SimpleProperty)) {
			logger.error("Property was not a SimpleProperty.%n");
			return false;
		}

		final SimpleProperty simpleProperty = (SimpleProperty) property;
		simpleProperty.value = value;
		simpleProperty.obj = value;

		return true;
	}

	public static Optional<Property> find (final Property haystack, final String needle) {
		if (!(haystack instanceof ComplexProperty)) {
			return Optional.empty();
		}

		final ComplexProperty complexHaystack = (ComplexProperty) haystack;
		for (final Property subProperty : complexHaystack.properties) {
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
