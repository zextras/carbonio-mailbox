// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class NamedElement {

  /**
   * @zm-api-field-description Name
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = false)
  private final String name;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private NamedElement() {
    this(null);
  }

  public NamedElement(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
