// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.mail.type.IdStatus;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusXmlConstants.E_CHECK_DEVICE_STATUS_RESPONSE)
public class CheckDeviceStatusResponse {

  /**
   * @zm-api-field-description Information on device status
   */
  @XmlElement(name = MailConstants.E_DEVICE /* device */, required = true)
  private final IdStatus device;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CheckDeviceStatusResponse() {
    this((IdStatus) null);
  }

  public CheckDeviceStatusResponse(IdStatus device) {
    this.device = device;
  }

  public IdStatus getDevice() {
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
