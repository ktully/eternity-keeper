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


package uk.me.mantas.eternity.tests;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.me.mantas.eternity.EKUtils;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.game.ComponentPersistencePacket;
import uk.me.mantas.eternity.game.ObjectPersistencePacket;
import uk.me.mantas.eternity.handlers.GetGameStructures;
import uk.me.mantas.eternity.serializer.properties.ComplexProperty;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SingleDimensionalArrayProperty;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.me.mantas.eternity.EKUtils.*;

public class EKUtilsTest {
	@BeforeClass
	public static void setDefaultLocale () {
		Locale.setDefault(Locale.UK);
	}

	@Test
	public void removeBOMTest () {
		byte[] data = new byte[]{-17, -69, -65, 100, 97, 116, 97};
		byte[] actual = removeBOM(data);
		byte[] expected = new byte[]{100, 97, 116, 97};
		assertArrayEquals(expected, actual);
	}

	@Test
	public void addBOMTest () {
		byte[] actual = addBOM(new byte[]{});
		byte[] expected = new byte[]{-17, -69, -65};
		assertArrayEquals(expected, actual);
	}

	@Test
	public void removeExtensionTest () {
		assertNull(removeExtension(null));
		assertEquals("", removeExtension(""));
		assertEquals("noextension", removeExtension("noextension"));
		assertEquals("noextension", removeExtension("noextension.extension"));
	}

	@Test
	public void getExtensionTest () {
		assertFalse(getExtension(null).isPresent());
		assertFalse(getExtension("").isPresent());
		assertFalse(getExtension("noextension").isPresent());
		assertEquals("ext", getExtension("file.ext").get());
		assertEquals("ext", getExtension("file.part.ext").get());
	}

	@Test
	public void findPropertyTest () {
		final Property needle = mock(Property.class);
		final Property needle2 = mock(Property.class);
		final ObjectPersistencePacket packet = mock(ObjectPersistencePacket.class);
		final List<Property> haystack = new ArrayList<Property>() {{
			add(needle);
			add(needle2);
		}};

		packet.ObjectName = "FindMe";
		needle.obj = packet;
		needle2.obj = packet;

		assertFalse(findProperty(haystack, "404").isPresent());
		assertSame(needle, findProperty(haystack, "FINDME").get());
	}

	@Test
	public void findComponentTest () {
		final ComponentPersistencePacket needle = mock(ComponentPersistencePacket.class);
		final ComponentPersistencePacket needle2 = mock(ComponentPersistencePacket.class);
		final ComponentPersistencePacket[] haystack = new ComponentPersistencePacket[] {
			null
			, needle
			, needle2
		};

		needle.TypeString = "FindMe";
		needle2.TypeString = "FindMe";

		assertFalse(findComponent(haystack, "404").isPresent());
		assertSame(needle, findComponent(haystack, "FINDME").get());
	}

	@Test
	public void findSubComponentTest () {
		final SingleDimensionalArrayProperty haystack = mock(SingleDimensionalArrayProperty.class);
		final ComplexProperty needleProperty = mock(ComplexProperty.class);
		final ComplexProperty needleProperty2 = mock(ComplexProperty.class);
		final ComponentPersistencePacket needle = mock(ComponentPersistencePacket.class);
		final ComponentPersistencePacket needle2 = mock(ComponentPersistencePacket.class);

		needle.TypeString = "FindMe";
		needle2.TypeString = "FindMe";

		needleProperty.obj = needle;
		needleProperty2.obj = needle2;

		haystack.items = new ArrayList<ComplexProperty>() {{
			add(null);
			add(needleProperty);
			add(needleProperty2);
		}};

		assertFalse(findSubComponent(haystack, "404").isPresent());
		assertSame(needleProperty, findSubComponent(haystack, "FINDME").get());
	}

	private enum Enum {A, B}
	private static class NotAnEnum {}

	@Test
	public void enumConstantNameTest () {
		final Optional<String> testA = EKUtils.enumConstantName(Enum.A);
		final Optional<String> testB = EKUtils.enumConstantName(Enum.B);
		final Optional<String> testFail = enumConstantName(new NotAnEnum());

		assertTrue(testA.isPresent());
		assertEquals("A", testA.get());
		assertTrue(testB.isPresent());
		assertEquals("B", testB.get());
		assertFalse(testFail.isPresent());
	}
}
