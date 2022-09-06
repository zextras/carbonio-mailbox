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
public class AccountQuotaInfo {

  /**
   * @zm-api-field-tag account-name
   * @zm-api-field-description Account name
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = true)
  private final String name;

  /**
   * @zm-api-field-tag account-id
   * @zm-api-field-description Account ID
   */
  @XmlAttribute(name = AdminConstants.A_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-tag quota-used-bytes
   * @zm-api-field-description Used quota in bytes, or 0 if no quota used
   */
  @XmlAttribute(name = AdminConstants.A_QUOTA_USED /* used */, required = true)
  private final long quotaUsed;

  /**
   * @zm-api-field-tag quota-limit-bytes
   * @zm-api-field-description Quota limit in bytes, or 0 if unlimited
   */
  @XmlAttribute(name = AdminConstants.A_QUOTA_LIMIT /* limit */, required = true)
  private final long quotaLimit;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private AccountQuotaInfo() {
    this((String) null, (String) null, -1L, -1L);
  }

  public AccountQuotaInfo(String name, String id, long quotaUsed, long quotaLimit) {
    this.name = name;
    this.id = id;
    this.quotaUsed = quotaUsed;
    this.quotaLimit = quotaLimit;
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

  public long getQuotaLimit() {
    return quotaLimit;
  }
}
