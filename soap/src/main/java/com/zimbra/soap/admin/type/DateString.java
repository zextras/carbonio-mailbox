// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.SyncAdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class DateString {

  /**
   * @zm-api-field-tag yyyy-MM-dd
   * @zm-api-field-description Date in format : <b>yyyy-MM-dd</b>
   */
  @XmlAttribute(name = SyncAdminConstants.A_DATE /* date */, required = true)
  private final String date;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DateString() {
    this((String) null);
  }

  public DateString(String date) {
    this.date = date;
  }

  public String getDate() {
    return date;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("date", date);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
