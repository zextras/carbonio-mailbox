// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class TimeZoneInfo {

  /**
   * @zm-api-field-tag timezone-id
   * @zm-api-field-description timezone ID. e.g "America/Los_Angeles"
   */
  @XmlAttribute(name = AdminConstants.A_TIMEZONE_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-tag timezone-display-name
   * @zm-api-field-description Timezone display anme, e.g. "Pacific Standard Time"
   */
  @XmlAttribute(name = AdminConstants.A_TIMEZONE_DISPLAYNAME /* displayName */, required = true)
  private final String displayName;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private TimeZoneInfo() {
    this(null, null);
  }

  public TimeZoneInfo(String id, String displayName) {
    this.displayName = displayName;
    this.id = id;
  }

  public static TimeZoneInfo fromIdAndDisplayName(String id, String displayName) {
    return new TimeZoneInfo(id, displayName);
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getId() {
    return id;
  }
}
