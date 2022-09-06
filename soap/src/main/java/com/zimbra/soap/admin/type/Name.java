// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class Name {

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Name
   */
  @XmlAttribute(name = BackupConstants.A_NAME /* name */, required = true)
  private final String name;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private Name() {
    this((String) null);
  }

  public Name(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("name", name);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
