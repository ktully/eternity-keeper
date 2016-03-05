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

import org.junit.Test;
import uk.me.mantas.eternity.serializer.properties.ComplexProperty;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;

import java.util.ArrayList;

import static org.junit.Assert.assertSame;

public class ComplexPropertyTest {
	@Test
	public void findPropertyTest () {
		final ComplexProperty root = new ComplexProperty("Root", null);
		final SimpleProperty simpleProperty = new SimpleProperty("Simple", null);
		final SimpleProperty simpleProperty2 = new SimpleProperty("Simple", null);
		final ComplexProperty complexProperty = new ComplexProperty("Complex", null);

		root.properties = new ArrayList<Property>() {{
			add(null);
			add(simpleProperty);
			add(complexProperty);
			add(simpleProperty2);
		}};

		assertSame(complexProperty, root.<ComplexProperty>findProperty("complex").get());
		assertSame(simpleProperty, root.<SimpleProperty>findProperty("simple").get());
	}
}
