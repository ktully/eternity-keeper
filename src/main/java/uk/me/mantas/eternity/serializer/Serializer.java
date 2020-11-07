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

import uk.me.mantas.eternity.serializer.properties.*;
import uk.me.mantas.eternity.serializer.write.*;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static uk.me.mantas.eternity.serializer.SharpSerializer.Elements;
import static uk.me.mantas.eternity.serializer.properties.MultiDimensionalArrayProperty.ArrayDimension;
import static uk.me.mantas.eternity.serializer.properties.MultiDimensionalArrayProperty.MultiDimensionalArrayItem;

public class Serializer {
	private static final String rootName = "Root";
	private final List<WriteCommand> commandCache = new ArrayList<>();
	private final IndexGenerator<String> types = new IndexGenerator<>();
	private final IndexGenerator<String> names = new IndexGenerator<>();

	private final BinaryWriter stream;
	private SerializerFormat format = SerializerFormat.PRESERVE;

	public Serializer (DataOutput stream) {
		this.stream = new BinaryWriter(stream);
		this.format = format;
	}

	public Serializer toFormat(SerializerFormat format) {
		this.format = format;

		return this;
	}

	public void serialize (Property property) throws IOException {
		if (property == null) {
			throw new IllegalArgumentException(
				"Tried to serialize null object.");
		}

		if (!property.name.equals(rootName)) {
			throw new IllegalArgumentException(
				"Attempted to serialize non-root property!");
		}

		serializeCore(property);
		writeNamesHeader();
		writeTypesHeader();
		writeCache();
	}

	private void writeCache () throws IOException {
		for (WriteCommand command : commandCache) {
			command.write(stream);
		}
	}

	private void writeTypesHeader () throws IOException {
		stream.writeNumber(types.items.size());
		for (String type : types.items) {
			String typeName = convertToTypeName(type);
			stream.writeStringGuarded(typeName);
		}
	}

	private String convertToTypeName (String type) {
		if (type == null) return null;

		switch (format) {
			case UNITY_2017:
				type = TypeMap.getBackwardsCompatibleType(type);
				break;

			case PRESERVE:
			default:
				break;
		}

		return type;
	}

	private void writeNamesHeader () throws IOException {
		stream.writeNumber(names.items.size());
		for (String name : names.items) {
			stream.writeStringGuarded(name);
		}
	}

	private void serializeCore (Property property) {
		if (property == null) {
			throw new IllegalArgumentException(
				"Cannot serialize null property!");
		}

		if (property instanceof NullProperty) {
			serializeNullProperty(property);
			return;
		}

		if (property instanceof SimpleProperty) {
			serializeSimpleProperty(property);
			return;
		}

		if (serializeReference(((ReferenceTargetProperty) property))) {
			return;
		}

		serializeReferenceTarget((ReferenceTargetProperty) property);
	}

	private void serializeSimpleProperty (Property property) {
		writePropertyHeader(Elements.SimpleObject, property.name, property);
		writeValue(property.obj);
	}

	private void writeValue (Object value) {
		commandCache.add(new ValueWriteCommand(value));
	}

	private void writePropertyHeader (
		byte elementID
		, String name
		, Property property) {

		writeElementID(elementID);
		writeName(name);
		writeType(property != null ? property.type : null);
	}

	private void writeType (TypePair type) {
		int index = types.getIndexOfItem(type != null ? type.cSharpType : null);
		commandCache.add(new NumberWriteCommand(index));
	}

	private void writeName (String name) {
		int index = names.getIndexOfItem(name);
		commandCache.add(new NumberWriteCommand(index));
	}

	private void writeElementID (byte elementID) {
		commandCache.add(new ByteWriteCommand(elementID));
	}

	private void serializeNullProperty (Property property) {
		writePropertyHeader(Elements.Null, property.name, property);
	}

	private void serializeReferenceTarget (ReferenceTargetProperty property) {
		if (property.reference != null) {
			property.reference.isProcessed = true;
		}

		if (property instanceof MultiDimensionalArrayProperty) {
			serializeMultiDimensionalArrayProperty(property);
			return;
		}

		if (property instanceof SingleDimensionalArrayProperty) {
			serializeSingleDimensionalArrayProperty(property);
			return;
		}

		if (property instanceof DictionaryProperty) {
			serializeDictionaryProperty(property);
			return;
		}

		if (property instanceof CollectionProperty) {
			serializeCollectionProperty(property);
			return;
		}

		serializeComplexProperty(property);
	}

	private void serializeMultiDimensionalArrayProperty (Property property) {

		if (!writePropertyHeaderWithReferenceID(
			Elements.MultiArrayWithID
			, ((ReferenceTargetProperty) property).reference
			, property.name
			, property)) {

			writePropertyHeader(Elements.MultiArray, property.name, property);
		}

		MultiDimensionalArrayProperty arrayProperty =
			(MultiDimensionalArrayProperty) property;

		writeType(arrayProperty.elementType);
		writeDimensions(arrayProperty.dimensions);
		writeMultiDimensionalArrayItems(arrayProperty.items);
	}

	private void writeMultiDimensionalArrayItems (
		List<MultiDimensionalArrayItem> items) {

		writeNumber(items.size());
		for (MultiDimensionalArrayItem item : items) {
			writeMultiDimensionalArrayItem(item);
		}
	}

	private void writeMultiDimensionalArrayItem (
		MultiDimensionalArrayItem item) {

		writeNumbers(item.indexes);
		serializeCore(item.value);
	}

	private void writeNumbers (int[] n) {
		commandCache.add(new NumbersWriteCommand(n));
	}

	private void writeDimensions (List<ArrayDimension> dimensions) {
		writeNumber(dimensions.size());
		for (ArrayDimension dim : dimensions) {
			writeDimension(dim);
		}
	}

	private void writeDimension (ArrayDimension dim) {
		writeNumber(dim.length);
		writeNumber(dim.lowerBound);
	}

	private void serializeComplexProperty (Property property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.ComplexObjectWithID
			, ((ReferenceTargetProperty) property).reference
			, property.name
			, property)) {

			writePropertyHeader(
				Elements.ComplexObject
				, property.name
				, property);
		}

		writeProperties(((ComplexProperty) property).properties);
	}

	@SuppressWarnings("unchecked")
	private void writeProperties (List properties) {
		writeNumber((short) properties.size());
		for (Property property : (List<Property>) properties) {
			serializeCore(property);
		}
	}

	private boolean writePropertyHeaderWithReferenceID (
		byte elementID
		, Reference reference
		, String name
		, Property property) {

		if (reference == null || reference.count < 2) {
			return false;
		}

		writePropertyHeader(elementID, name, property);
		writeNumber(reference.id);
		return true;
	}

	private void writeNumber (int n) {
		commandCache.add(new NumberWriteCommand(n));
	}

	private void serializeCollectionProperty (Property property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.CollectionWithID
			, ((ReferenceTargetProperty) property).reference
			, property.name
			, property)) {

			writePropertyHeader(Elements.Collection, property.name, property);
		}

		CollectionProperty listProperty = (CollectionProperty) property;

		writeType(listProperty.elementType);
		writeProperties(listProperty.properties);
		writeItems(listProperty.items);
	}

	@SuppressWarnings("unchecked")
	private void writeItems (List items) {
		writeNumber(items.size());
		for (Property item : (List<Property>) items) {
			serializeCore(item);
		}
	}

	private void serializeDictionaryProperty (Property property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.DictionaryWithID
			, ((ReferenceTargetProperty) property).reference
			, property.name
			, property)) {

			writePropertyHeader(Elements.Dictionary, property.name, property);
		}

		DictionaryProperty dictProperty = (DictionaryProperty) property;

		writeType(dictProperty.keyType);
		writeType(dictProperty.valueType);
		writeProperties(dictProperty.properties);
		writeDictionaryItems(dictProperty.items);
	}

	private void writeDictionaryItems (List<Entry<Property, Property>> items) {
		writeNumber(items.size());
		for (Entry<Property, Property> item : items) {
			writeDictionaryItem(item);
		}
	}

	private void writeDictionaryItem (Entry<Property, Property> item) {
		serializeCore(item.getKey());
		serializeCore(item.getValue());
	}

	private void serializeSingleDimensionalArrayProperty (Property property) {

		if (!writePropertyHeaderWithReferenceID(
			Elements.SingleArrayWithID
			, ((ReferenceTargetProperty) property).reference
			, property.name
			, property)) {

			writePropertyHeader(Elements.SingleArray, property.name, property);
		}

		SingleDimensionalArrayProperty arrayProperty =
			(SingleDimensionalArrayProperty) property;

		writeType(arrayProperty.elementType);
		writeNumber(arrayProperty.lowerBound);
		writeItems(arrayProperty.items);
	}

	private boolean serializeReference (ReferenceTargetProperty property) {
		if (property.reference == null) {
			return false;
		}

		if (property.reference.count < 2 && !property.reference.isProcessed) {
			writePropertyHeader(Elements.Reference, property.name, null);
			writeNumber(property.reference.id);
			return true;
		}

		return false;
	}
}
