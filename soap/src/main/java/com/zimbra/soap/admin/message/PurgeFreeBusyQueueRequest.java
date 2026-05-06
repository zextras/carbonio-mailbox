// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.NamedElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Purges the queue for the given freebusy provider on the current host
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_PURGE_FREE_BUSY_QUEUE_REQUEST)
public class PurgeFreeBusyQueueRequest {

    /**
     * @zm-api-field-description FreeBusy Provider specification
     */
    @XmlElement(name=AdminConstants.E_PROVIDER, required=false)
    private NamedElement provider;

    public PurgeFreeBusyQueueRequest() {
    }

    public void setProvider(NamedElement provider) {
        this.provider = provider;
    }

    public NamedElement getProvider() { return provider; }
}
