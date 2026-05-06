// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Maps;

/**
 * Test JAXB class to exercise a field annotated with {@link XmlAnyAttribute}
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="any-attr-tester")
public class AnyAttrTester {
    @XmlAttribute
    private String given;

    @XmlAnyAttribute
    private Map<javax.xml.namespace.QName,Object> extraAttributes = Maps.newHashMap();

    public AnyAttrTester() { }

    public String getGiven() { return given; }
    public void setGiven(String given) { this.given = given; }

    public Map<javax.xml.namespace.QName,Object> getExtraAttributes() { return extraAttributes; }
    public void setExtraAttributes(Map<javax.xml.namespace.QName,Object> extraAttributes) {
        this.extraAttributes = extraAttributes;
    }
}
