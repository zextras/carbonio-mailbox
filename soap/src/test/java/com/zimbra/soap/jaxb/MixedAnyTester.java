// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

import com.google.common.collect.Lists;

/**
 * Test JAXB class to exercise a field annotated with {@link XmlAnyElement}, {@link XmlMixed} and
 * {@link XmlElementRefs} and {@link XmlElementRef}
 *
 * {@link XmlAnyElement} means that some of the objects in the list can be {@link Element}
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="mixed-tester")
public class MixedAnyTester {
    @XmlElementRefs({
        @XmlElementRef(type=StringAttribIntValue.class) /* note: tests out case where name isn't specified here */
    })
    @XmlAnyElement
    @XmlMixed
    private List<Object> elems = Lists.newArrayList();

    public MixedAnyTester() { }

    public List<Object> getElems() { return elems; }
    public void setElems(List<Object> elems) { this.elems = elems; }
}
