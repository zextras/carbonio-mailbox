// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.MailboxMoveSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Register Mailbox move out. <br>
 *     This request is invoked by move destination server against move source server to signal the
 *     start of a mailbox move. The receiving server registers a move-out. This helps prevent
 *     simultaneous moves of the same mailbox. <br>
 *     <br>
 *     ALREADY_BEING_MOVED_OUT fault will be returned if mailbox is already in the middle of a move.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = BackupConstants.E_REGISTER_MAILBOX_MOVE_OUT_REQUEST)
public class RegisterMailboxMoveOutRequest {

  /**
   * @zm-api-field-description Specification for Mailbox move
   */
  @XmlElement(name = BackupConstants.E_ACCOUNT /* account */, required = true)
  private MailboxMoveSpec account;

  private RegisterMailboxMoveOutRequest() {}

  private RegisterMailboxMoveOutRequest(MailboxMoveSpec account) {
    setAccount(account);
  }

  public static RegisterMailboxMoveOutRequest create(MailboxMoveSpec account) {
    return new RegisterMailboxMoveOutRequest(account);
  }

  public void setAccount(MailboxMoveSpec account) {
    this.account = account;
  }

  public MailboxMoveSpec getAccount() {
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
