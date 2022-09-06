// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.Name;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Returns the version info for a mailbox. <br>
 *     Mailbox move uses this request to prevent a move to an older server.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = BackupConstants.E_GET_MAILBOX_VERSION_REQUEST)
public class GetMailboxVersionRequest {

  /**
   * @zm-api-field-tag account-email-address
   * @zm-api-field-description Account email address
   */
  @XmlElement(name = BackupConstants.E_ACCOUNT /* account */, required = true)
  private Name account;

  private GetMailboxVersionRequest() {}

  private GetMailboxVersionRequest(Name account) {
    setAccount(account);
  }

  public static GetMailboxVersionRequest create(Name account) {
    return new GetMailboxVersionRequest(account);
  }

  public void setAccount(Name account) {
    this.account = account;
  }

  public Name getAccount() {
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
