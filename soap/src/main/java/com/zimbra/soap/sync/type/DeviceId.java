// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.sync.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.SyncConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class DeviceId {

    /**
     * @zm-api-field-tag device-id
     * @zm-api-field-description device ID
     */
    @XmlAttribute(name=SyncConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DeviceId() {
        this((String) null);
    }

    public DeviceId(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper.add("id", id);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
