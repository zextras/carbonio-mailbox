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

import com.zimbra.common.soap.SyncAdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=SyncAdminConstants.E_GET_DEVICES_COUNT_USED_TODAY_RESPONSE)
public class GetDevicesCountUsedTodayResponse {

    /**
     * @zm-api-field-tag num-devices-used-today
     * @zm-api-field-description Number of mobile devices on the server used today
     */
    @XmlAttribute(name=SyncAdminConstants.A_COUNT /* count */, required=true)
    private final int count;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetDevicesCountUsedTodayResponse() {
        this(-1);
    }

    public GetDevicesCountUsedTodayResponse(int count) {
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
