// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Test JAXB class with a String XmlAttribute and a String XmlElement */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "string-attr-string-elem", namespace = "urn:ZimbraTest2")
public class StringAttrStringElem {
  @XmlAttribute(name = "attribute-1", required = true)
  private String attr1;

  @XmlElement(name = "element1", namespace = "urn:ZimbraTest3", required = true)
  private String elem1;

  public StringAttrStringElem() {}

  public String getAttr1() {
    return attr1;
  }

  public void setAttr1(String attr1) {
    this.attr1 = attr1;
  }

  public String getElem1() {
    return elem1;
  }

  public void setElem1(String elem1) {
    this.elem1 = elem1;
  }
}
