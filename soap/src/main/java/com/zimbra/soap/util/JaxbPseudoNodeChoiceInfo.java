// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

import java.lang.reflect.Type;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;

import com.google.common.collect.Lists;

/**
 * Place holder to store information related to XmlElements or XmlElementRefs annotations
 * i.e. Captures the fact that there is a choice between various elements at this point
 */
public final class JaxbPseudoNodeChoiceInfo
implements JaxbNodeInfo {
    private boolean canHaveMultipleElements;
    private String fieldName;
    private final List <JaxbElementInfo> elems = Lists.newArrayList();

    public JaxbPseudoNodeChoiceInfo(String fieldName, Type defaultGenericType) {
        this.fieldName = fieldName;
        canHaveMultipleElements = JaxbInfo.representsMultipleElements(defaultGenericType);
    }

    public JaxbElementInfo getElemInfo(String name) {
        for (JaxbElementInfo entry : elems) {
            if (name.equals(entry.getName())) {
                return entry;
            }
        }
        return null;
    }

    public boolean hasElement(String name) {
        return (getElemInfo(name) != null);
    }

    public Iterable<String> getElementNames() {
        List<String> elemNames = Lists.newArrayList();
        for (JaxbNodeInfo entry : elems) {
            elemNames.add(entry.getName());
        }
        return elemNames;
    }

    public Class<?> getClassForElementName(String name) {
        JaxbElementInfo info = getElemInfo(name);
        return info == null ? null : info.getAtomClass();
    }

    public Iterable<JaxbElementInfo> getElements() {
        return elems;
    }

    public void add(XmlElement elem) {
        JaxbElementInfo info = new JaxbElementInfo(elem, fieldName, null);
        Class<?> atomClass = info.getAtomClass();
        if (atomClass != null) {
            elems.add(info);
        }
    }

    public void add(XmlElementRef elemRef) {
        JaxbElementInfo info = new JaxbElementInfo(elemRef, fieldName, null);
        Class<?> atomClass = info.getAtomClass();
        if (atomClass != null) {
            elems.add(info);
        }
    }

    @Override
    public String getName() { return null; }
    @Override
    public String getNamespace() { return null; }
    @Override
    public boolean isRequired() { return true; }
    @Override
    public boolean isMultiElement() { return canHaveMultipleElements; }

    public String getFieldName() { return fieldName; }
}
