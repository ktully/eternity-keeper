package uk.me.mantas.eternity.serializer;

// We host our own implementation of SharpSerializer which is used in
// Pillars of Eternity to serialize game objects into saves.

import com.google.common.io.LittleEndianDataInputStream;
import uk.me.mantas.eternity.serializer.properties.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Map.Entry;


public class SharpSerializer {
	public static final Map<String, Class> typeMap = TypeMap.map;
	private Map<Integer, Object> objectCache = new HashMap<>();

	public static class Elements {
		public static final byte Collection = 1;
		public static final byte ComplexObject = 2;
		public static final byte Dictionary = 3;
		public static final byte MultiArray = 4;
		public static final byte Null = 5;
		public static final byte SimpleObject = 6;
		public static final byte SingleArray = 7;
		public static final byte ComplexObjectWithID = 8;
		public static final byte Reference = 9;
		public static final byte CollectionWithID = 10;
		public static final byte DictionaryWithID = 11;
		public static final byte SingleArrayWithID = 12;
		public static final byte MultiArrayWithID = 13;

		public static boolean isElementWithID (byte elementID) {
			return elementID == ComplexObjectWithID
				|| elementID == CollectionWithID
				|| elementID == DictionaryWithID
				|| elementID == SingleArrayWithID
				|| elementID == MultiArrayWithID;
		}
	}

	private final File targetFile;
	private long position = 0;

	public SharpSerializer (String filePath) throws FileNotFoundException {
		targetFile = new File(filePath);
		if (!targetFile.exists()) {
			throw new FileNotFoundException();
		}
	}

	public Optional<Object> deserialize () {
		try {
			FileInputStream baseStream = new FileInputStream(targetFile);
			try (LittleEndianDataInputStream stream =
				new LittleEndianDataInputStream(baseStream)) {

				baseStream.getChannel().position(position);
				Deserializer deserializer = new Deserializer(stream);
				Property property = deserializer.deserialize();
				position = baseStream.getChannel().position();

				return Optional.ofNullable(createObject(property));
			}
		} catch (IOException e) {
			System.err.printf(
				"Error opening target file '%s' for deserializing: %s%n"
				, targetFile
				, e.getMessage());
		}

		return Optional.empty();
	}

	private Object createObject (Property property) {
		if (property == null) {
			System.err.printf("Property is null!%n");
			return null;
		}

		if (property instanceof NullProperty) {
			return null;
		}

		if (property.type == null) {
			System.err.printf(
				"Tried to create an object from a property with no type!%n");

			return null;
		}

		if (property instanceof SimpleProperty) {
			if (((SimpleProperty) property).type == null) {
				throw new IllegalArgumentException();
			}

			return createObjectFromSimpleProperty((SimpleProperty) property);
		}

		if (!(property instanceof ReferenceTargetProperty)) {
			System.err.printf("Don't know what to do with this property!%n");
			return null;
		}

		ReferenceTargetProperty referenceTarget =
			(ReferenceTargetProperty) property;

		if (referenceTarget.reference != null
			&& !referenceTarget.reference.isProcessed) {

			return objectCache.get(referenceTarget.reference.id);
		}

		Object value = createObjectCore(property);
		if (value == null) {
			System.err.printf("Unimplemented property type!%n");
			return null;
		}

		return value;
	}

	private Object createObjectCore (Object property) {
		// MultiDimensionalArray

		if (property instanceof SingleDimensionalArrayProperty) {
			return createObjectFromSingleDimensionalArrayProperty(
				(SingleDimensionalArrayProperty) property);
		}

		if (property instanceof DictionaryProperty) {
			return createObjectFromDictionaryProperty(
				(DictionaryProperty) property);
		}

		if (property instanceof CollectionProperty) {
			return createObjectFromCollectionProperty(
				(CollectionProperty) property);
		}

		if (property instanceof ComplexProperty) {
			return createObjectFromComplexProperty((ComplexProperty) property);
		}

		return null;
	}

	private Object createObjectFromCollectionProperty (
		CollectionProperty property) {

		Class type = property.type;
		Object collection = createInstance(type);

		if (property.reference != null) {
			objectCache.put(property.reference.id, collection);
		}

		fillProperties(collection, property.properties);
		try {
			Method addMethod = collection.getClass().getMethod(
				"add"
				, Object.class);

			for (Property item : property.items) {
				Object value = createObject(item);
				addMethod.invoke(collection, value);
			}
		} catch (NoSuchMethodException e) {
			System.err.printf(
				"Supposed 'Collection' class '%s' had no add method: %s%n"
				, collection.getClass().getSimpleName()
				, e.getMessage());
		} catch (InvocationTargetException | IllegalAccessException e) {
			System.err.printf(
				"Unable to call add method on class '%s': %s%n"
				, collection.getClass().getSimpleName()
				, e.getMessage());
		}

		return collection;
	}

	private Object createObjectFromDictionaryProperty (
		DictionaryProperty property) {

		Object dictionary = createInstance(property.type);

		if (property.reference != null) {
			objectCache.put(property.reference.id, dictionary);
		}

		fillProperties(dictionary, property.properties);
		try {
			Method putMethod = dictionary.getClass().getMethod(
				"put"
				, Object.class
				, Object.class);

			for (Entry<Property, Property> item : property.items) {
				Object key = createObject(item.getKey());
				Object value = createObject(item.getValue());
				putMethod.invoke(dictionary, key, value);
			}
		} catch (NoSuchMethodException e) {
			System.err.printf(
				"Supposed 'Dictionary' class '%s' had no put method: %s%n"
				, dictionary.getClass().getSimpleName()
				, e.getMessage());
		} catch (InvocationTargetException | IllegalAccessException e) {
			System.err.printf(
				"Unable to call put method on class '%s': %s%n"
				, dictionary.getClass().getSimpleName()
				, e.getMessage());
		}

		return dictionary;
	}

	private Object createObjectFromSingleDimensionalArrayProperty (
		SingleDimensionalArrayProperty property) {

		int itemsCount = property.items.size();
		Object[] array = new Object[itemsCount];

		if (property.reference != null) {
			objectCache.put(property.reference.id, array);
		}

		for (int index = property.lowerBound
			; index < property.lowerBound + itemsCount
			; index++) {

			Property item = (Property) property.items.get(index);
			Object value = createObject(item);
			if (value != null) {
				array[index] = value;
			}
		}

		return Arrays.copyOf(array, array.length, property.type);
	}

	private Object createObjectFromComplexProperty (ComplexProperty property) {
		Object obj = createInstance(property.type);
		if (obj == null) {
			return null;
		}

		if (property.reference != null) {
			objectCache.put(property.reference.id, obj);
		}

		fillProperties(obj, property.properties);
		return obj;
	}

	private void fillProperties (Object obj, List properties) {
		//noinspection unchecked
		for (Property property : (List<Property>) properties) {
			Field field;

			try {
				field = obj.getClass().getField(property.name);
			} catch (NoSuchFieldException e) {
				System.err.printf(
					"Class '%s' has no field '%s': %s%n"
					, obj.getClass().getSimpleName()
					, property.name
					, e.getMessage());

				continue;
			}

			Object value = createObject(property);
			if (value == null) {
				continue;
			}

			try {
				field.set(obj, value);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				System.err.printf(
					"Unable to set field '%s' of class '%s': %s%n"
					, property.name
					, obj.getClass().getSimpleName()
					, e.getMessage());
			}
		}
	}

	private Object createInstance (Class type) {
		if (type == null) {
			return null;
		}

		try {
			return type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			System.err.printf(
				"Unable to instantiate object of type '%s': %s%n"
				, type.getSimpleName()
				, e.getMessage());
		}

		return null;
	}

	private Object createObjectFromSimpleProperty (SimpleProperty property) {
		return property.value;
	}
}
