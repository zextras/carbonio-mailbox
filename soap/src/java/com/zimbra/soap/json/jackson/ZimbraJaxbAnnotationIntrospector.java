// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json.jackson;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * This class exists because of:
 *     https://github.com/FasterXML/jackson-modules-base/issues/47
 * Perhaps when that is fixed, this can go away.
 */
public class ZimbraJaxbAnnotationIntrospector extends JaxbAnnotationIntrospector {
    private static final long serialVersionUID = 3903948048784286612L;

    public ZimbraJaxbAnnotationIntrospector(TypeFactory typeFactory) {
        super(typeFactory);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a)
    {
        PropertyName propertyName = super.findNameForSerialization(a);
        if (propertyName == null) {
            return propertyName;
        }
        PropertyName pn = null;
        if (a instanceof AnnotatedMethod) {
            AnnotatedMethod am = (AnnotatedMethod) a;
            pn = xmlElementWrapperName(am);
        }
        if (a instanceof AnnotatedField) {
            AnnotatedField af = (AnnotatedField) a;
            pn = xmlElementWrapperName(af);
        }
        return pn != null ? pn : propertyName;
    }

    private PropertyName xmlElementWrapperName(Annotated ae) {
        XmlElement element = ae.getAnnotation(XmlElement.class);
        if (element == null) {
            return null;
        }
        XmlElementWrapper wrapper = ae.getAnnotation(XmlElementWrapper.class);
        return (wrapper == null) ? null : combineNames(wrapper.name(), wrapper.namespace());
    }

    private static PropertyName combineNames(String localName, String namespace)
    {
        if (MARKER_FOR_DEFAULT.equals(localName)) {
            return null;
        }
        if (MARKER_FOR_DEFAULT.equals(namespace)) {
            return new PropertyName(localName);
        }
        return new PropertyName(localName, namespace);
    }
}
