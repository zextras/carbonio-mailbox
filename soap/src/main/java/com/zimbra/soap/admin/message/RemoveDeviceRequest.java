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
 * @zm-api-command-description Remove a device or remove all devices attached to an account. This
 *     will not cause a reset of sync data, but will cause a reset of policies on the next sync.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = SyncAdminConstants.E_REMOVE_DEVICE_REQUEST)
public class RemoveDeviceRequest {

  /**
   * @zm-api-field-description Account Selector
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT, required = true)
  private AccountSelector account;

  /**
   * @zm-api-field-description Device specification - Note - if not supplied ALL devices will be
   *     removed.
   */
  @XmlElement(name = SyncConstants.E_DEVICE, required = false)
  private DeviceId deviceId;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RemoveDeviceRequest() {}

  public RemoveDeviceRequest(AccountSelector account) {
    this.account = account;
  }

  public DeviceId getDeviceId() {
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
