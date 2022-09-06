// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.type.NamedElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Register a device
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusXmlConstants.E_REGISTER_DEVICE_REQUEST)
public class RegisterDeviceRequest {

  /**
   * @zm-api-field-description Specify the device
   */
  @XmlElement(name = MailConstants.E_DEVICE /* device */, required = true)
  private final NamedElement device;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RegisterDeviceRequest() {
    this((NamedElement) null);
  }

  public RegisterDeviceRequest(NamedElement device) {
    this.device = device;
  }

  public NamedElement getDevice() {
    return device;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("device", device);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
