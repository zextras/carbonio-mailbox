// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = AdminConstants.E_GET_DOMAIN_RESPONSE)
public class GetDomainResponse {

  /**
   * @zm-api-field-description Information about domain
   */
  @XmlElement(name = AdminConstants.E_DOMAIN)
  private final DomainInfo domain;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetDomainResponse() {
    this(null);
  }

  public GetDomainResponse(DomainInfo domain) {
    this.domain = domain;
  }

  public DomainInfo getDomain() {
    return domain;
  }
}
