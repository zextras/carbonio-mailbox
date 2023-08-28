// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Invalidate reminder device
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_INVALIDATE_REMINDER_DEVICE_REQUEST)
public class InvalidateReminderDeviceRequest {

    /**
     * @zm-api-field-tag device-email-address
     * @zm-api-field-description Device email address
     */
    @XmlAttribute(name=MailConstants.A_ADDRESS /* a */, required=true)
    private String address;

    public InvalidateReminderDeviceRequest() {
    }

    public void setAddress(String address) { this.address = address; }
    public String getAddress() { return address; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("address", address);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
