// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.SyncAdminConstants;

@XmlRootElement(name=SyncAdminConstants.E_REMOVE_STALE_DEVICE_METADATA_REQUEST)
public class RemoveStaleDeviceMetadataRequest {

    @XmlAttribute(name=SyncAdminConstants.A_LAST_USED_DATE_OLDER_THAN, required=false)
    private int lastUsedDateOlderThan;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private RemoveStaleDeviceMetadataRequest() {
    }

    private RemoveStaleDeviceMetadataRequest(int days) {
        this.lastUsedDateOlderThan = days;
    }

    public int getLastUsedDateOlderThan() {
        return lastUsedDateOlderThan;
    }

    public void setLastUsedDateOlderThan(int lastUsedDateOlderThan) {
        this.lastUsedDateOlderThan = lastUsedDateOlderThan;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("lastUsedDateOlderThan", this.lastUsedDateOlderThan).toString();
    }
}
