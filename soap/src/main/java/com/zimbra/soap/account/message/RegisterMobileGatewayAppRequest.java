// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.ZmgDeviceSpec;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Registering app/device to receive push notifications
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_REGISTER_MOBILE_GATEWAY_APP_REQUEST)
public class RegisterMobileGatewayAppRequest {

  /**
   * @zm-api-field-description Zmg Device specification
   */
  @ZimbraUniqueElement
  @XmlElement(name = AccountConstants.E_ZMG_DEVICE /* m */, required = true)
  private final ZmgDeviceSpec zmgDevice;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RegisterMobileGatewayAppRequest() {
    this((ZmgDeviceSpec) null);
  }

  public RegisterMobileGatewayAppRequest(ZmgDeviceSpec zmgDevice) {
    this.zmgDevice = zmgDevice;
  }

  public ZmgDeviceSpec getZmgDevice() {
    return zmgDevice;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("zmgDevice", zmgDevice);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
