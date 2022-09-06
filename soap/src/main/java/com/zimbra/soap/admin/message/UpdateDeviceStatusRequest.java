// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusAdminConstants;
import com.zimbra.soap.mail.type.IdStatus;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Update device status
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusAdminConstants.E_UPDATE_DEVICE_STATUS_REQUEST)
public class UpdateDeviceStatusRequest {

  /**
   * @zm-api-field-description Account selector
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT /* account */, required = true)
  private final AccountSelector account;

  /**
   * @zm-api-field-description Information on new device status
   */
  @XmlElement(name = MailConstants.E_DEVICE /* device */, required = true)
  private final IdStatus device;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private UpdateDeviceStatusRequest() {
    this((AccountSelector) null, (IdStatus) null);
  }

  public UpdateDeviceStatusRequest(AccountSelector account, IdStatus device) {
    this.account = account;
    this.device = device;
  }

  public AccountSelector getAccount() {
    return account;
  }

  public IdStatus getDevice() {
    return device;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("account", account).add("device", device);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
