// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlRootElement(name=AccountConstants.E_GET_TRUSTED_DEVICES_RESPONSE)
public class GetTrustedDevicesResponse {

    public GetTrustedDevicesResponse() {}

    @XmlAttribute(name=AccountConstants.A_NUM_OTHER_TRUSTED_DEVICES)
    private Integer numOtherTrustedDevices;

    @XmlAttribute(name=AccountConstants.A_THIS_DEVICE_TRUSTED)
    private ZmBoolean thisDeviceTrusted;

    public void setNumOtherTrustedDevices(Integer n) { numOtherTrustedDevices = n; }
    public Integer getNumOtherTrustedDevices() { return numOtherTrustedDevices; }

    public void setThisDeviceTrusted(Boolean b) { thisDeviceTrusted = ZmBoolean.fromBool(b); }
    public Boolean getThisDeviceTrusted() { return ZmBoolean.toBool(thisDeviceTrusted); }

}
