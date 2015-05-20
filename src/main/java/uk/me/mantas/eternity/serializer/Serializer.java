package uk.me.mantas.eternity.serializer;

import com.google.common.primitives.UnsignedInteger;
import uk.me.mantas.eternity.serializer.properties.*;
import uk.me.mantas.eternity.serializer.write.ByteWriteCommand;
import uk.me.mantas.eternity.serializer.write.NumberWriteCommand;
import uk.me.mantas.eternity.serializer.write.ValueWriteCommand;
import uk.me.mantas.eternity.serializer.write.WriteCommand;

import java.io.DataOutput;
import java.lang.reflect.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;

import static uk.me.mantas.eternity.serializer.SharpSerializer.Elements;

public class Serializer {
	private static final String rootName = "Root";
	private final List<WriteCommand> commandCache = new ArrayList<>();
	private final IndexGenerator<Class> types = new IndexGenerator<>();
	private final IndexGenerator<String> names = new IndexGenerator<>();
	private final BinaryWriter stream;
	private final Map<Object, String> instanceMap;
	private final Map<Class, TypeInfo> typeInfoCache = new HashMap<>();
	private final Map<Class, List<Field>> fieldCache = new HashMap<>();
	private final Map<Object, ReferenceTargetProperty> propertyCache =
		new HashMap<>();

	private int currentReferenceID = 1;

	public Serializer (DataOutput stream, Map<Object, String> instanceMap) {
		this.stream = new BinaryWriter(stream);
		this.instanceMap = instanceMap;
	}

	public void serialize (Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException(
				"Tried to serialize null object.");
		}

		Property property = createProperty(rootName, obj);
		serializeCore(new PropertyTypeInfo(property, null));
		System.out.printf("Done!%n");
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
					, property.valueType));

			return;
		}

		if (property.expectedPropertyType != null
			&& property.expectedPropertyType == property.valueType) {

			property.valueType = null;
		}

		if (property.property instanceof SimpleProperty) {
			serializeSimpleProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.valueType));

			return;
		}

		if (!serializeReference((ReferenceTargetProperty) property.property)) {
			serializeReferenceTarget(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.valueType));

			return;
		}

		throw new IllegalArgumentException("Unknown property!");
	}

	private void serializeSimpleProperty (PropertyTypeInfo property) {
		writePropertyHeader(
			Elements.SimpleObject
			, property.name
			, property.valueType);

		writeValue(((SimpleProperty) property.property).value);
	}

	private void writeValue (Object value) {
		commandCache.add(new ValueWriteCommand(value));
	}

	private void writePropertyHeader (
		byte elementID
		, String name
		, Class valueType) {

		writeElementID(elementID);
		writeName(name);
		writeType(valueType);
	}

	private void writeType (Class type) {
		int index = types.getIndexOfItem(type);
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
		writePropertyHeader(Elements.Null, property.name, property.valueType);
	}

	private void serializeReferenceTarget (PropertyTypeInfo property) {
		// MultiDimensionalArrayProperty

		((ReferenceTargetProperty) property.property)
			.reference.isProcessed = true;

		if (property.property instanceof SingleDimensionalArrayProperty) {
			serializeSingleDimensionalArrayProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.valueType));

			return;
		}

		if (property.property instanceof DictionaryProperty) {
			serializeDictionaryProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.valueType));

			return;
		}

		if (property.property instanceof CollectionProperty) {
			serializeCollectionProperty(
				new PropertyTypeInfo(
					property.property
					, property.expectedPropertyType
					, property.valueType));

			return;
		}

		serializeComplexProperty(
			new PropertyTypeInfo(
				property.property
				, property.expectedPropertyType
				, property.valueType));
	}

	private void serializeComplexProperty (PropertyTypeInfo property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.ComplexObjectWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.valueType)) {

			writePropertyHeader(
				Elements.ComplexObject
				, property.name
				, property.valueType);
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
				serializeCore(new PropertyTypeInfo(property, field.getType()));
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
		, Class valueType) {

		if (reference.count < 2) {
			return false;
		}

		writePropertyHeader(elementID, name, valueType);
		writeNumber(reference.id);
	}

	private void writeNumber (int n) {
		commandCache.add(new NumberWriteCommand(n));
	}

	private void serializeCollectionProperty (PropertyTypeInfo property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.CollectionWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.valueType)) {

			writePropertyHeader(
				Elements.Collection
				, property.name
				, property.valueType);
		}

		CollectionProperty listProperty =
			(CollectionProperty) property.property;

		writeType(listProperty.elementType);
		writeProperties(listProperty.properties, listProperty.type);
		writeItems(listProperty.items, listProperty.elementType);
	}

	private void serializeDictionaryProperty (PropertyTypeInfo property) {
		if (!writePropertyHeaderWithReferenceID(
			Elements.DictionaryWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.valueType)) {

			writePropertyHeader(
				Elements.Dictionary
				, property.name
				, property.valueType);
		}

		DictionaryProperty dictProperty =
			(DictionaryProperty) property.property;

		writeType(dictProperty.keyType);
		writeType(dictProperty.valueType);
		writeProperties(dictProperty.properties, dictProperty.type);
		writeDictionaryItems(
			dictProperty.items
			, dictProperty.keyType
			, dictProperty.valueType);
	}

	private void serializeSingleDimensionalArrayProperty (
		PropertyTypeInfo property) {

		if (!writePropertyHeaderWithReferenceID(
			Elements.SingleArrayWithID
			, ((ReferenceTargetProperty) property.property).reference
			, property.name
			, property.valueType)) {

			writePropertyHeader(
				Elements.SingleArray
				, property.name
				, property.valueType);
		}

		SingleDimensionalArrayProperty arrayProperty =
			(SingleDimensionalArrayProperty) property.property;

		writeType(arrayProperty.elementType);
		writeNumber(arrayProperty.lowerBound);
		writeItems(arrayProperty.items, arrayProperty.elementType);
	}

	private boolean serializeReference (ReferenceTargetProperty property) {
		if (property.reference.count > 1) {
			if (property.reference.isProcessed) {
				writePropertyHeader(
					Elements.Reference
					, property.name
					, null);

				writeNumber(property.reference.id);
				return true;
			}
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

		// MultiDimensionalArrayProperty

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

		property.elementType = new TypePair(info.elementType, null);

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

		property.keyType = new TypePair(info.keyType, null);
		property.valueType = new TypePair(info.elementType, null);

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

		property.elementType = new TypePair(info.elementType, null);
		property.lowerBound = 0;

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
					, new TypePair(typeInfo.type, null));
			}

			System.err.printf("Multi-dimensional arrays unimplemented!%n");
			throw new IllegalArgumentException();
		}

		if (typeInfo.isDictionary) {
			return new DictionaryProperty(
				name
				, new TypePair(typeInfo.type, null));
		}

		if (typeInfo.isCollection) {
			return new CollectionProperty(
				name
				, new TypePair(typeInfo.type, null));
		}

		// isEnumerable

		return new ComplexProperty(name, new TypePair(typeInfo.type, null));
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
			, new TypePair(typeInfo.type, null));

		property.value = value;
		return property;
	}

	private TypeInfo getTypeInfo (Object value) {
		if (value == null) {
			throw new IllegalArgumentException(
				"Cannot get type of null object.");
		}

		Class type = value.getClass();
		return getTypeInfo(type);
	}

	private TypeInfo getTypeInfo (Class type) {
		TypeInfo typeInfo = typeInfoCache.get(type);

		if (typeInfo == null) {
			typeInfo = new TypeInfo();
			typeInfo.type = type;
			typeInfo.isSimple = isSimple(type);

			if (type == byte[].class || type == Byte[].class) {
				typeInfo.elementType = byte.class;
			}

			if (!typeInfo.isSimple) {
				typeInfo.isArray = type.isArray();
				if (typeInfo.isArray) {
					if (type.getComponentType() != null) {
						typeInfo.elementType = type.getComponentType();
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
						// to rely on our instance map and some string hacking
						// further down the line.
						typeInfo.keyType = Object.class;
						typeInfo.elementType = Object.class;
					}
				}
			}

			typeInfoCache.put(type, typeInfo);
		}

		return typeInfo;
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
