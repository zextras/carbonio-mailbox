// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/** Test JAXB class to exercise a field annotated with {@link XmlTransient} */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "transient-tester")
public class TransientTester {
  @XmlTransient private String toBeIgnored;

  @XmlAttribute(name = "attr", required = true)
  private Integer nummer;

  public TransientTester() {}

  public TransientTester(String str, Integer num) {
    setToBeIgnored(str);
    setNummer(num);
  }

  public String getToBeIgnored() {
    return toBeIgnored;
  }

  public void setToBeIgnored(String toBeIgnored) {
    this.toBeIgnored = toBeIgnored;
  }

  public Integer getNummer() {
    return nummer;
  }

  public void setNummer(Integer nummer) {
    this.nummer = nummer;
  }
}
