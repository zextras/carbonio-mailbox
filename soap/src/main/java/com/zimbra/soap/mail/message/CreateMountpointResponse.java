// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.Mountpoint;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_CREATE_MOUNTPOINT_RESPONSE)
public class CreateMountpointResponse {

    /**
     * @zm-api-field-tag link
     * @zm-api-field-description Details of the created mountpoint
     */
    @XmlElement(name=MailConstants.E_MOUNT /* link */, required=true)
    private Mountpoint mount;

    public CreateMountpointResponse() {
    }

    public void setMount(Mountpoint mount) { this.mount = mount; }
    public Mountpoint getMount() { return mount; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("mount", mount);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
