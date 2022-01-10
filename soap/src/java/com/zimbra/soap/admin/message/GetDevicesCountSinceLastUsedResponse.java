// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.SyncAdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=SyncAdminConstants.E_GET_DEVICES_COUNT_SINCE_LAST_USED_RESPONSE)
public class GetDevicesCountSinceLastUsedResponse {

    /**
     * @zm-api-field-tag number-of-devices
     * @zm-api-field-description Number of mobile devices on the server since the specified last used date
     */
    @XmlAttribute(name=SyncAdminConstants.A_COUNT /* count */, required=true)
    private final int count;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetDevicesCountSinceLastUsedResponse() {
        this(-1);
    }

    public GetDevicesCountSinceLastUsedResponse(int count) {
        this.count = count;
    }

    public int getCount() { return count; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("count", count);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
