package uk.me.mantas.eternity.serializer;

import com.google.common.primitives.Shorts;
import uk.me.mantas.eternity.serializer.properties.*;

import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.AbstractMap.Entry;
import static java.util.AbstractMap.SimpleImmutableEntry;
import static uk.me.mantas.eternity.serializer.SharpSerializer.Elements;
import static uk.me.mantas.eternity.serializer.SharpSerializer.typeMap;

public class Deserializer {
	private final DataInput stream;
	private final List<String> names = new ArrayList<>();
	private final List<Class> types = new ArrayList<>();

	private Map<Integer, ReferenceTargetProperty> propertyCache =
		new HashMap<>();

	public Deserializer (DataInput stream) {
		this.stream = stream;

		try {
			readHeader(names, Function.identity());
			readHeader(types, Deserializer::convertToType);
		} catch (IOException e) {
			System.err.printf(
				"An error occurred whilst deserializing: %s%n"
				, e.getMessage());
		}
	}

	private static Class convertToType (String s) {
		if (s == null) {
			return null;
		}

		String key = s.replaceAll("`\\d", "");
		String innerClass = null;
		Pattern innerClassPattern = Pattern.compile("\\[\\[([^]]+)\\]\\]");
		Matcher innerClassMatcher = innerClassPattern.matcher(s);

		if (key.contains(",")) {
			key = key.split(",")[0];
		}

		if (key.contains("[[")) {
			key = key.split("\\[\\[")[0];
		}

		if (innerClassMatcher.find()) {
			innerClass = innerClassMatcher.group(1);
			if (innerClass != null && innerClass.contains(",")) {
				innerClass = innerClass.split(",")[0];
			}
		}

		//noinspection StatementWithEmptyBody
		if (innerClass != null) {
			// At the moment we don't care about the inner class since all our
			// collections are of type Object.
			//key = String.format("%s<%s>", key, innerClass);
		}

		Class value = typeMap.get(key);
		if (value == null) {
			System.err.printf(
				"Unable to find class corresponding to key '%s'.%n"
				, key);

			return Object.class;
		}

		return value;
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

		return new String(buffer);
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

	private Property deserialize (byte elementID, Class expectedType)
		throws IOException {

		String propertyName = readName();
		return deserialize(elementID, propertyName, expectedType);
	}

	private Property deserialize (
		byte elementID
		, String propertyName
		, Class expectedType) throws IOException {

		Class propertyType = readType();
		if (propertyType == null) {
			propertyType = expectedType;
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
		readProperties(property.properties, property.type);
		readItems(property.items, property.elementType);
	}

	private void parseDictionaryProperty (DictionaryProperty property)
		throws IOException {

		property.keyType = readType();
		property.valueType = readType();
		readProperties(property.properties, property.type);
		readDictionaryItems(
			property.items
			, property.keyType
			, property.valueType);
	}

	private void readDictionaryItems (
		List<Entry<Property, Property>> items
		, Class keyType
		, Class valueType)
		throws IOException {

		int count = readNumber();
		for (int i = 0; i < count; i++) {
			readDictionaryItem(items, keyType, valueType);
		}
	}

	private void readDictionaryItem (
		List<Entry<Property, Property>> items
		, Class keyType
		, Class valueType)
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

	private void readItems (List items, Class elementType) throws IOException {
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

		readProperties(property.properties, property.type);
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
				System.err.printf(
					"No such field '%s' for type '%s'!%n"
					, propertyName
					, ownerType.getSimpleName());
			}

			Class propertyType =
				subPropertyInfo != null ? subPropertyInfo.getType() : null;

			Object subProperty = deserialize(
				elementID
				, propertyName
				, propertyType);

			//noinspection unchecked
			properties.add(subProperty);
		}
	}

	private void parseSimpleProperty (SimpleProperty property)
		throws IOException {

		property.value = readValue(property.type);
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
				return (long) stream.readInt() & 0xffff;
			}

			if (expectedType.getSimpleName().equals("String")) {
				return readCSharpString();
			}

			if (expectedType.getSimpleName().equals("boolean")) {
				return stream.readBoolean();
			}

			if (expectedType.getSimpleName().equals("UUID")) {
				ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
				stream.readFully(buffer.array());

				int a = Integer.reverseBytes(buffer.getInt(0));
				short b = Shorts.fromBytes(buffer.array()[5], buffer.array()[4]);
				short c = Shorts.fromBytes(buffer.array()[7], buffer.array()[6]);
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
			System.err.printf(
				"Unable to read property value: %s%n", e.getMessage());
		}

		if (expectedType != null) {
			String typeName = readCSharpString();
			Class javaType = typeMap.get(typeName);

			if (javaType == null) {
				System.err.printf(
					"No mapping found for type '%s'!%n", typeName);
			}

			return javaType;
		} else {
			System.err.printf("Expected type was null in readValueCore.%n");
		}

		return null;
	}

	private Object readEnumeration (Class type) throws IOException {
		int value = stream.readInt();
		try {
			return type.getEnumConstants()[value];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.printf(
				"Tried to get enum value index #%d that "
				+ "didn't exist for enum %s.%n"
				, value
				, type.getSimpleName());
		}

		return null;
	}

	private Property createProperty (
		byte elementID
		, String propertyName
		, Class propertyType) {

		switch (elementID) {
			case Elements.SimpleObject:
				return new SimpleProperty(propertyName, propertyType);

			case Elements.ComplexObject:
			case Elements.ComplexObjectWithID:
				return new ComplexProperty(propertyName, propertyType);

			case Elements.SingleArray:
				return new SingleDimensionalArrayProperty(
					propertyName
					, propertyType);

			case Elements.Dictionary:
				return new DictionaryProperty(propertyName, propertyType);

			case Elements.Collection:
				return new CollectionProperty(propertyName, propertyType);

			case Elements.Null:
				return new NullProperty(propertyName);

			default:
				System.err.printf("Unimplemented element ID #%d.%n", elementID);
		}

		return null;
	}

	private Property createProperty (
		int referenceID
		, String propertyName
		, Class propertyType) {

		ReferenceTargetProperty cachedProperty = propertyCache.get(referenceID);

		if (cachedProperty == null) {
			System.err.printf(
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

	private Class readType () throws IOException {
		int index = readNumber();
		return types.get(index);
	}

	private String readName () throws IOException {
		int index = readNumber();
		return names.get(index);
	}
}
