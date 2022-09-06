// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class IntIdAttr {

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = AdminConstants.A_ID /* id */, required = true)
  private final int id;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private IntIdAttr() {
    this(-1);
  }

  public IntIdAttr(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
