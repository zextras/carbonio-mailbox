// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainAggregateQuotaInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_AGGR_QUOTA_USAGE_ON_SERVER_RESPONSE)
public class GetAggregateQuotaUsageOnServerResponse {

  /**
   * @zm-api-field-description Aggregate quota information reported per domain
   */
  @XmlElement(name = AdminConstants.E_DOMAIN /* domain */, required = false)
  private List<DomainAggregateQuotaInfo> domainQuotas = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetAggregateQuotaUsageOnServerResponse() {
    this(null);
  }

  public GetAggregateQuotaUsageOnServerResponse(Collection<DomainAggregateQuotaInfo> domainQuotas) {
    setDomainQuotas(domainQuotas);
  }

  public GetAggregateQuotaUsageOnServerResponse setDomainQuotas(
      Collection<DomainAggregateQuotaInfo> domainQuotas) {
    this.domainQuotas.clear();
    if (domainQuotas != null) {
      this.domainQuotas.addAll(domainQuotas);
    }
    return this;
  }

  public GetAggregateQuotaUsageOnServerResponse addDomainQuota(
      DomainAggregateQuotaInfo domainQuota) {
    domainQuotas.add(domainQuota);
    return this;
  }

  public List<DomainAggregateQuotaInfo> getDomainQuotas() {
    return Collections.unmodifiableList(domainQuotas);
  }
}
