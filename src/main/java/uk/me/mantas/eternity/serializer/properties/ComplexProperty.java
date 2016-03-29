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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComplexProperty extends ReferenceTargetProperty {
	private static final Logger logger = Logger.getLogger(ComplexProperty.class);
	public List<Property> properties = new ArrayList<>();

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
			logger.error(
				"Tried to make ComplexProperty flat copy of %s!%n"
				, source.getClass().getSimpleName());
		}
	}

	public <T extends Property> Optional<T> findProperty (final String needle) {
		final Optional<Property> found =
			properties.stream()
				.filter(property -> property != null)
				.filter(property -> property.name.equalsIgnoreCase(needle))
				.findFirst();

		if (!found.isPresent()) {
			return Optional.empty();
		}

		//noinspection unchecked
		return Optional.of((T) found.get());
	}
}
