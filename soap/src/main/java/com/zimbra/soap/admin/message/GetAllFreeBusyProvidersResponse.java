// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.FreeBusyProviderInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_FREE_BUSY_PROVIDERS_RESPONSE)
public class GetAllFreeBusyProvidersResponse {

  /**
   * @zm-api-field-description Information on Free/Busy providers
   */
  @XmlElement(name = AdminConstants.E_PROVIDER, required = false)
  private List<FreeBusyProviderInfo> providers = Lists.newArrayList();

  public GetAllFreeBusyProvidersResponse() {}

  public void setProviders(Iterable<FreeBusyProviderInfo> providers) {
    this.providers.clear();
    if (providers != null) {
      Iterables.addAll(this.providers, providers);
    }
  }

  public GetAllFreeBusyProvidersResponse addProvider(FreeBusyProviderInfo provider) {
    this.providers.add(provider);
    return this;
  }

  public List<FreeBusyProviderInfo> getProviders() {
    return Collections.unmodifiableList(providers);
  }
}
