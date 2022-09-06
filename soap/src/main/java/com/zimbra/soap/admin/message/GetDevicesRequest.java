// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.OctopusAdminConstants;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get devices
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusAdminConstants.E_GET_DEVICES_REQUEST)
public class GetDevicesRequest {

  /**
   * @zm-api-field-description Account
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT /* account */, required = true)
  private final AccountSelector account;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetDevicesRequest() {
    this((AccountSelector) null);
  }

  public GetDevicesRequest(AccountSelector account) {
    this.account = account;
  }

  public AccountSelector getAccount() {
    return account;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("account", account);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
