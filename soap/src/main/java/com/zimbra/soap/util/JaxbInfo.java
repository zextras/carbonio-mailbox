// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;

/**
 * Zimbra SOAP interfaces are more flexible than Jaxb in what is acceptable.
 * In particular, attributes can be represented as elements.
 * This class provides a limited amount of Jaxb information for a class to
 * aid transforming Zimbra acceptable Xml into Jaxb acceptable Xml.
 *
 * @author gren
 *
 * Treatment of annotations:
 *     XmlAccessType      : Supported
 *     XmlAccessorType    : Supported
 *     XmlAnyAttribute    : Ignored
 *     XmlAnyElement      : Ignored
 *     XmlAttribute       : Supported
 *     XmlElement         : Supported
 *     XmlElementRef      : Supported
 *     XmlElementRefs     : Supported
 *     XmlElementWrapper  : Supported
 *     XmlElements        : Supported
 *     XmlEnum            : Ignored
 *     XmlEnumValue       : Ignored
 *     XmlMixed           : Ignored
 *     XmlRootElement     : Supported
 *     XmlSeeAlso         : Not supported - useful for finding subclasses
 *     XmlTransient       : Supported
 *     XmlType            : Useful for propOrder
 *     XmlValue           : Ignored
 */

public final class JaxbInfo {

    private static final Log LOG = ZimbraLog.soap;
    // Various annotation classes use this
    public static final String DEFAULT_MARKER = "##default";

    private Class<?> jaxbClass = null;
    private String stamp = null;
    private String rootElementName = null;
    private String rootElementNamespace = null;
    private XmlType xmlType = null;

    /**
     * names of known attributes that can be associated with the element
     */
    private final List<String> attributeNames = Lists.newArrayList();

    public static final Map<String,JaxbInfo> CACHE = Maps.newHashMap();

    /* excludes attributes represented in super classes */
    private final List<JaxbAttributeInfo> attrInfo = Lists.newArrayList();

    /* Note: There isn't quite a one-to-one correspondence between elements in this array and XML sub-elements
     * as "pseudo" parent entries are added to collect together groups of elements where they are in an
     * choice group (These pseudo parents represent JAXB XmlElements or XmlElementRefs)
     * Also excludes elements represented in super classes
     */
    private final List<JaxbNodeInfo> jaxbElemNodeInfo = Lists.newArrayList();
    private JaxbValueInfo elementValue = null;
    private boolean haveKvpXmlInfo = false;
    private KeyValuePairXmlRepresentationInfo kvpXmlInfo;

    /**
     * @param klass is a JAXB annotated class associated with a particular element
     */
    private JaxbInfo(Class<?> klass) {
        this.jaxbClass = klass;
        if (klass == null) {
            LOG.error("null class provided to JaxbInfo constructor");
            return;
        }
        stamp = "JaxbInfo class=" + this.jaxbClass.getName() + ":";
        gatherInfo();
        synchronized(CACHE) {
            CACHE.put(klass.getName(), this);
        }
    }

    public static JaxbInfo getFromCache(Class<?> klass) {
        if (klass == null || klass.isPrimitive()) {
            return null;
        }
        JaxbInfo jbi = null;
        synchronized(CACHE) {
            jbi = CACHE.get(klass.getName());
        }
        if (jbi == null) {
            jbi = new JaxbInfo(klass);
        }
        return jbi;
    }

    public static void clearCache() {
        synchronized(CACHE) {
            CACHE.clear();
        }
    }

    private JaxbInfo getSuperClassInfo() {
        Class<?> superClass = jaxbClass.getSuperclass();
        if (superClass == null) {
            return null;
        }
        JaxbInfo encJaxbInfo = JaxbInfo.getFromCache(superClass);
        if (encJaxbInfo == null) {
            encJaxbInfo = new JaxbInfo(superClass);
        }
        return encJaxbInfo;
    }

    public Iterable<String> getAttributeNames() {
        List<String> allNames = Lists.newArrayList();
        Iterables.addAll(allNames, this.attributeNames);
        JaxbInfo encClassInfo = getSuperClassInfo();
        if (encClassInfo != null) {
            Iterables.addAll(allNames, encClassInfo.getAttributeNames());
        }
        return allNames;
    }

    public Iterable<JaxbAttributeInfo> getAttributes() {
        List<JaxbAttributeInfo> attrs = Lists.newArrayList();
        Iterables.addAll(attrs, this.attrInfo);
        JaxbInfo encClassInfo = getSuperClassInfo();
        if (encClassInfo != null) {
            Iterables.addAll(attrs, encClassInfo.getAttributes());
        }
        return attrs;
    }

    /**
     * Get XML names of all possible sub-elements (including those described in superclasses)
     */
    public Iterable<String> getElementNames() {
        List<String> elemNames = Lists.newArrayList();
        for (JaxbNodeInfo node : jaxbElemNodeInfo) {
            if (node instanceof JaxbPseudoNodeChoiceInfo) {
                JaxbPseudoNodeChoiceInfo pseudoNode = (JaxbPseudoNodeChoiceInfo) node;
                Iterables.addAll(elemNames, pseudoNode.getElementNames());
            } else {
                elemNames.add(node.getName());
            }
        }
        JaxbInfo encClassInfo = getSuperClassInfo();
        if (encClassInfo != null) {
            Iterables.addAll(elemNames, encClassInfo.getElementNames());
        }
        return elemNames;
    }

    /**
     * Get information relating to sub-elements (including those described in superclasses)
     */
    public Iterable<JaxbNodeInfo> getJaxbNodeInfos() {
        List<JaxbNodeInfo> elems = Lists.newArrayList();
        Iterables.addAll(elems, this.jaxbElemNodeInfo);
        JaxbInfo encClassInfo = getSuperClassInfo();
        if (encClassInfo != null) {
            Iterables.addAll(elems, encClassInfo.getJaxbNodeInfos());
        }
        return elems;
    }

    public boolean hasElement(String name) {
        return (getElemNodeInfo(name) != null);
    }

    public WrappedElementInfo getWrapperInfo(String name) {
        for (JaxbNodeInfo node : jaxbElemNodeInfo) {
            if ((node instanceof WrappedElementInfo) && (name.equals(node.getName()))) {
                return (WrappedElementInfo) node;
            }
        }
        JaxbInfo encClassInfo = getSuperClassInfo();
        if (encClassInfo != null) {
            return encClassInfo.getWrapperInfo(name);
        }
        return null;
    }

    public boolean hasWrapperElement(String name) {
        return (getWrapperInfo(name) != null);
    }

    public Iterable<String> getWrappedSubElementNames(String wrapperName) {
        WrappedElementInfo info = getWrapperInfo(wrapperName);
        if (info != null) {
            return info.getElementNames();
        }
        return null;
    }

    public Class<?> getClassForWrappedElement(String wrapperName, String elementName) {
        WrappedElementInfo info = getWrapperInfo(wrapperName);
        if (info != null) {
            Class<?> wKlass = info.getClassForElementName(elementName);
            if (wKlass == null) {
                LOG.debug("%s No class for wrapper element=%s sub-element=%s", stamp, wrapperName, elementName);
            }
            return wKlass;
        }
        LOG.debug("%s Unknown wrapper element=%s (looking for sub-element=%s)", stamp, wrapperName, elementName);
        return null;
    }

    public boolean hasAttribute(String name) {
        return Iterables.contains(this.getAttributeNames(), name);
    }

    public List<String> getPropOrder() {
        List<String> propOrder = Lists.newArrayList();
        if ( (null == xmlType) || (null == xmlType.propOrder()) )
            return propOrder;
        for (String prop : xmlType.propOrder()) {
            propOrder.add(prop);
        }
        return propOrder;
    }

    private List<List <org.dom4j.QName>> getNameOrderFromElems() {
        List<List <org.dom4j.QName>> nameOrder = Lists.newArrayList();
        for (JaxbNodeInfo node : jaxbElemNodeInfo) {
            if (node instanceof JaxbPseudoNodeChoiceInfo) {
                JaxbPseudoNodeChoiceInfo choiceNode = (JaxbPseudoNodeChoiceInfo) node;
                List <org.dom4j.QName> names = Lists.newArrayList();
                for (JaxbNodeInfo choiceSub : choiceNode.getElements()) {
                    if (choiceSub instanceof JaxbElementInfo) {
                        JaxbElementInfo jei = (JaxbElementInfo) choiceSub;
                        names.add(getQName(jei));
                    }
                }
                nameOrder.add(names);
            } else if (node instanceof JaxbElementInfo) {
                nameOrder.add(Lists.newArrayList(getQName(node)));
            } else if (node instanceof WrappedElementInfo) {
                nameOrder.add(Lists.newArrayList(getQName(node)));
            }
        }
        return nameOrder;
    }

    public List<List <org.dom4j.QName>> getElementNameOrder() {
        List<List <org.dom4j.QName>> nameOrder = Lists.newArrayList();
        JaxbInfo encClassInfo = getSuperClassInfo();
        if (encClassInfo != null) {
            nameOrder.addAll(encClassInfo.getElementNameOrder());
        }
        List<String> propOrder = getPropOrder();
        if (propOrder.isEmpty()) {
            if (!nameOrder.isEmpty()) {
                // Super class specifies order, forcing it to be required.  Add names in order
                nameOrder.addAll(getNameOrderFromElems());
            }
            return nameOrder;
        }
        for (String fieldName : propOrder) {
            List <org.dom4j.QName> names = Lists.newArrayList();
            for (JaxbNodeInfo node : jaxbElemNodeInfo) {
                if (node instanceof JaxbPseudoNodeChoiceInfo) {
                    JaxbPseudoNodeChoiceInfo choiceNode = (JaxbPseudoNodeChoiceInfo) node;
                    if (fieldName.equals(choiceNode.getFieldName())) {
                        for (JaxbNodeInfo choiceSub : choiceNode.getElements()) {
                            if (choiceSub instanceof JaxbElementInfo) {
                                JaxbElementInfo jei = (JaxbElementInfo) choiceSub;
                                names.add(getQName(jei));
                            }
                        }
                    }
                } else if (node instanceof JaxbElementInfo) {
                    JaxbElementInfo elemNode = (JaxbElementInfo) node;
                    if (fieldName.equals(elemNode.getFieldName())) {
                        names.add(getQName(elemNode));
                    }
                } else if (node instanceof WrappedElementInfo) {
                    WrappedElementInfo wNode = (WrappedElementInfo) node;
                    if (fieldName.equals(wNode.getFieldName())) {
                        names.add(getQName(wNode));
                    }
                }
            }
            if (!names.isEmpty()) {
                nameOrder.add(names);
            }
        }
        return nameOrder;
    }

    private org.dom4j.QName getQName(JaxbNodeInfo elemNode) {
        String elemNs = elemNode.getNamespace();
        if (DEFAULT_MARKER.equals(elemNs)) {
            XmlSchema xmlSchemaAnnot = jaxbClass.getPackage().getAnnotation(XmlSchema.class);
            elemNs = XmlNsForm.QUALIFIED.equals(xmlSchemaAnnot.elementFormDefault()) ? xmlSchemaAnnot.namespace() : "";
        }
        org.dom4j.Namespace dom4jNS = new org.dom4j.Namespace("", elemNs);
        return new org.dom4j.QName(elemNode.getName(), dom4jNS);
    }

    public static String getRootElementName(Class<?> kls) {
        String name;
        if (kls == null) {
            return null;
        }
        XmlRootElement re = kls.getAnnotation(XmlRootElement.class);
        if (re != null) {
            name = re.name();
        } else {
            name = kls.getName();
            int lastDot =  name.lastIndexOf('.');
            if (lastDot >= 0) {
                name = name.substring(lastDot + 1);
            }
        }
        return name;
    }

    public String getRootElementName() {
        if (rootElementName == null) {
            rootElementName = getRootElementName(jaxbClass);
        }
        return rootElementName;
    }

    public final class KeyValuePairXmlRepresentationInfo {
        private final String xmlElementName;
        private final String xmlAttributeName;
        public KeyValuePairXmlRepresentationInfo(String elemName, String attrName) {
            xmlElementName = elemName;
            xmlAttributeName = attrName;
        }
        public String getXmlElementName() { return xmlElementName; }
        public String getXmlAttributeName() { return xmlAttributeName; }
    }

    /**
     * If this object has keyvaluepairs for children, this returns information about them, otherwise returns null.
     * Note implicit assumption that there can only be one set of keyvaluepairs - this is because the JSON
     * representation would be unable to differentiate between them.
     */
    public KeyValuePairXmlRepresentationInfo getKeyValuePairElementInfo() {
        if (haveKvpXmlInfo) {
            return kvpXmlInfo;
        }
        String elemName = null;
        String attrName = null;
        Field fields[] = jaxbClass.getDeclaredFields();
        for (Field field: fields) {
            ZimbraKeyValuePairs annot = field.getAnnotation(ZimbraKeyValuePairs.class);
            if (annot == null) {
                continue;
            }
            XmlElement xmlElemAnnot = field.getAnnotation(XmlElement.class);
            if (xmlElemAnnot != null) {
                elemName = xmlElemAnnot.name();
            } else {
                elemName = field.getName();
            }
        }
        if (elemName != null) {
            Method methods[] = jaxbClass.getDeclaredMethods();
            for (Method method : methods) {
                ZimbraKeyValuePairs annot = method.getAnnotation(ZimbraKeyValuePairs.class);
                if (annot == null) {
                    continue;
                }
                XmlElement xmlElemAnnot = method.getAnnotation(XmlElement.class);
                if (xmlElemAnnot != null) {
                    elemName = xmlElemAnnot.name();
                } else {
                    elemName = method.getName();
                }
            }
        }
        if (elemName != null) {
            Class<?> kvpElemClass = this.getClassForElement(elemName);
            if (kvpElemClass != null) {
                JaxbInfo kvpJaxbInfo = JaxbInfo.getFromCache(kvpElemClass);
                if (kvpJaxbInfo != null) {
                    Iterable<String> attribNames = kvpJaxbInfo.getAttributeNames();
                    if (attribNames != null) {
                        for (String attribName : attribNames) { // Should only be one...
                            attrName = attribName;
                            break;
                        }
                    }
                }
            }
        }
        if ((elemName != null) && (attrName != null)) {
            kvpXmlInfo = new KeyValuePairXmlRepresentationInfo(elemName, attrName);
        } else {
            kvpXmlInfo = null;
        }
        haveKvpXmlInfo = true;
        return kvpXmlInfo;
    }

    public static String getRootElementNamespace(Class<?> kls) {
        String namespace = null;
        if (kls == null) {
            return null;
        }
        XmlRootElement re = kls.getAnnotation(XmlRootElement.class);
        if (re != null) {
            namespace = re.namespace();
        }
        return namespace;
    }

    public String getRootElementNameSpace() {
        if (rootElementNamespace == null) {
            rootElementNamespace = getRootElementName(jaxbClass);
        }
        return rootElementNamespace;
    }

    public Class<?> getClassForElement(String name) {
        JaxbNodeInfo node = getElemNodeInfo(name);
        if (node != null) {
            if (node instanceof JaxbElementInfo) {
                JaxbElementInfo elemInfo = (JaxbElementInfo) node;
                return elemInfo.getAtomClass();
            }
            // note: Can't have got a JaxbPseudoNodeChoiceInfo - there is no name associated with them
            //       If we have a Wrapper element, then there isn't a class associated with it
            return null;
        }

        JaxbInfo encClassInfo = getSuperClassInfo();
        if (encClassInfo != null) {
            return encClassInfo.getClassForElement(name);
        }
        return null;
    }

    public JaxbNodeInfo getElemNodeInfo(String name) {
        if (name == null) {
            return null;
        }
        for (JaxbNodeInfo entry : jaxbElemNodeInfo) {
            if (entry instanceof JaxbPseudoNodeChoiceInfo) {
                JaxbPseudoNodeChoiceInfo pseudoNode = (JaxbPseudoNodeChoiceInfo) entry;
                JaxbElementInfo choiceElem = pseudoNode.getElemInfo(name);
                if (choiceElem != null) {
                    return choiceElem;
                }
            } else if (name.equals(entry.getName())) {
                return entry;
            }
        }
        JaxbInfo encClassInfo = getSuperClassInfo();
        if (encClassInfo != null) {
            return encClassInfo.getElemNodeInfo(name);
        }
        return null;
    }

    private JaxbAttributeInfo getAttrInfo(String name) {
        if (name == null) {
            return null;
        }
        for (JaxbAttributeInfo entry : attrInfo) {
            if (name.equals(entry.getName())) {
                return entry;
            }
        }
        return null;
    }

    public Class<?> getClassForAttribute(String name) {
        JaxbAttributeInfo info = getAttrInfo(name);
        if (info == null) {
            JaxbInfo encClassInfo = getSuperClassInfo();
            if (encClassInfo != null) {
                return encClassInfo.getClassForAttribute(name);
            }
            return null;
        }
        return info.getAtomClass();
    }

    private void setXmlAttributeInfo(XmlAttribute attr, String fieldName, Type defaultGenericType) {
        JaxbAttributeInfo info = new JaxbAttributeInfo(this, attr, fieldName, defaultGenericType);
        String name = info.getName();
        Class<?> atomClass = info.getAtomClass();
        if (atomClass != null && !Strings.isNullOrEmpty(name)) {
            attrInfo.add(info);
        }
    }

    private void setXmlElementInfo(XmlElement elem, String fieldName, Type defaultGenericType) {
        JaxbElementInfo info = new JaxbElementInfo(elem, fieldName, defaultGenericType);
        String name = info.getName();
        Class<?> atomClass = info.getAtomClass();
        if (atomClass != null && !Strings.isNullOrEmpty(name)) {
            jaxbElemNodeInfo.add(info);
        }
    }

    private void setXmlElementInfo(XmlElementRef elemRef, String fieldName, Type defaultGenericType) {
        JaxbElementInfo info = new JaxbElementInfo(elemRef, fieldName, defaultGenericType);
        String name = info.getName();
        Class<?> atomClass = info.getAtomClass();
        if (atomClass != null && !Strings.isNullOrEmpty(name)) {
            jaxbElemNodeInfo.add(info);
        }
    }

    private void processFieldRelatedAnnotations(Annotation annots[], String fieldName, Type defaultGenericType) {
        WrappedElementInfo wrappedInfo = null;
        for (Annotation annot : annots) {
            if (annot instanceof XmlElementWrapper) {
                XmlElementWrapper wrapper = (XmlElementWrapper) annot;
                wrappedInfo = new WrappedElementInfo(wrapper, fieldName);
                jaxbElemNodeInfo.add(wrappedInfo);
                break;
            }
        }
        for (Annotation annot : annots) {
            if (annot instanceof XmlValue) {
                elementValue = new JaxbValueInfo((XmlValue)annot, fieldName, defaultGenericType);
            } else if (annot instanceof XmlAttribute) {
                XmlAttribute attr = (XmlAttribute) annot;
                String attrName = attr.name();
                if ((attrName == null) || DEFAULT_MARKER.equals(attrName)) {
                    attrName = fieldName;
                }
                this.setXmlAttributeInfo(attr, fieldName, defaultGenericType);
                this.attributeNames.add(attrName);
            } else if (annot instanceof XmlElement) {
                XmlElement xmlElem = (XmlElement) annot;
                if (wrappedInfo == null) {
                    setXmlElementInfo(xmlElem, fieldName, defaultGenericType);
                } else {
                    wrappedInfo.add(xmlElem, fieldName, defaultGenericType);
                }
            } else if (annot instanceof XmlElementRef) {
                XmlElementRef xmlElemR = (XmlElementRef) annot;
                if (wrappedInfo == null) {
                    setXmlElementInfo(xmlElemR, null, null);
                } else {
                    wrappedInfo.add(xmlElemR, null, null);
                }
            } else if (annot instanceof XmlElements) {
                JaxbPseudoNodeChoiceInfo choiceNode = new JaxbPseudoNodeChoiceInfo(fieldName, defaultGenericType);
                if (wrappedInfo == null) {
                    jaxbElemNodeInfo.add(choiceNode);
                } else {
                    wrappedInfo.add(choiceNode);
                }
                XmlElements xmlElemsAnnot = (XmlElements) annot;
                for (XmlElement xmlE : xmlElemsAnnot.value()) {
                    choiceNode.add(xmlE);
                }
            } else if (annot instanceof XmlElementRefs) {
                JaxbPseudoNodeChoiceInfo choiceNode = new JaxbPseudoNodeChoiceInfo(fieldName, defaultGenericType);
                if (wrappedInfo == null) {
                    jaxbElemNodeInfo.add(choiceNode);
                } else {
                    wrappedInfo.add(choiceNode);
                }
                XmlElementRefs elemRefs = (XmlElementRefs) annot;
                for (XmlElementRef xmlE : elemRefs.value()) {
                    choiceNode.add(xmlE);
                }
            }
        }
    }

    public static boolean isGetter(Method method) {
        String methodName = method.getName();
        if ((!methodName.startsWith("get")) && (!methodName.startsWith("is"))) {
            return false;
        }
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        return (!void.class.equals(method.getReturnType()));
    }

    public static boolean isSetter(Method method) {
        if (!method.getName().startsWith("set")) {
            return false;
        }
        return (method.getParameterTypes().length == 1);
    }

    public static boolean isGetterOrSetter(Method method) {
        return isGetter(method) || isSetter(method);
    }

    private String guessFieldNameFromGetterOrSetter(String methodName) {
        String fieldName = null;
        if ((methodName.startsWith("set")) || (methodName.startsWith("get"))) {
            fieldName = methodName.substring(3,4).toLowerCase() + methodName.substring(4);
        } else if (methodName.startsWith("is")) {
            fieldName = methodName.substring(2,3).toLowerCase() + methodName.substring(3);
        }
        return fieldName;
    }

    /**
     * Returns the most elemental class associated with {@link genericType}
     * May return null
     */
    public static Class<?> classFromType(Type genericType) {
        Class<?> defKlass;
        if (genericType == null) {
            return null;
        }
        if (genericType instanceof Class<?>) {
            defKlass = (Class<?>) genericType;
        } else if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type typeArgs[] = pt.getActualTypeArguments();
            if (typeArgs.length != 1) {
                // Odd - better to ignore this
                return null;
            }
            return classFromType(typeArgs[0]);
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) genericType;
            defKlass = gat.getGenericComponentType().getClass();
        } else if (genericType instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) genericType;
            defKlass = tv.getClass();
        } else {
            LOG.debug("classFromType unknown instance type [" + genericType.toString() + "] - ignoring");
            defKlass = null;
        }
        return defKlass;
    }

    /**
     * Returns the most elemental class associated with {@link genericType}
     * May return null
     */
    public static boolean representsMultipleElements(Type genericType) {
        Class<?> defKlass = null;
        if (genericType == null) {
            return false;
        }
        if (genericType instanceof Class<?>) {
            defKlass = (Class<?>) genericType;
            return Collection.class.isAssignableFrom(defKlass);
        } else if (genericType instanceof ParameterizedType) {
            // e.g. java.util.List<com.zimbra.soap.type.AttributeName>
            ParameterizedType pt = (ParameterizedType) genericType;
            Type rawType = pt.getRawType();
            if (rawType instanceof Class<?>) {
                return Collection.class.isAssignableFrom((Class<?>) rawType);
            }
            return false;
        } else if (genericType instanceof GenericArrayType) {
            return true;
        } else if (genericType instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) genericType;
            return Collection.class.isAssignableFrom(tv.getClass());
        } else {
            return false;
        }
    }

    private void gatherInfo() {
        XmlAccessorType accessorType;
        rootElementName = null;
        XmlAccessType accessType = null;

        XmlRootElement rootE = jaxbClass.getAnnotation(XmlRootElement.class);
        if (rootE != null) {
            rootElementName = rootE.name();
        }
        xmlType = jaxbClass.getAnnotation(XmlType.class);

        accessorType = jaxbClass.getAnnotation(XmlAccessorType.class);
        if (accessorType == null) {
            Package pkg = jaxbClass.getPackage();
            accessorType = pkg.getAnnotation(XmlAccessorType.class);
        }
        if (accessorType != null) {
            accessType = accessorType.value();
        }
        if (accessType == null) {
            // Default value for JAXB
            accessType = XmlAccessType.PUBLIC_MEMBER;
        }

        Field fields[] = jaxbClass.getDeclaredFields();
        for (Field field: fields) {
            XmlTransient xmlTransient = field.getAnnotation(XmlTransient.class);
            if (xmlTransient != null) {
                continue;
            }
            Annotation fAnnots[] = field.getAnnotations();
            if ((fAnnots == null) || (fAnnots.length == 0)) {
                boolean autoFields =
                    (accessType.equals(XmlAccessType.PUBLIC_MEMBER) || accessType.equals(XmlAccessType.FIELD));
                if (!autoFields) {
                    continue;
                }
            }
            processFieldRelatedAnnotations(fAnnots, field.getName(), field.getGenericType());
        }

        Method methods[] = jaxbClass.getDeclaredMethods();
        for (Method method : methods) {
            XmlTransient xmlTransient = method.getAnnotation(XmlTransient.class);
            if (xmlTransient != null) {
                continue;
            }
            if (!isGetterOrSetter(method)) {
                continue;
            }
            Annotation mAnnots[] = method.getAnnotations();
            if ((mAnnots == null) || (mAnnots.length == 0)) {
                boolean autoGettersSetters =
                    (accessType.equals(XmlAccessType.PUBLIC_MEMBER) || accessType.equals(XmlAccessType.PROPERTY));
                if (!autoGettersSetters) {
                    continue;
                }
            }
            processFieldRelatedAnnotations(mAnnots,
                    guessFieldNameFromGetterOrSetter(method.getName()),
                    method.getGenericReturnType());
        }
    }

    protected String getStamp() { return stamp; }
    public Class<?> getJaxbClass() { return jaxbClass; }
    public JaxbValueInfo getElementValue() { return elementValue; }
}
