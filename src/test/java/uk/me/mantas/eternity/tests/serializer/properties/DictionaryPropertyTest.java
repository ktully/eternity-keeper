/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2015 the authors.
 * <p>
 * Eternity Keeper is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * Eternity Keeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package uk.me.mantas.eternity.tests.serializer.properties;

import com.google.common.collect.Maps;
import org.junit.Test;
import uk.me.mantas.eternity.serializer.properties.ComplexProperty;
import uk.me.mantas.eternity.serializer.properties.DictionaryProperty;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;

import java.util.ArrayList;
import java.util.Map.Entry;

import static org.junit.Assert.assertSame;

public class DictionaryPropertyTest {
	@Test
	public void findEntryTest () {
		final DictionaryProperty dictionary = new DictionaryProperty("Root", null);
		final ComplexProperty complexProperty = new ComplexProperty(null, null);
		final SimpleProperty simpleProperty = new SimpleProperty(null, null);
		final Property objectKeyProperty = new ComplexProperty(null, null);
		final Property stringKeyProperty = new ComplexProperty(null, null);
		final Object objectKey = new Object();
		final String stringKey = "FindMe";

		objectKeyProperty.obj = objectKey;
		stringKeyProperty.obj = stringKey;

		dictionary.items = new ArrayList<Entry<Property, Property>>() {{
			add(null);
			add(Maps.immutableEntry(objectKeyProperty, complexProperty));
			add(Maps.immutableEntry(stringKeyProperty, simpleProperty));
		}};

		assertSame(complexProperty, dictionary.<ComplexProperty>findEntry(objectKey).get());
		assertSame(simpleProperty, dictionary.<SimpleProperty>findEntry("FindMe").get());
	}
}
