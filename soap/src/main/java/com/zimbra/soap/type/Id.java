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
public class Id {

  /**
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = AdminConstants.A_ID, required = false)
  private final String id;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  protected Id() {
    this((String) null);
  }

  public Id(String id) {
    this.id = id;
  }

  public Id(Integer id) {
    this(id.toString());
  }

  public String getId() {
    return id;
  }
}
