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
public class DomainAggregateQuotaInfo {

  /**
   * @zm-api-field-tag domain-name
   * @zm-api-field-description Domain name
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = true)
  private final String name;
  /**
   * @zm-api-field-tag domain-id
   * @zm-api-field-description Domain name
   */
  @XmlAttribute(name = AdminConstants.A_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-tag quota-used-on-server
   * @zm-api-field-description Quota used on server
   */
  @XmlAttribute(name = AdminConstants.A_QUOTA_USED, required = true)
  private final long quotaUsed;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DomainAggregateQuotaInfo() {
    this(null, null, -1L);
  }

  public DomainAggregateQuotaInfo(String name, String id, long quotaUsed) {
    this.name = name;
    this.id = id;
    this.quotaUsed = quotaUsed;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public long getQuotaUsed() {
    return quotaUsed;
  }
}
