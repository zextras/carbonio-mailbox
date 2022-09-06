// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainInfo;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = AdminConstants.E_GET_ALL_DOMAINS_RESPONSE)
public class GetAllDomainsResponse {

  /**
   * @zm-api-field-description Information on domains
   */
  @XmlElement(name = AdminConstants.E_DOMAIN)
  private List<DomainInfo> domainList = new ArrayList<DomainInfo>();

  public GetAllDomainsResponse() {}

  public void addDomain(DomainInfo domain) {
    this.getDomainList().add(domain);
  }

  public List<DomainInfo> getDomainList() {
    return domainList;
  }
}
