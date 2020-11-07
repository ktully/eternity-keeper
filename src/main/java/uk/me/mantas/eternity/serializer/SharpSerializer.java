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

// We host our own implementation of SharpSerializer which is used in
// Pillars of Eternity to serialize game objects into saves.

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import uk.me.mantas.eternity.Logger;
import uk.me.mantas.eternity.serializer.properties.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Map.Entry;


public class SharpSerializer {
	private static final Logger logger = Logger.getLogger(SharpSerializer.class);

	public static final Map<String, Class> typeMap = TypeMap.map;
	private Map<Integer, Property> propertyCache = new HashMap<>();

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

	private SerializerFormat format = SerializerFormat.PRESERVE;

	public SharpSerializer (String filePath) throws FileNotFoundException {
		targetFile = new File(filePath);
		if (!targetFile.exists()) {
			throw new FileNotFoundException();
		}
	}

	public SharpSerializer toFormat(SerializerFormat format) {
		this.format = format;

		return this;
	}

	public Optional<Property> deserialize () {
		try {
			FileInputStream baseStream = new FileInputStream(targetFile);
			try (LittleEndianDataInputStream stream =
				new LittleEndianDataInputStream(baseStream)) {

				baseStream.getChannel().position(position);
				Deserializer deserializer = new Deserializer(stream, this);
				Property property = deserializer.deserialize();
				position = baseStream.getChannel().position();

				return Optional.ofNullable(createObject(property));
			}
		} catch (IOException e) {
			logger.error(
				"Error opening target file '%s' for deserializing: %s%n"
				, targetFile
				, e.getMessage());
		}

		return Optional.empty();
	}

	public void serialize (Property property) {
		try {
			FileOutputStream baseStream = new FileOutputStream(
				targetFile
				, true);

			try (LittleEndianDataOutputStream stream =
				new LittleEndianDataOutputStream(baseStream)) {

				baseStream.getChannel()
					.position(baseStream.getChannel().size());

				Serializer serializer =	new Serializer(stream).toFormat(format);
				serializer.serialize(property);
			}
		} catch (IOException e) {
			logger.error(
				"Error opening target file '%s' for serializing: %s%n"
				, targetFile
				, e.getMessage());
		}
	}

	public Optional<Property> followReference (final ReferenceTargetProperty property) {
		if (property.reference == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(propertyCache.get(property.reference.id));
	}

	private Property createObject (Property property) {
		if (property == null) {
			logger.error("Property is null!%n");
			return null;
		}

		if (property instanceof NullProperty) {
			property.obj = null;
			return property;
		}

		if (property.type == null) {
			logger.error(
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
			logger.error("Don't know what to do with this property!%n");
			return null;
		}

		ReferenceTargetProperty referenceTarget =
			(ReferenceTargetProperty) property;

		if (referenceTarget.reference != null
			&& !referenceTarget.reference.isProcessed) {

			return propertyCache.get(referenceTarget.reference.id);
		}

		Property value = createObjectCore(property);
		if (value == null) {
			logger.error("Unimplemented property type!%n");
			return null;
		}

		return value;
	}

	private Property createObjectCore (Object property) {
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

	private Property createObjectFromCollectionProperty (
		CollectionProperty property) {

		Class type = property.type.type;
		Object collection = createInstance(type);

		if (property.reference != null) {
			propertyCache.put(property.reference.id, property);
		}

		fillProperties(collection, property.properties);
		try {
			Method addMethod = collection.getClass().getMethod(
				"add"
				, Object.class);

			for (Property item : property.items) {
				Property value = createObject(item);
				addMethod.invoke(collection, value.obj);
			}
		} catch (NoSuchMethodException e) {
			logger.error(
				"Supposed 'Collection' class '%s' had no add method: %s%n"
				, collection.getClass().getSimpleName()
				, e.getMessage());
		} catch (InvocationTargetException | IllegalAccessException e) {
			logger.error(
				"Unable to call add method on class '%s': %s%n"
				, collection.getClass().getSimpleName()
				, e.getMessage());
		}

		property.obj = collection;
		return property;
	}

	private Property createObjectFromDictionaryProperty (
		DictionaryProperty property) {

		Object dictionary = createInstance(property.type.type);

		if (property.reference != null) {
			propertyCache.put(property.reference.id, property);
		}

		fillProperties(dictionary, property.properties);
		try {
			Method putMethod = dictionary.getClass().getMethod(
				"put"
				, Object.class
				, Object.class);

			for (Entry<Property, Property> item : property.items) {
				Property key = createObject(item.getKey());
				Property value = createObject(item.getValue());
				putMethod.invoke(dictionary, key.obj, value.obj);
			}
		} catch (NoSuchMethodException e) {
			logger.error(
				"Supposed 'Dictionary' class '%s' had no put method: %s%n"
				, dictionary.getClass().getSimpleName()
				, e.getMessage());
		} catch (InvocationTargetException | IllegalAccessException e) {
			logger.error(
				"Unable to call put method on class '%s': %s%n"
				, dictionary.getClass().getSimpleName()
				, e.getMessage());
		}

		property.obj = dictionary;
		return property;
	}

	private Property createObjectFromSingleDimensionalArrayProperty (
		SingleDimensionalArrayProperty property) {

		int itemsCount = property.items.size();
		Object[] array = new Object[itemsCount];

		if (property.reference != null) {
			propertyCache.put(property.reference.id, property);
		}

		for (int index = property.lowerBound; index < property.lowerBound + itemsCount; index++) {
			Property item = (Property) property.items.get(index);
			Property value = createObject(item);
			if (value != null) {
				array[index] = value.obj;
			}
		}

		property.obj = Arrays.copyOf(array, array.length, property.type.type);
		return property;
	}

	private Property createObjectFromComplexProperty (ComplexProperty property) {
		Object obj = createInstance(property.type.type);
		if (obj == null) {
			return null;
		}

		if (property.reference != null) {
			propertyCache.put(property.reference.id, property);
		}

		fillProperties(obj, property.properties);
		property.obj = obj;
		return property;
	}

	private void fillProperties (Object obj, List properties) {
		//noinspection unchecked
		for (Property property : (List<Property>) properties) {
			Field field;

			try {
				field = obj.getClass().getField(property.name);
			} catch (NoSuchFieldException e) {
				logger.error(
					"Class '%s' has no field '%s': %s%n"
					, obj.getClass().getSimpleName()
					, property.name
					, e.getMessage());

				continue;
			}

			Property value = createObject(property);
			if (value == null) {
				continue;
			}

			try {
				field.set(obj, value.obj);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				logger.error(
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
			logger.error(
				"Unable to instantiate object of type '%s': %s%n"
				, type.getSimpleName()
				, e.getMessage());
		}

		return null;
	}

	private Property createObjectFromSimpleProperty (SimpleProperty property) {
		property.obj = property.value;
		return property;
	}
}
