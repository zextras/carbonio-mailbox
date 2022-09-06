// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Check Domain MX record
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHECK_DOMAIN_MX_RECORD_REQUEST)
public class CheckDomainMXRecordRequest {

  /**
   * @zm-api-field-description Domain
   */
  @XmlElement(name = AdminConstants.E_DOMAIN, required = false)
  private final DomainSelector domain;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CheckDomainMXRecordRequest() {
    this((DomainSelector) null);
  }

  public CheckDomainMXRecordRequest(DomainSelector domain) {
    this.domain = domain;
  }

  public DomainSelector getDomain() {
    return domain;
  }
}
