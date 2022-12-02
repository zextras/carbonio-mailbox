// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.FreeBusyQueueProvider;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_FREE_BUSY_QUEUE_INFO_RESPONSE)
public class GetFreeBusyQueueInfoResponse {

    /**
     * @zm-api-field-description Information on Free/Busy providers
     */
    @XmlElement(name=AdminConstants.E_PROVIDER, required=false)
    private List<FreeBusyQueueProvider> providers = Lists.newArrayList();

    public GetFreeBusyQueueInfoResponse() {
    }

    public void setProviders(Iterable <FreeBusyQueueProvider> providers) {
        this.providers.clear();
        if (providers != null) {
            Iterables.addAll(this.providers,providers);
        }
    }

    public GetFreeBusyQueueInfoResponse addProvider(
                    FreeBusyQueueProvider provider) {
        this.providers.add(provider);
        return this;
    }

    public List<FreeBusyQueueProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }
}
