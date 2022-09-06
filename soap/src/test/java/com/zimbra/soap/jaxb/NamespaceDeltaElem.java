// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Test JAXB class for exercising changes in namespace associated with elements */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "ns-delta", namespace = "urn:ZimbraTest4")
public class NamespaceDeltaElem {
  @XmlElement(name = "strAttrStrElem", namespace = "urn:ZimbraTest5")
  private StringAttrStringElem sase;

  public NamespaceDeltaElem() {}

  public StringAttrStringElem getSase() {
    return sase;
  }

  public void setSase(StringAttrStringElem sase) {
    this.sase = sase;
  }
}
