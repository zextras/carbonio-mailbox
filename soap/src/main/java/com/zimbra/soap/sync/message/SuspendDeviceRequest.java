// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.sync.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.SyncConstants;
import com.zimbra.soap.sync.type.DeviceId;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Suspend a device from further sync actions
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = SyncConstants.E_SUSPEND_DEVICE_REQUEST)
public class SuspendDeviceRequest {

  /**
   * @zm-api-field-description Device selector
   */
  @XmlElement(name = SyncConstants.E_DEVICE /* device */, required = true)
  private final DeviceId device;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SuspendDeviceRequest() {
    this((DeviceId) null);
  }

  public SuspendDeviceRequest(DeviceId device) {
    this.device = device;
  }

  public DeviceId getDevice() {
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
