// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.mail.type.NameId;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=OctopusXmlConstants.E_REGISTER_DEVICE_RESPONSE)
public class RegisterDeviceResponse {

    /**
     * @zm-api-field-description Information on the registered device
     */
    @XmlElement(name=MailConstants.E_DEVICE /* device */, required=true)
    private final NameId device;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private RegisterDeviceResponse() {
        this((NameId) null);
    }

    public RegisterDeviceResponse(NameId device) {
        this.device = device;
    }

    public static RegisterDeviceResponse fromNameAndId(String name, String id) {
        NameId dev = new NameId(name, id);
        return new RegisterDeviceResponse(dev);
    }

    public NameId getDevice() { return device; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("device", device);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
