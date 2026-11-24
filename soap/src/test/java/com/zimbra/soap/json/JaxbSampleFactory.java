// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

final class JaxbSampleFactory {

  private static final int MAX_DEPTH = 3;
  private static final int COLLECTION_SIZE = 2;

  <T> T create(Class<T> type) {
    return type.cast(createBean(type, 0));
  }

  private Object createBean(Class<?> type, int depth) {
    if (depth > MAX_DEPTH || !isInstantiable(type)) {
      return null;
    }
    Object bean = instantiate(type, depth);
    if (bean != null) {
      fillFields(bean, depth);
    }
    return bean;
  }

  private boolean isInstantiable(Class<?> type) {
    return !type.isInterface() && !Modifier.isAbstract(type.getModifiers()) && !type.isEnum();
  }

  private Object instantiate(Class<?> type, int depth) {
    Constructor<?> constructor = simplestConstructor(type);
    if (constructor == null) {
      return null;
    }
    Object[] arguments =
        Arrays.stream(constructor.getParameterTypes())
            .map(parameterType -> valueFor(parameterType, parameterType, "arg", depth + 1))
            .toArray();
    try {
      constructor.setAccessible(true);
      return constructor.newInstance(arguments);
    } catch (ReflectiveOperationException | RuntimeException e) {
      return null;
    }
  }

  private Constructor<?> simplestConstructor(Class<?> type) {
    return Arrays.stream(type.getDeclaredConstructors())
        .filter(constructor -> !constructor.isSynthetic())
        .min(Comparator.comparingInt(Constructor::getParameterCount))
        .orElse(null);
  }

  private void fillFields(Object bean, int depth) {
    for (Field field : serializableFields(bean.getClass())) {
      fillField(bean, field, depth);
    }
  }

  private List<Field> serializableFields(Class<?> type) {
    List<Field> fields = new ArrayList<>();
    for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
      Arrays.stream(current.getDeclaredFields()).filter(this::isSerializable).forEach(fields::add);
    }
    return fields;
  }

  private boolean isSerializable(Field field) {
    int modifiers = field.getModifiers();
    return !Modifier.isStatic(modifiers)
        && !Modifier.isTransient(modifiers)
        && !field.isSynthetic()
        && !field.isAnnotationPresent(XmlTransient.class);
  }

  private void fillField(Object bean, Field field, int depth) {
    try {
      field.setAccessible(true);
      Object current = field.get(bean);
      if (current instanceof Collection<?> collection) {
        addItems(asObjectCollection(collection), field, depth);
      } else if (current instanceof Map<?, ?> map) {
        addEntries(asObjectMap(map), field, depth);
      } else if (current == null) {
        Class<?> valueType = choiceTypes(field).stream().findFirst().orElse(field.getType());
        Object value = valueFor(valueType, field.getGenericType(), field.getName(), depth + 1);
        if (value != null) {
          field.set(bean, value);
        }
      }
    } catch (ReflectiveOperationException | RuntimeException e) {
      return;
    }
  }

  private void addItems(Collection<Object> collection, Field field, int depth) {
    collection.addAll(itemsFor(field, elementType(field.getGenericType(), 0), depth));
  }

  private void addEntries(Map<Object, Object> map, Field field, int depth) {
    map.putAll(entriesFor(field.getGenericType(), field.getName(), depth));
  }

  private List<Object> itemsFor(Field field, Type elementType, int depth) {
    List<Class<?>> choices = choiceTypes(field);
    List<Object> items = new ArrayList<>();
    if (!choices.isEmpty()) {
      choices.forEach(choice -> Optional.ofNullable(valueFor(choice, choice, field.getName(), depth + 1)).ifPresent(items::add));
      return items;
    }
    Class<?> rawElementType = rawType(elementType);
    for (int index = 0; index < COLLECTION_SIZE; index++) {
      Optional.ofNullable(valueFor(rawElementType, elementType, field.getName() + index, depth + 1)).ifPresent(items::add);
    }
    return items;
  }

  private Map<Object, Object> entriesFor(Type mapType, String name, int depth) {
    Type valueType = elementType(mapType, 1);
    Map<Object, Object> entries = new LinkedHashMap<>();
    for (int index = 0; index < COLLECTION_SIZE; index++) {
      Object value = valueFor(rawType(valueType), valueType, name + index, depth + 1);
      if (value != null) {
        entries.put(name + "Key" + index, value);
      }
    }
    return entries;
  }

  private List<Class<?>> choiceTypes(Field field) {
    List<Class<?>> types = new ArrayList<>();
    XmlElements elements = field.getAnnotation(XmlElements.class);
    if (elements != null) {
      Arrays.stream(elements.value()).map(XmlElement::type).filter(this::isDeclared).forEach(types::add);
    }
    XmlElementRefs refs = field.getAnnotation(XmlElementRefs.class);
    if (refs != null) {
      Arrays.stream(refs.value()).map(XmlElementRef::type).filter(this::isDeclared).forEach(types::add);
    }
    return types;
  }

  private boolean isDeclared(Class<?> type) {
    return type != XmlElement.DEFAULT.class && type != XmlElementRef.DEFAULT.class;
  }

  private Object valueFor(Class<?> type, Type genericType, String name, int depth) {
    if (type == String.class) {
      return name;
    }
    if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type == Boolean.class || type == Character.class) {
      return primitiveValue(type);
    }
    if (type.isEnum()) {
      return enumValue(type);
    }
    if (type == byte[].class) {
      return name.getBytes();
    }
    if (type == Date.class) {
      return new Date(0L);
    }
    if (type == org.w3c.dom.Element.class) {
      return domElement(name);
    }
    if (List.class.isAssignableFrom(type) || type == Collection.class) {
      return new ArrayList<>(collectionValues(genericType, name, depth));
    }
    if (Set.class.isAssignableFrom(type)) {
      return new LinkedHashSet<>(collectionValues(genericType, name, depth));
    }
    if (Map.class.isAssignableFrom(type)) {
      return new LinkedHashMap<>(entriesFor(genericType, name, depth));
    }
    if (type.getName().startsWith("com.zimbra.")) {
      return createBean(type, depth);
    }
    return null;
  }

  private List<Object> collectionValues(Type collectionType, String name, int depth) {
    Type elementType = elementType(collectionType, 0);
    List<Object> values = new ArrayList<>();
    for (int index = 0; index < COLLECTION_SIZE; index++) {
      Optional.ofNullable(valueFor(rawType(elementType), elementType, name + index, depth)).ifPresent(values::add);
    }
    return values;
  }

  private Object primitiveValue(Class<?> type) {
    if (type == boolean.class || type == Boolean.class) {
      return Boolean.TRUE;
    }
    if (type == int.class || type == Integer.class) {
      return 7;
    }
    if (type == long.class || type == Long.class) {
      return 1700000000000L;
    }
    if (type == short.class || type == Short.class) {
      return (short) 3;
    }
    if (type == byte.class || type == Byte.class) {
      return (byte) 2;
    }
    if (type == double.class || type == Double.class) {
      return 1.5d;
    }
    if (type == float.class || type == Float.class) {
      return 2.5f;
    }
    if (type == char.class || type == Character.class) {
      return 'c';
    }
    return null;
  }

  private Object enumValue(Class<?> type) {
    Object[] constants = type.getEnumConstants();
    if (constants.length == 0) {
      return null;
    }
    return Arrays.stream(constants).filter(constant -> hasDistinctXmlName(type, constant)).findFirst().orElse(constants[0]);
  }

  private boolean hasDistinctXmlName(Class<?> type, Object constant) {
    String name = ((Enum<?>) constant).name();
    try {
      XmlEnumValue xmlName = type.getField(name).getAnnotation(XmlEnumValue.class);
      return xmlName != null && !xmlName.value().equals(name);
    } catch (NoSuchFieldException e) {
      return false;
    }
  }

  private org.w3c.dom.Element domElement(String name) {
    try {
      org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      org.w3c.dom.Element element = document.createElementNS("urn:zimbraTest", "any");
      element.setAttribute("name", name);
      element.setTextContent(name);
      return element;
    } catch (ParserConfigurationException e) {
      return null;
    }
  }

  private Type elementType(Type containerType, int argumentIndex) {
    if (containerType instanceof ParameterizedType parameterized
        && parameterized.getActualTypeArguments().length > argumentIndex) {
      return parameterized.getActualTypeArguments()[argumentIndex];
    }
    return String.class;
  }

  private Class<?> rawType(Type type) {
    if (type instanceof Class<?> raw) {
      return raw;
    }
    if (type instanceof ParameterizedType parameterized) {
      return (Class<?>) parameterized.getRawType();
    }
    if (type instanceof WildcardType wildcard && wildcard.getUpperBounds().length > 0) {
      return rawType(wildcard.getUpperBounds()[0]);
    }
    return Object.class;
  }

  @SuppressWarnings("unchecked")
  private Collection<Object> asObjectCollection(Collection<?> collection) {
    return (Collection<Object>) collection;
  }

  @SuppressWarnings("unchecked")
  private Map<Object, Object> asObjectMap(Map<?, ?> map) {
    return (Map<Object, Object>) map;
  }
}
