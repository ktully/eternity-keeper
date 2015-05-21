package uk.me.mantas.eternity.serializer;

import com.google.common.primitives.UnsignedInteger;
import uk.me.mantas.eternity.serializer.properties.*;
import uk.me.mantas.eternity.serializer.write.*;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.me.mantas.eternity.serializer.SharpSerializer.Elements;
import static uk.me.mantas.eternity.serializer.properties.MultiDimensionalArrayProperty.ArrayDimension;
import static uk.me.mantas.eternity.serializer.properties.MultiDimensionalArrayProperty.MultiDimensionalArrayItem;

public class Serializer {
	private static final String rootName = "Root";
	private final List<WriteCommand> commandCache = new ArrayList<>();
	private final IndexGenerator<String> types = new IndexGenerator<>();
	private final IndexGenerator<String> names = new IndexGenerator<>();
	private final BinaryWriter stream;
	private final Map<Object, String> instanceMap;
	private final Map<TypePair, TypeInfo> typeInfoCache = new HashMap<>();
	private final Map<Class, List<Field>> fieldCache = new HashMap<>();
	private final Map<Object, ReferenceTargetProperty> propertyCache =
		new HashMap<>();

	private int currentReferenceID = 1;

	public Serializer (DataOutput stream, Map<Object, String> instanceMap) {
		this.stream = new BinaryWriter(stream);
		this.instanceMap = instanceMap;
	}

	public void serialize (Object obj) throws IOException {
		if (obj == null) {
			throw new IllegalArgumentException(
				"Tried to serialize null object.");
		}

		Property property = createProperty(rootName, obj);
		serializeCore(new PropertyTypeInfo(property, null, null));
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
		return type != null ? type : null;
	}

	private void writeNamesHeader () throws IOException {
		stream.writeNumber(names.items.size());
		for (String name : names.items) {
			stream.writeStringGuarded(name);
		}
	}

	private void serializeCore (PropertyTypeInfo property) {
		if (property == null) {
			throw new IllegalArgumentException(
				"Cannot serialize null property!");
		}

		if (property.property instanceof NullProperty) {
			serializeNullProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.cSharpExpectedPropertyType
					, property.valueType
					, property.cSharpValueType));

			return;
		}

		if (property.expectedPropertyType != null
			&& property.expectedPropertyType == property.valueType) {

			property.valueType = null;
			property.cSharpValueType = null;
		}

		if (property.property instanceof SimpleProperty) {
			serializeSimpleProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.cSharpExpectedPropertyType
					, property.valueType
					, property.cSharpValueType));

			return;
		}

		if (serializeReference(((ReferenceTargetProperty) property.property))) {
			return;
		}

		serializeReferenceTarget(
			new PropertyTypeInfo(
				property.property
				, property.expectedPropertyType
				, property.cSharpExpectedPropertyType
				, property.valueType
				, property.cSharpValueType));
	}

	private void serializeSimpleProperty (PropertyTypeInfo property) {
		writePropertyHeader(
			Elements.SimpleObject
			, property.name
			, property.cSharpValueType);

		writeValue(((SimpleProperty) property.property).value);
	}

	private void writeValue (Object value) {
		commandCache.add(new ValueWriteCommand(value));
	}

	private void writePropertyHeader (
		byte elementID
		, String name
		, String valueType) {

		writeElementID(elementID);
		writeName(name);
		writeType(valueType);
	}

	private void writeType (String cSharpType) {
		int index = types.getIndexOfItem(cSharpType);
		commandCache.add(new NumberWriteCommand(index));
	}

	private void writeName (String name) {
		int index = names.getIndexOfItem(name);
		commandCache.add(new NumberWriteCommand(index));
	}

	private void writeElementID (byte elementID) {
		commandCache.add(new ByteWriteCommand(elementID));
	}

	private void serializeNullProperty (PropertyTypeInfo property) {
		writePropertyHeader(
			Elements.Null
			, property.name
			, property.cSharpValueType);
	}

	private void serializeReferenceTarget (PropertyTypeInfo property) {
		((ReferenceTargetProperty) property.property)
			.reference.isProcessed = true;

		if (property.property instanceof MultiDimensionalArrayProperty) {
			serializeMultiDimensionalArrayProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.cSharpExpectedPropertyType
					, property.valueType
					, property.cSharpValueType));

			return;
		}

		if (property.property instanceof SingleDimensionalArrayProperty) {
			serializeSingleDimensionalArrayProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.cSharpExpectedPropertyType
					, property.valueType
					, property.cSharpValueType));

			return;
		}

		if (property.property instanceof DictionaryProperty) {
			serializeDictionaryProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.cSharpExpectedPropertyType
					, property.valueType
					, property.cSharpValueType));

			return;
		}

		if (property.property instanceof CollectionProperty) {
			serializeCollectionProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.cSharpExpectedPropertyType
					, property.valueType
					, property.cSharpValueType));

			return;
		}

		serializeComplexProperty(
			new PropertyTypeInfo(
				property.property
				, property.expectedPropertyType
				, property.cSharpExpectedPropertyType
				, property.valueType
				, property.cSharpValueType));
	}

	private void serializeMultiDimensionalArrayProperty (
		PropertyTypeInfo property) {

		if (!writePropertyHeaderWithReferenceID(
			Elements.MultiArrayWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.cSharpValueType)) {

			writePropertyHeader(
				Elements.MultiArray
				, property.name
				, property.cSharpValueType);
		}

		MultiDimensionalArrayProperty arrayProperty =
			(MultiDimensionalArrayProperty) property.property;

		writeType(arrayProperty.type.cSharpType);
		writeDimensions(arrayProperty.dimensions);
		writeMultiDimensionalArrayItems(
			arrayProperty.items
			, arrayProperty.elementType);
	}

	private void writeMultiDimensionalArrayItems (
		List<MultiDimensionalArrayItem> items
		, TypePair elementType) {

		writeNumber(items.size());
		for (MultiDimensionalArrayItem item : items) {
			writeMultiDimensionalArrayItem(item, elementType);
		}
	}

	private void writeMultiDimensionalArrayItem (
		MultiDimensionalArrayItem item
		, TypePair elementType) {

		writeNumbers(item.indexes);
		serializeCore(
			new PropertyTypeInfo(
				item.value
				, elementType.type
				, elementType.cSharpType));
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

	private void serializeComplexProperty (PropertyTypeInfo property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.ComplexObjectWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.cSharpValueType)) {

			writePropertyHeader(
				Elements.ComplexObject
				, property.name
				, property.cSharpValueType);
		}

		writeProperties(
			((ComplexProperty) property.property).properties
			, property.property.type);
	}

	@SuppressWarnings("unchecked")
	private void writeProperties (List properties, TypePair type) {
		writeNumber((short) properties.size());
		for (Property property : (List<Property>) properties) {
			try {
				Field field = type.type.getField(property.name);
				serializeCore(
					new PropertyTypeInfo(
						property
						, field.getType()
						, (property.type != null)
							? property.type.cSharpType
							: null));
			} catch (NoSuchFieldException e) {
				System.err.printf(
					"Class '%s' has no field named '%s': %s%n"
					, type.type.getSimpleName()
					, property.name, e.getMessage());
			}
		}
	}

	private boolean writePropertyHeaderWithReferenceID (
		byte elementID
		, Reference reference
		, String name
		, String valueType) {

		if (reference.count < 2) {
			return false;
		}

		writePropertyHeader(elementID, name, valueType);
		writeNumber(reference.id);
		return true;
	}

	private void writeNumber (int n) {
		commandCache.add(new NumberWriteCommand(n));
	}

	private void serializeCollectionProperty (PropertyTypeInfo property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.CollectionWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.cSharpValueType)) {

			writePropertyHeader(
				Elements.Collection
				, property.name
				, property.cSharpValueType);
		}

		CollectionProperty listProperty =
			(CollectionProperty) property.property;

		writeType(listProperty.elementType.cSharpType);
		writeProperties(listProperty.properties, listProperty.type);
		writeItems(listProperty.items, listProperty.elementType);
	}

	@SuppressWarnings("unchecked")
	private void writeItems (List items, TypePair defaultItemType) {
		writeNumber(items.size());
		for (Property item : (List<Property>) items) {
			serializeCore(
				new PropertyTypeInfo(
					item
					, defaultItemType.type
					, defaultItemType.cSharpType));
		}
	}

	private void serializeDictionaryProperty (PropertyTypeInfo property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.DictionaryWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.cSharpValueType)) {

			writePropertyHeader(
				Elements.Dictionary
				, property.name
				, property.cSharpValueType);
		}

		DictionaryProperty dictProperty =
			(DictionaryProperty) property.property;

		writeType(dictProperty.keyType.cSharpType);
		writeType(dictProperty.valueType.cSharpType);
		writeProperties(dictProperty.properties, dictProperty.type);
		writeDictionaryItems(
			dictProperty.items
			, dictProperty.keyType
			, dictProperty.valueType);
	}

	private void writeDictionaryItems (
		List<Entry<Property, Property>> items
		, TypePair keyType
		, TypePair valueType) {

		writeNumber(items.size());
		for (Entry<Property, Property> item : items) {
			writeDictionaryItem(item, keyType, valueType);
		}
	}

	private void writeDictionaryItem (
		Entry<Property, Property> item
		, TypePair keyType
		, TypePair valueType) {

		serializeCore(
			new PropertyTypeInfo(
				item.getKey()
				, keyType.type
				, keyType.cSharpType));

		serializeCore(
			new PropertyTypeInfo(
				item.getValue()
				, valueType.type
				, valueType.cSharpType));
	}

	private void serializeSingleDimensionalArrayProperty (
		PropertyTypeInfo property) {

		if (!writePropertyHeaderWithReferenceID(
			Elements.SingleArrayWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.cSharpValueType)) {

			writePropertyHeader(
				Elements.SingleArray
				, property.name
				, property.cSharpValueType);
		}

		SingleDimensionalArrayProperty arrayProperty =
			(SingleDimensionalArrayProperty) property.property;

		writeType(arrayProperty.elementType.cSharpType);
		writeNumber(arrayProperty.lowerBound);
		writeItems(arrayProperty.items, arrayProperty.elementType);
	}

	private boolean serializeReference (ReferenceTargetProperty property) {
		if (property.reference.count > 1 && property.reference.isProcessed) {
			writePropertyHeader(
				Elements.Reference
				, property.name
				, null);

			writeNumber(property.reference.id);
			return true;
		}

		return false;
	}

	private Property createProperty (String name, Object value) {
		if (value == null) {
			return new NullProperty(name);
		}

		TypeInfo typeInfo = getTypeInfo(value);
		Property property = createSimpleProperty(name, typeInfo, value);
		if (property != null) {
			return property;
		}

		ReferenceTargetProperty referenceTarget =
			createReferenceTargetInstance(name, typeInfo);

		ReferenceTargetProperty cachedTarget = propertyCache.get(value);
		if (cachedTarget != null) {
			cachedTarget.reference.count++;
			referenceTarget.makeFlatCopyFrom(cachedTarget);
			return referenceTarget;
		}

		referenceTarget.reference = new Reference(currentReferenceID);
		currentReferenceID++;
		propertyCache.put(value, referenceTarget);

		if (referenceTarget instanceof SingleDimensionalArrayProperty) {
			fillSingleDimensionalArrayProperty(
				(SingleDimensionalArrayProperty) referenceTarget
				, typeInfo
				, value);

			return referenceTarget;
		}

		if (referenceTarget instanceof MultiDimensionalArrayProperty) {
			fillMultiDimensionalArrayProperty(
				(MultiDimensionalArrayProperty) referenceTarget
				, typeInfo
				, value);

			return referenceTarget;
		}

		if (referenceTarget instanceof DictionaryProperty) {
			fillDictionaryProperty(
				(DictionaryProperty) referenceTarget
				, typeInfo
				, value);

			return referenceTarget;
		}

		if (referenceTarget instanceof CollectionProperty) {
			fillCollectionProperty(
				(CollectionProperty) referenceTarget
				, typeInfo
				, value);

			return referenceTarget;
		}

		if (referenceTarget instanceof ComplexProperty) {
			fillComplexProperty(
				(ComplexProperty) referenceTarget
				, typeInfo
				, value);

			return referenceTarget;
		}

		System.err.printf("Unable to fill property!%n");
		throw new IllegalArgumentException();
	}

	private void fillMultiDimensionalArrayProperty (
		MultiDimensionalArrayProperty property
		, TypeInfo info
		, Object value) {

		if (property == null) {
			return;
		}

		ArrayAnalyser analyser = new ArrayAnalyser(value);

		property.elementType = new TypePair(info.type, null);
		property.dimensions = analyser.getDimensions();

		for (Integer[] coord : analyser.getCoordSet()) {
			Object ar = value;
			Object subValue = null;
			for (int i = 0; i < coord.length; i++) {
				ar = Array.get(ar, coord[i]);
				if (i == coord.length - 1) {
					subValue = ar;
				}
			}

			Property item = createProperty(null, subValue);
			property.items.add(
				new MultiDimensionalArrayItem(unboxInts(coord), item));
		}
	}

	private int[] unboxInts (Integer[] in) {
		int[] out = new int[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = in[i];
		}

		return out;
	}

	private void fillComplexProperty (
		ComplexProperty property
		, TypeInfo info
		, Object value) {

		if (property == null) {
			return;
		}

		parseProperties(property, info, value);
	}

	private void parseProperties (
		ComplexProperty property
		, TypeInfo info
		, Object value) {

		List<Field> fields = getProperties(info);
		for (Field field : fields) {
			try {
				int mod = field.getModifiers();
				if (!Modifier.isPublic(mod)
					|| Modifier.isStatic(mod)
					|| Modifier.isFinal(mod)) {

					continue;
				}

				Object subValue = field.get(value);
				Property subProperty = createProperty(
					field.getName()
					, subValue);

				//noinspection unchecked
				property.properties.add(subProperty);
			} catch (IllegalAccessException e) {
				System.err.printf(
					"Unable to retrieve field '%s' for class '%s': %s%n"
					, field.getName()
					, value.getClass().getSimpleName()
					, e.getMessage());
			}
		}
	}

	private List<Field> getProperties (TypeInfo info) {
		List<Field> fields = fieldCache.get(info.type);
		if (fields != null) {
			return fields;
		}

		fields = Arrays.asList(info.type.getDeclaredFields());
		fieldCache.put(info.type, fields);
		return fields;
	}

	private void fillCollectionProperty (
		CollectionProperty property
		, TypeInfo info
		, Object value) {

		if (property == null) {
			return;
		}

		parseProperties(property, info, value);
		parseCollectionItems(property, info, value);
	}

	private void parseCollectionItems (
		CollectionProperty property
		, TypeInfo info
		, Object value) {

		property.elementType = new TypePair(
			info.elementType
			, info.cSharpElementType);

		try {
			Method iteratorMethod = value.getClass().getMethod("iterator");
			Iterator it = (Iterator) iteratorMethod.invoke(value);
			while (it.hasNext()) {
				Object item = it.next();
				Property itemProperty = createProperty(null, item);
				property.items.add(itemProperty);
			}
		} catch (NoSuchMethodException
			| IllegalAccessException
			| InvocationTargetException e) {

			System.err.printf(
				"Supposed collection type '%s' had no iterator() method: %s%n"
				, value.getClass().getSimpleName()
				, e.getMessage());
		}
	}

	private void fillDictionaryProperty (
		DictionaryProperty property
		, TypeInfo info
		, Object value) {

		if (property == null) {
			return;
		}

		parseProperties(property, info, value);
		parseDictionaryItems(property, info, value);
	}

	@SuppressWarnings("unchecked")
	private void parseDictionaryItems (
		DictionaryProperty property
		, TypeInfo info
		, Object value) {

		property.keyType = new TypePair(info.keyType, info.cSharpKeyType);
		property.valueType = new TypePair(
			info.elementType
			, info.cSharpElementType);

		try {
			Method entrySetMethod = value.getClass().getMethod("entrySet");
			Set<Entry> entries = (Set<Entry>) entrySetMethod.invoke(value);
			for (Entry entry : entries) {
				Property keyProperty = createProperty(null, entry.getKey());
				Property valueProperty = createProperty(null, entry.getValue());
				property.items.add(
					new SimpleImmutableEntry<>(keyProperty, valueProperty));
			}
		} catch (NoSuchMethodException
			| IllegalAccessException
			| InvocationTargetException e) {

			System.err.printf(
				"Supposed dictionary type '%s' had no entrySet() method: %s%n"
				, value.getClass().getSimpleName()
				, e.getMessage());
		}
	}

	private void fillSingleDimensionalArrayProperty (
		SingleDimensionalArrayProperty property
		, TypeInfo info
		, Object value) {

		if (property == null) {
			return;
		}

		property.lowerBound = 0;
		property.elementType = new TypePair(
			info.elementType
			, info.cSharpElementType);

		int length = Array.getLength(value);
		for (int i = 0; i < length; i++) {
			Object item = Array.get(value, i);
			Property itemProperty = createProperty(null, item);

			//noinspection unchecked
			property.items.add(itemProperty);
		}
	}

	private ReferenceTargetProperty createReferenceTargetInstance (
		String name
		, TypeInfo typeInfo) {

		if (typeInfo.isArray) {
			if (typeInfo.dimensionCount < 2) {
				return new SingleDimensionalArrayProperty(
					name
					, new TypePair(typeInfo.type, typeInfo.cSharpType));
			}

			return new MultiDimensionalArrayProperty(
				name
				, new TypePair(typeInfo.type, typeInfo.cSharpType));
		}

		if (typeInfo.isDictionary) {
			return new DictionaryProperty(
				name
				, new TypePair(typeInfo.type, typeInfo.cSharpType));
		}

		if (typeInfo.isCollection) {
			return new CollectionProperty(
				name
				, new TypePair(typeInfo.type, typeInfo.cSharpType));
		}

		// isEnumerable

		return new ComplexProperty(
			name
			, new TypePair(typeInfo.type, typeInfo.cSharpType));
	}

	private Property createSimpleProperty (
		String name
		, TypeInfo typeInfo
		, Object value) {

		if (!typeInfo.isSimple) {
			return null;
		}

		SimpleProperty property = new SimpleProperty(
			name
			, new TypePair(typeInfo.type, typeInfo.cSharpType));

		property.value = value;
		return property;
	}

	private TypeInfo getTypeInfo (Object value) {
		if (value == null) {
			throw new IllegalArgumentException(
				"Cannot get type of null object.");
		}

		Class type = value.getClass();
		String cSharpType = instanceMap.get(value);
		if (cSharpType == null) {
			cSharpType = SharpSerializer.stringMap.get(type);
		}

		if (cSharpType == null && !value.getClass().isArray()) {
			System.err.printf(
				"Unable to determine C# type for class '%s'!%n"
				, type.getSimpleName());
		}

		return getTypeInfo(type, cSharpType);
	}

	private TypeInfo getTypeInfo (Class type, String cSharpType) {
		TypePair pair = new TypePair(type, cSharpType);
		TypeInfo typeInfo = typeInfoCache.get(pair);

		if (typeInfo == null) {
			typeInfo = new TypeInfo();
			typeInfo.cSharpType = cSharpType;
			typeInfo.type = type;
			typeInfo.isSimple = isSimple(type);

			if (type == byte[].class || type == Byte[].class) {
				typeInfo.elementType = byte.class;
				typeInfo.cSharpElementType =
					SharpSerializer.stringMap.get(Byte[].class);

				if (typeInfo.cSharpElementType == null) {
					System.err.printf(
						"Unable to determine C# element type for class '%s'!%n"
						, type.getSimpleName());
				}
			}

			if (!typeInfo.isSimple) {
				typeInfo.isArray = type.isArray();
				if (typeInfo.isArray) {
					if (type.getComponentType() != null) {
						typeInfo.elementType = type.getComponentType();
						typeInfo.cSharpElementType =
							SharpSerializer.stringMap.get(typeInfo.elementType);

						if (typeInfo.cSharpElementType == null) {
							System.err.printf(
								"Unable to determine C# element "
								+ "type for class '%s'!%n"
								, type.getSimpleName());
						}
					}

					typeInfo.dimensionCount =
						type.getName().lastIndexOf("[") + 1;
				} else {
					typeInfo.isEnumerable = isCollection(type);
					typeInfo.isCollection = isCollection(type);
					if (typeInfo.isCollection) {
						typeInfo.isDictionary = isDictionary(type);

						// Can't get compile-time generics information at
						// runtime in Java because of type erasure so we have
						// to rely on our instance map and some string hacking.
						String[] extractedTypes =
							extractTypes(typeInfo.cSharpType);

						String cSharpKeyType = extractedTypes[0];
						String cSharpElementType = extractedTypes[1];

						typeInfo.cSharpKeyType = cSharpKeyType;
						typeInfo.cSharpElementType = cSharpElementType;

						if (cSharpKeyType.contains(",")) {
							typeInfo.keyType = SharpSerializer.typeMap.get(
								cSharpKeyType.split(",")[0]);
						} else {
							typeInfo.keyType =
								SharpSerializer.typeMap.get(cSharpKeyType);
						}

						if (cSharpElementType.contains(",")) {
							typeInfo.elementType = SharpSerializer.typeMap.get(
								cSharpElementType.split(",")[0]);
						} else {
							typeInfo.elementType =
								SharpSerializer.typeMap.get(cSharpElementType);
						}

					}
				}
			}

			typeInfoCache.put(pair, typeInfo);
		}

		return typeInfo;
	}

	private String[] extractTypes (String cSharpType) {
		String[] extracted = new String[]{"System.Object", "System.Object"};
		if (!cSharpType.contains("[[")) {
			System.err.printf(
				"Unable to find any types in supposed C# collection '%s'!%n"
				, cSharpType);

			return extracted;
		}

		Pattern innerClassPattern = Pattern.compile("\\[([^]]+)\\]");
		Matcher innerClassMatcher = innerClassPattern.matcher(cSharpType);

		for (int i = 0; i < 2; i++) {
			if (innerClassMatcher.find()) {
				extracted[i] = innerClassMatcher.group(1);
			}
		}

		return extracted;
	}

	private boolean isDictionary (Class type) {
		return type == HashMap.class || type == Hashtable.class;
	}

	private boolean isCollection (Class type) {
		return type == CSharpCollection.class
			|| type == SortedList.class
			|| type == ArrayList.class
			|| type == HashMap.class
			|| type == Hashtable.class;
	}

	private boolean isSimple (Class type) {
		return type == String.class
			|| type == UUID.class
			|| type == Class.class
			|| type == Integer.class
			|| type == Float.class
			|| type == Double.class
			|| type == Boolean.class
			|| type == UnsignedInteger.class
			|| type == Byte[].class
			|| type == byte[].class
			|| type.isEnum()
			|| type.isPrimitive();
	}
}
