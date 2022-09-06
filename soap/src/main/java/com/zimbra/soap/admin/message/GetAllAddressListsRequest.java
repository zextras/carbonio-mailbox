// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainSelector;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-description Returns all address lists which are there in the user's domain
 */
@XmlRootElement(name = AdminConstants.E_GET_ALL_ADDRESS_LISTS_REQUEST)
public class GetAllAddressListsRequest {
  /**
   * @zm-api-field-description Domain
   */
  @XmlElement(name = AdminConstants.E_DOMAIN, required = false)
  private final DomainSelector domain;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetAllAddressListsRequest() {
    this((DomainSelector) null);
  }

  public GetAllAddressListsRequest(DomainSelector domain) {
    this.domain = domain;
  }

  public DomainSelector getDomain() {
    return domain;
  }
}
