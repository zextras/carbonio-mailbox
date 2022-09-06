// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.SyncAdminConstants;
import com.zimbra.common.soap.SyncConstants;
import com.zimbra.soap.admin.type.DeviceId;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Resume sync with a device or all devices attached to an account if
 *     currently suspended. This will cause a policy reset, but will not reset sync data.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = SyncAdminConstants.E_RESUME_DEVICE_REQUEST)
public class ResumeDeviceRequest {

  /**
   * @zm-api-field-description Account selector
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT, required = true)
  private AccountSelector account;

  /**
   * @zm-api-field-tag device-id
   * @zm-api-field-description Device ID
   */
  @XmlElement(name = SyncConstants.E_DEVICE, required = false)
  private DeviceId deviceId;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ResumeDeviceRequest() {}

  public ResumeDeviceRequest(AccountSelector account) {
    this.account = account;
  }

  public DeviceId getDevice() {
    return this.deviceId;
  }

  public void setDeviceId(DeviceId deviceId) {
    this.deviceId = deviceId;
  }

  public AccountSelector getAccount() {
    return this.account;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("account", this.account)
        .add("device", this.deviceId)
        .toString();
  }
}
