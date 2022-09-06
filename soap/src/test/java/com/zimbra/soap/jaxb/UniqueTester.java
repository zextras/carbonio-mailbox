// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Test JAXB class with a variety of XmlElements which should be treated as unique or normally */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "unique-tester")
public class UniqueTester {
  @ZimbraUniqueElement
  @XmlElement(name = "unique-str-elem", namespace = "urn:zimbraTest1", required = false)
  private String uniqueStrElem;

  @XmlElement(name = "non-unique-elem", namespace = "urn:zimbraTest1", required = false)
  private String nonUniqueStrElem;

  @ZimbraUniqueElement
  @XmlElement(name = "unique-complex-elem", required = false)
  private StringAttribIntValue uniqueComplexElem;

  public UniqueTester() {}

  public String getUniqueStrElem() {
    return uniqueStrElem;
  }

  public void setUniqueStrElem(String uniqueStrElem) {
    this.uniqueStrElem = uniqueStrElem;
  }

  public String getNonUniqueStrElem() {
    return nonUniqueStrElem;
  }

  public void setNonUniqueStrElem(String nonUniqueStrElem) {
    this.nonUniqueStrElem = nonUniqueStrElem;
  }

  public StringAttribIntValue getUniqueComplexElem() {
    return uniqueComplexElem;
  }

  public void setUniqueComplexElem(StringAttribIntValue uniqueComplexElem) {
    this.uniqueComplexElem = uniqueComplexElem;
  }
}
