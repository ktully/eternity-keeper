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


package uk.me.mantas.eternity.serializer;

import uk.me.mantas.eternity.serializer.properties.Property;

public class PropertyTypeInfo {
	public Property property;
	public Class expectedPropertyType;
	public String cSharpExpectedPropertyType;
	public Class valueType;
	public String cSharpValueType;
	public String name;

	public PropertyTypeInfo (
		Property property
		, Class valueType
		, String cSharpValueType) {

		this.property = property;
		this.name = property.name;
		this.expectedPropertyType = valueType;
		this.cSharpExpectedPropertyType = cSharpValueType;
		this.valueType = (property.type != null) ? property.type.type : null;
		this.cSharpValueType =
			(property.type != null) ? property.type.cSharpType : null;
	}

	public PropertyTypeInfo (
		Property property
		, Class expectedPropertyType
		, String cSharpExpectedPropertyType
		, Class valueType
		, String cSharpValueType) {

		this.property = property;
		this.expectedPropertyType = expectedPropertyType;
		this.cSharpExpectedPropertyType = cSharpExpectedPropertyType;
		this.valueType = valueType;
		this.cSharpValueType = cSharpValueType;
		this.name = property.name;
	}
}
