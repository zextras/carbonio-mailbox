// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import com.google.common.collect.Lists;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Test JAXB class to exercise a field annotated with {@link XmlAnyElement} */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "any-elem-tester")
public class AnyTester {
  @XmlElement private String given;

  @XmlAnyElement private List<org.w3c.dom.Element> elems = Lists.newArrayList();

  public AnyTester() {}

  public List<org.w3c.dom.Element> getElems() {
    return elems;
  }

  public void setElems(List<org.w3c.dom.Element> elems) {
    this.elems = elems;
  }

  public String getGiven() {
    return given;
  }

  public void setGiven(String given) {
    this.given = given;
  }
}
