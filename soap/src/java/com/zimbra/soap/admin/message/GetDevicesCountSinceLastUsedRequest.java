// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.SyncAdminConstants;
import com.zimbra.soap.admin.type.DateString;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get the mobile devices count on the server since last used date
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=SyncAdminConstants.E_GET_DEVICES_COUNT_SINCE_LAST_USED_REQUEST)
public class GetDevicesCountSinceLastUsedRequest {

    /**
     * @zm-api-field-description Last used date
     */
    @XmlElement(name=SyncAdminConstants.E_LAST_USED_DATE /* lastUsedDate */, required=true)
    private final DateString lastUsedDate;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetDevicesCountSinceLastUsedRequest() {
        this((DateString) null);
    }

    public GetDevicesCountSinceLastUsedRequest(DateString lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    public static GetDevicesCountSinceLastUsedRequest fromLastUsedDate(
                    String lastUsedDate) {
        DateString lud = new DateString(lastUsedDate);
        return new GetDevicesCountSinceLastUsedRequest(lud);
    }

    public DateString getLastUsedDate() { return lastUsedDate; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("lastUsedDate", lastUsedDate);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
