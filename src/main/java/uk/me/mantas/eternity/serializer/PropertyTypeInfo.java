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
