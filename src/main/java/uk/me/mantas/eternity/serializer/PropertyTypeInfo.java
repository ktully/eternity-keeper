package uk.me.mantas.eternity.serializer;

import uk.me.mantas.eternity.serializer.properties.Property;

public class PropertyTypeInfo {
	public Property property;
	public Class expectedPropertyType;
	public Class valueType;
	public String name;

	public PropertyTypeInfo (Property property, Class valueType) {
		this.property = property;
		this.expectedPropertyType = valueType;
		this.valueType = property.type.type;
		this.name = property.name;
	}

	public PropertyTypeInfo (
		Property property
		, Class expectedPropertyType
		, Class valueType) {

		this.property = property;
		this.expectedPropertyType = expectedPropertyType;
		this.valueType = valueType;
		this.name = property.name;
	}
}
