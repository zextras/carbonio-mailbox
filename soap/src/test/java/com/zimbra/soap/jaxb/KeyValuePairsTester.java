// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import com.zimbra.common.soap.Element;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;
import com.zimbra.soap.type.KeyValuePair;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Test {@link ZimbraKeyValuePairs} annotation */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "key-value-pairs-tester")
public class KeyValuePairsTester {
  @XmlElement(name = Element.XMLElement.E_ATTRIBUTE /* a */)
  @ZimbraKeyValuePairs
  private List<KeyValuePair> attrList;

  public KeyValuePairsTester() {}

  public KeyValuePairsTester(List<KeyValuePair> attrs) {
    setAttrList(attrs);
  }

  public List<KeyValuePair> getAttrList() {
    return attrList;
  }

  public void setAttrList(List<KeyValuePair> attrList) {
    this.attrList = attrList;
  }
}
