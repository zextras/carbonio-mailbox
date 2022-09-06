// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.MailboxVersionInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = BackupConstants.E_GET_MAILBOX_VERSION_RESPONSE)
@XmlType(propOrder = {})
public class GetMailboxVersionResponse {

  /**
   * @zm-api-field-description Mailbox Version information
   */
  @XmlElement(name = BackupConstants.E_ACCOUNT /* account */, required = true)
  private MailboxVersionInfo account;

  private GetMailboxVersionResponse() {}

  private GetMailboxVersionResponse(MailboxVersionInfo account) {
    setAccount(account);
  }

  public static GetMailboxVersionResponse create(MailboxVersionInfo account) {
    return new GetMailboxVersionResponse(account);
  }

  public void setAccount(MailboxVersionInfo account) {
    this.account = account;
  }

  public MailboxVersionInfo getAccount() {
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
