// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Returns custom loggers created for the given account since the last
 *     server start. If the request is sent to a server other than the one that the account resides
 *     on, it is proxied to the correct server.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ACCOUNT_LOGGERS_REQUEST)
public class GetAccountLoggersRequest {

  /**
   * @zm-api-field-description Deprecated - use account instead
   */
  @XmlElement(name = AdminConstants.E_ID /* id */, required = false)
  @Deprecated
  private String id;

  /**
   * @zm-api-field-description Use to select account
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT /* account */, required = false)
  private AccountSelector account;

  public GetAccountLoggersRequest() {
    this((AccountSelector) null);
  }

  public GetAccountLoggersRequest(AccountSelector account) {
    this.account = account;
  }

  @Deprecated
  public void setId(String id) {
    this.id = id;
  }

  public void setAccount(AccountSelector account) {
    this.account = account;
  }

  @Deprecated
  public String getId() {
    return id;
  }

  public AccountSelector getAccount() {
    return account;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("account", account);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
