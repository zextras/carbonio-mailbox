// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/** Test JAXB class with a String XmlAttribute and int XmlValue */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "string-attr-int-value")
public class StringAttribIntValue {
  @XmlAttribute(name = "attr1", required = true)
  private String attrib1;

  @XmlValue() private int myValue;

  public StringAttribIntValue() {}

  public StringAttribIntValue(String a, int v) {
    setAttrib1(a);
    setMyValue(v);
  }

  public String getAttrib1() {
    return attrib1;
  }

  public void setAttrib1(String attrib1) {
    this.attrib1 = attrib1;
  }

  public int getMyValue() {
    return myValue;
  }

  public void setMyValue(int myValue) {
    this.myValue = myValue;
  }
}
