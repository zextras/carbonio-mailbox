// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Change Account
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHANGE_PRIMARY_EMAIL_REQUEST)
@XmlType(propOrder = {})
public class ChangePrimaryEmailRequest {

  /**
   * @zm-api-field-description Specifies the account to be changed
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT, required = true)
  private AccountSelector account;

  /**
   * @zm-api-field-tag new-account-name
   * @zm-api-field-description New account name
   */
  @XmlElement(name = AdminConstants.E_NEW_NAME, required = true)
  private final String newName;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ChangePrimaryEmailRequest() {
    this(null, null);
  }

  public ChangePrimaryEmailRequest(AccountSelector account, String newName) {
    this.account = account;
    this.newName = newName;
  }

  public AccountSelector getAccount() {
    return account;
  }

  public String getNewName() {
    return newName;
  }
}
