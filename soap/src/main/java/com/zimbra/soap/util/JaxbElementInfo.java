// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

import java.lang.reflect.Type;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;

public class JaxbElementInfo
implements JaxbNodeInfo {
    private static final Log LOG = ZimbraLog.soap;
    private String name;
    private String namespace;
    private boolean required;
    private boolean canHaveMultipleElements;
    private String fieldName;
    private String stamp;
    private Class<?> atomClass;

    public JaxbElementInfo(XmlElement annot, String fieldName, Type defaultGenericType) {
        this.name = annot.name();
        this.namespace = annot.namespace();
        this.required = annot.required();
        this.fieldName = fieldName;
        if ((name == null) || JaxbInfo.DEFAULT_MARKER.equals(name)) {
            name = fieldName;
        }
        if (name == null) {
            throw new RuntimeException(
                String.format("Ignoring element with annotation %s unable to determine name", annot.toString()));
        }
        stamp = String.format("[element=%s]", name);
        atomClass = annot.type();
        if (atomClass == XmlElement.DEFAULT.class)
            atomClass = JaxbInfo.classFromType(defaultGenericType);
        if (atomClass == null) {
            LOG.debug("%s Unable to determine class for element with annotation '%s'", stamp, annot);
        }
        canHaveMultipleElements = JaxbInfo.representsMultipleElements(defaultGenericType);
    }

    /**
     * Note that unless the collection item type (for collection property) or property type (for single valued
     * property) is JAXBElement any name and namespace specified in {@link XmlElementRef} are ignored - the actual
     * values are taken from the {@link XmlRootElement} annotation on the collection item type or property type.
     */
    public JaxbElementInfo(XmlElementRef annot, String fieldName, Type defaultGenericType) {
        atomClass = annot.type();
        if (atomClass == XmlElementRef.DEFAULT.class)
            atomClass = JaxbInfo.classFromType(defaultGenericType);
        if (atomClass == null) {
            LOG.debug("%s Unable to determine name for element with annotation '%s'", stamp, annot);
        }
        name = JaxbInfo.getRootElementName(atomClass);
        if (name == null) {
            name = annot.name();
            namespace = annot.namespace();
        } else {
            namespace = JaxbInfo.getRootElementNamespace(atomClass);
        }
        this.fieldName = fieldName;
        required = true; // TODO annotation.required() does not exist!
        if ((name == null) || JaxbInfo.DEFAULT_MARKER.equals(name)) {
            name = fieldName;
        }
        if (name == null) {
            throw new RuntimeException(
                String.format("Ignoring element with annotation %s unable to determine name", annot.toString()));
        }
        stamp = String.format("[element=%s]", name);
        canHaveMultipleElements = JaxbInfo.representsMultipleElements(defaultGenericType);
    }

    @Override
    public String getName() { return name; }
    @Override
    public String getNamespace() { return namespace; }
    @Override
    public boolean isRequired() { return required; }
    @Override
    public boolean isMultiElement() { return canHaveMultipleElements; }

    public String getFieldName() { return fieldName; }
 
    /**
     * @return the class that would represent one XML Element (i.e. if associated with a List, want
     * the class associated with an element of the array - not List)
     */
    public Class<?> getAtomClass() { return atomClass; }
}
