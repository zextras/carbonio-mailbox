// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.MailboxVolumesInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = BackupConstants.E_GET_MAILBOX_VOLUMES_RESPONSE)
@XmlType(propOrder = {})
public class GetMailboxVolumesResponse {

  /**
   * @zm-api-field-description Mailbox Volume Information
   */
  @XmlElement(name = BackupConstants.E_ACCOUNT /* account */, required = true)
  private MailboxVolumesInfo account;

  private GetMailboxVolumesResponse() {}

  private GetMailboxVolumesResponse(MailboxVolumesInfo account) {
    setAccount(account);
  }

  public static GetMailboxVolumesResponse create(MailboxVolumesInfo account) {
    return new GetMailboxVolumesResponse(account);
  }

  public void setAccount(MailboxVolumesInfo account) {
    this.account = account;
  }

  public MailboxVolumesInfo getAccount() {
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
