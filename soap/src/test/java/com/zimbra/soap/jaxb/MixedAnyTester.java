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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import org.w3c.dom.Element;

/**
 * Test JAXB class to exercise a field annotated with {@link XmlAnyElement}, {@link XmlMixed} and
 * {@link XmlElementRefs} and {@link XmlElementRef}
 *
 * <p>{@link XmlAnyElement} means that some of the objects in the list can be {@link Element}
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "mixed-tester")
public class MixedAnyTester {
  @XmlElementRefs({
    @XmlElementRef(
        type =
            StringAttribIntValue.class) /* note: tests out case where name isn't specified here */
  })
  @XmlAnyElement
  @XmlMixed
  private List<Object> elems = Lists.newArrayList();

  public MixedAnyTester() {}

  public List<Object> getElems() {
    return elems;
  }

  public void setElems(List<Object> elems) {
    this.elems = elems;
  }
}
