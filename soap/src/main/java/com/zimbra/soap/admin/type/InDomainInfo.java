// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.NamedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class InDomainInfo {

  /**
   * @zm-api-field-description Domains
   */
  @XmlElement(name = AdminConstants.E_DOMAIN, required = false)
  private List<NamedElement> domains = Lists.newArrayList();

  /**
   * @zm-api-field-description Rights
   */
  @XmlElement(name = AdminConstants.E_RIGHTS, required = true)
  private final EffectiveRightsInfo rights;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private InDomainInfo() {
    this(null, null);
  }

  public InDomainInfo(EffectiveRightsInfo rights) {
    this(null, rights);
  }

  public InDomainInfo(Collection<NamedElement> domains, EffectiveRightsInfo rights) {
    this.rights = rights;
    setDomains(domains);
  }

  public InDomainInfo setDomains(Collection<NamedElement> domains) {
    this.domains.clear();
    if (domains != null) {
      this.domains.addAll(domains);
    }
    return this;
  }

  public InDomainInfo addDomain(NamedElement domain) {
    domains.add(domain);
    return this;
  }

  public List<NamedElement> getDomains() {
    return Collections.unmodifiableList(domains);
  }

  public EffectiveRightsInfo getRights() {
    return rights;
  }
}
