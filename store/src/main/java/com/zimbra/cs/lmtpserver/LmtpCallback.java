// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;

public interface LmtpCallback {

  /** Called after the message is delivered to the given account. */
  public void afterDelivery(
      Account account,
      Mailbox mbox,
      String envelopeSender,
      String recipientEmail,
      Message newMessage);

  /** Called when mail forwarding is set up for the account but delivery to mailbox is disabled. */
  public void forwardWithoutDelivery(
      Account account,
      Mailbox mbox,
      String envelopeSender,
      String recipientEmail,
      ParsedMessage pm);
}
