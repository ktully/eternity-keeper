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

import com.google.common.primitives.Shorts;
import com.google.common.primitives.UnsignedInteger;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.serializer.properties.*;

import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

import static java.util.AbstractMap.Entry;
import static java.util.AbstractMap.SimpleImmutableEntry;
import static uk.me.mantas.eternity.serializer.SharpSerializer.Elements;
import static uk.me.mantas.eternity.serializer.SharpSerializer.typeMap;

public class Deserializer {
	private static final Logger logger = Logger.getLogger(Deserializer.class);
	private final SharpSerializer parent;
	private final DataInput stream;
	private final List<String> names = new ArrayList<>();
	private final List<TypePair> types = new ArrayList<>();

	private Map<Integer, ReferenceTargetProperty> propertyCache =
		new HashMap<>();

	public Deserializer (DataInput stream, SharpSerializer parent) {
		this.stream = stream;
		this.parent = parent;

		try {
			readHeader(names, Function.identity());
			readHeader(types, this::convertToType);
		} catch (IOException e) {
			logger.error(
				"An error occurred whilst deserializing: %s%n"
				, e.getMessage());
		}
	}

	private TypePair convertToType (String s) {
		if (s == null) {
			return null;
		}

		String key = s.replaceAll("`\\d", "");

		if (key.contains(",")) {
			key = key.split(",")[0];
		}

		if (key.contains("[[")) {
			key = key.split("\\[\\[")[0];
		}

		Class value = typeMap.get(key);
		if (value == null) {
			logger.error(
				"Unable to find class corresponding to key '%s'.%n"
				, key);

			return new TypePair(Object.class, s);
		}

		return new TypePair(value, s);
	}

	private <T> void readHeader (
		List<T> items
		, Function<String, T> readCallback) throws IOException {

		int count = readNumber();

		for (int i = 0; i < count; i++) {
			String itemAsText = readString();
			T item = readCallback.apply(itemAsText);
			items.add(item);
		}
	}

	private String readString () throws IOException {
		if (!stream.readBoolean()) {
			return null;
		}

		return readCSharpString();
	}

	private String readCSharpString () throws IOException {
		int length = read7BitEncodedInt();
		byte[] buffer = new byte[length];
		stream.readFully(buffer);

		return new String(buffer, "UTF-8");
	}

	private int read7BitEncodedInt ()
		throws IOException, NumberFormatException {

		int count = 0;
		int shift = 0;
		byte b;

		do {
			if (shift == 5 * 7) {
				throw new NumberFormatException("Bad 7 bit encoded int.");
			}

			b = stream.readByte();
			count |= (b & 0x7f) << shift;
			shift += 7;
		} while ((b & 0x80) != 0);

		return count;
	}

	private int readNumber () throws IOException {
		byte size = stream.readByte();

		switch (size) {
			case 0:
				return 0;

			case 1:
				return stream.readByte() & 0xff;

			case 2:
				return stream.readShort();

			default:
				return stream.readInt();
		}
	}

	public Property deserialize () throws IOException {
		byte elementID = stream.readByte();
		return deserialize(elementID, null);
	}

	private Property deserialize (byte elementID, TypePair expectedType)
		throws IOException {

		String propertyName = readName();
		return deserialize(elementID, propertyName, expectedType);
	}

	private Property deserialize (
		byte elementID
		, String propertyName
		, TypePair expectedType) throws IOException {

		TypePair propertyType = readType();
		if (propertyType == null) {
			propertyType = new TypePair(expectedType.type, null);
		}

		int referenceID = 0;
		if (elementID == Elements.Reference
			|| Elements.isElementWithID(elementID)) {

			referenceID = readNumber();
			if (elementID == Elements.Reference) {
				return createProperty(referenceID, propertyName, propertyType);
			}
		}

		Property property =
			createProperty(elementID, propertyName, propertyType);

		if (property == null) {
			return null;
		}

		if (property instanceof NullProperty) {
			return property;
		}

		if (property instanceof SimpleProperty) {
			parseSimpleProperty((SimpleProperty) property);
			return property;
		}

		if (property instanceof ReferenceTargetProperty) {
			if (referenceID > 0) {
				((ReferenceTargetProperty) property).reference =
					new Reference(referenceID);

				((ReferenceTargetProperty) property).reference.isProcessed =
					true;

				propertyCache.put(
					referenceID
					, (ReferenceTargetProperty) property);
			}
		}

		// MultiDimensionalArray property.

		if (property instanceof SingleDimensionalArrayProperty) {
			parseSingleDimensionalArrayProperty(
				(SingleDimensionalArrayProperty) property);
			return property;
		}

		if (property instanceof DictionaryProperty) {
			parseDictionaryProperty((DictionaryProperty) property);
			return property;
		}

		if (property instanceof CollectionProperty) {
			parseCollectionProperty((CollectionProperty) property);
			return property;
		}

		if (property instanceof ComplexProperty) {
			parseComplexProperty((ComplexProperty) property);
			return property;
		}

		return property;
	}

	private void parseCollectionProperty (CollectionProperty property)
		throws IOException {

		property.elementType = readType();
		readProperties(property.properties, property.type.type);
		readItems(property.items, property.elementType);
	}

	private void parseDictionaryProperty (DictionaryProperty property)
		throws IOException {

		property.keyType = readType();
		property.valueType = readType();
		readProperties(property.properties, property.type.type);
		readDictionaryItems(
			property.items
			, property.keyType
			, property.valueType);
	}

	private void readDictionaryItems (
		List<Entry<Property, Property>> items
		, TypePair keyType
		, TypePair valueType)
		throws IOException {

		int count = readNumber();
		for (int i = 0; i < count; i++) {
			readDictionaryItem(items, keyType, valueType);
		}
	}

	private void readDictionaryItem (
		List<Entry<Property, Property>> items
		, TypePair keyType
		, TypePair valueType)
		throws IOException {

		// Key
		byte elementID = stream.readByte();
		Property keyProperty = deserialize(elementID, keyType);

		// Value
		elementID = stream.readByte();
		Property valueProperty = deserialize(elementID, valueType);

		Entry<Property, Property> item =
			new SimpleImmutableEntry<>(keyProperty, valueProperty);
		items.add(item);
	}

	private void parseSingleDimensionalArrayProperty (
		SingleDimensionalArrayProperty property)
		throws IOException {

		property.elementType = readType();
		property.lowerBound = readNumber();
		readItems(property.items, property.elementType);
	}

	private void readItems (List items, TypePair elementType) throws IOException {
		int count = readNumber();
		for (int i = 0; i < count; i++) {
			byte elementID = stream.readByte();
			Object subProperty = deserialize(elementID, elementType);

			//noinspection unchecked
			items.add(subProperty);
		}
	}

	private void parseComplexProperty (ComplexProperty property)
		throws IOException {

		readProperties(property.properties, property.type.type);
	}

	private void readProperties (List properties, Class ownerType)
		throws IOException {

		int count = readNumber();
		for (int i = 0; i < count; i++) {
			byte elementID = stream.readByte();
			String propertyName = readName();
			Field subPropertyInfo = null;

			try {
				subPropertyInfo = ownerType.getField(propertyName);
			} catch (NoSuchFieldException e) {
				logger.error(
					"No such field '%s' for type '%s'!%n"
					, propertyName
					, ownerType.getSimpleName());
			}

			Class propertyType =
				subPropertyInfo != null ? subPropertyInfo.getType() : null;

			Object subProperty = deserialize(
				elementID
				, propertyName
				, new TypePair(propertyType, null));

			//noinspection unchecked
			properties.add(subProperty);
		}
	}

	private void parseSimpleProperty (SimpleProperty property)
		throws IOException {

		property.value = readValue(property.type.type);
	}

	private Object readValue (Class expectedType) throws IOException {
		if (!stream.readBoolean()) {
			return null;
		}

		return readValueCore(expectedType);
	}

	private Object readValueCore (Class expectedType) throws IOException {
		try {
			if (expectedType.getSimpleName().equals("int")) {
				return stream.readInt();
			}

			if (expectedType.getSimpleName().equals("UnsignedInteger")) {
				return UnsignedInteger.valueOf((long) stream.readInt());
			}

			if (expectedType.getSimpleName().equals("String")) {
				return readCSharpString();
			}

			if (expectedType.getSimpleName().equals("boolean")) {
				return stream.readBoolean();
			}

			if (expectedType.getSimpleName().equals("Byte[]")) {
				int length = readNumber();
				if (length < 1) {
					return null;
				}

				byte[] buffer = new byte[length];
				stream.readFully(buffer);

				return boxBytes(buffer);
			}

			if (expectedType.getSimpleName().equals("UUID")) {
				ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
				stream.readFully(buffer.array());

				int a = Integer.reverseBytes(buffer.getInt(0));
				int b = Shorts.fromBytes(
					buffer.array()[5]
					, buffer.array()[4])
					& 0xffff;

				int c = Shorts.fromBytes(
					buffer.array()[7]
					, buffer.array()[6])
					& 0xffff;

				int d = buffer.array()[8] & 0xff;
				int e = buffer.array()[9] & 0xff;
				int f = buffer.array()[10] & 0xff;
				int g = buffer.array()[11] & 0xff;
				int h = buffer.array()[12] & 0xff;
				int i = buffer.array()[13] & 0xff;
				int j = buffer.array()[14] & 0xff;
				int k = buffer.array()[15] & 0xff;

				long mostSig = ((long) a << 32) + ((long) b << 16) + c;
				long leastSig = ((long) d << 56) + ((long) e << 48)
					+ ((long) f << 40) + ((long) g << 32) + ((long) h << 24)
					+ ((long) i << 16) + ((long) j << 8) + k;

				return new UUID(mostSig, leastSig);
			}

			if (expectedType.getSimpleName().equals("float")) {
				return stream.readFloat();
			}

			if (expectedType.isEnum()) {
				return readEnumeration(expectedType);
			}
		} catch (Exception e) {
			logger.error(
				"Unable to read property value: %s%n", e.getMessage());
		}

		if (expectedType != null) {
			String typeName = readCSharpString();
			Class javaType = typeMap.get(typeName);

			if (javaType == null) {
				logger.error(
					"No mapping found for type '%s', expected type was '%s'.%n"
					, typeName
					, expectedType.getSimpleName());
			}

			return javaType;
		} else {
			logger.error("Expected type was null in readValueCore.%n");
		}

		return null;
	}

	private Byte[] boxBytes (byte[] in) {
		Byte[] out = new Byte[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = in[i];
		}

		return out;
	}

	private Object readEnumeration (Class type) throws IOException {
		int value = stream.readInt();
		try {
			Object[] constants = type.getEnumConstants();
			try {
				for (Object constant : constants) {
					Field n = constant.getClass().getField("n");
					int enumVal = n.getInt(constant);
					if (enumVal == value) {
						return constant;
					}
				}

				return constants[value];
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return constants[value];
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error(
				"Tried to get enum value index #%d that "
					+ "didn't exist for enum %s.%n"
				, value
				, type.getSimpleName());
		}

		return null;
	}

	private Property createProperty (byte elementID, String propertyName, TypePair propertyType) {
		switch (elementID) {
			case Elements.SimpleObject:
				return new SimpleProperty(propertyName, propertyType);

			case Elements.ComplexObject:
			case Elements.ComplexObjectWithID:
				return new ComplexProperty(propertyName, propertyType);

			case Elements.SingleArray:
			case Elements.SingleArrayWithID:
				return new SingleDimensionalArrayProperty(propertyName, propertyType);

			case Elements.Dictionary:
			case Elements.DictionaryWithID:
				return new DictionaryProperty(propertyName, propertyType);

			case Elements.Collection:
				return new CollectionProperty(propertyName, propertyType);

			case Elements.Null:
				return new NullProperty(propertyName);

			default:
				logger.error("Unimplemented element ID #%d.%n", elementID);
		}

		return null;
	}

	private Property createProperty (
		int referenceID
		, String propertyName
		, TypePair propertyType) {

		ReferenceTargetProperty cachedProperty = propertyCache.get(referenceID);

		if (cachedProperty == null) {
			logger.error(
				"No cached property found for reference ID #%d!%n"
				, referenceID);

			return null;
		}

		ReferenceTargetProperty property =
			(ReferenceTargetProperty)
				Property.createInstance(
					cachedProperty.getPropertyArt()
					, propertyName
					, propertyType);

		cachedProperty.reference.count++;

		//noinspection ConstantConditions
		property.makeFlatCopyFrom(cachedProperty);
		property.reference = new Reference(referenceID);

		return property;
	}

	private TypePair readType () throws IOException {
		int index = readNumber();
		return types.get(index);
	}

	private String readName () throws IOException {
		int index = readNumber();
		return names.get(index);
	}
}
