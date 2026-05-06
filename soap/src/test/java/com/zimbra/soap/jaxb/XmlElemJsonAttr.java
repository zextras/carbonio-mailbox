// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;

/** Test JAXB class to demonstrate affect of {@link ZimbraJsonAttribute} annotation */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="XmlElemJsonAttr")
public class XmlElemJsonAttr {
    @ZimbraJsonAttribute
    @XmlElement(name="xml-elem-json-attr", required=false)
    private String xmlElemJsonAttr;

    @XmlElement(name="classic-elem", required=false)
    private String defaultElem;

    public XmlElemJsonAttr() { }

    public XmlElemJsonAttr(String xmlElemJsonAttr, String defaultElem) {
        setXmlElemJsonAttr(xmlElemJsonAttr);
        setDefaultElem(defaultElem);
    }

    public String getXmlElemJsonAttr() { return xmlElemJsonAttr; }
    public void setXmlElemJsonAttr(String xmlElemJsonAttr) { this.xmlElemJsonAttr = xmlElemJsonAttr; }

    public String getDefaultElem() { return defaultElem; }
    public void setDefaultElem(String defaultElem) { this.defaultElem = defaultElem; }
}
