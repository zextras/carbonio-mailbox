// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.VoiceAdminConstants;
import com.zimbra.soap.admin.type.VoiceProviderInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceAdminConstants.E_GET_ALL_UC_PROVIDERS_RESPONSE)
public class GetAllUCProvidersResponse {

    /**
     * @zm-api-field-description Details for a UC provider
     */
    @XmlElement(name=VoiceAdminConstants.E_PROVIDER, required=false)
    private List<VoiceProviderInfo> providers = Lists.newArrayList();

    public GetAllUCProvidersResponse() {
    }

    public void setProviders(Iterable <VoiceProviderInfo> providers) {
        this.providers.clear();
        if (providers == null) {
            this.providers = null;
        } else {
            this.providers = Lists.newArrayList();
            Iterables.addAll(this.providers, providers);
        }
    }

    public void addProvider(VoiceProviderInfo provider) {
        this.providers.add(provider);
    }

    public List<VoiceProviderInfo> getProviders() {
        return providers;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("providers", providers);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
