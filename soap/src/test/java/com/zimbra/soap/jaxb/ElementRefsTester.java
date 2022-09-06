// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import com.google.common.collect.Lists;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Test JAXB class to exercise a field annotated with {@link XmlElementRefs} and {@link
 * XmlElementRef}
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "elem-ref-tester")
public class ElementRefsTester {
  @XmlElementRefs({
    @XmlElementRef(
        type = StringAttribIntValue.class), /* note: name not supplied as would be ignored anyway */
    @XmlElementRef(type = EnumAttribs.class)
  })
  private List<Object> elems = Lists.newArrayList();

  public ElementRefsTester() {}

  public List<Object> getElems() {
    return elems;
  }

  public void setElems(List<Object> elems) {
    this.elems = elems;
  }
}
