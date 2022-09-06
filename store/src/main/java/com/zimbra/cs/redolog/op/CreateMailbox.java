// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 11. 2.
 */
package com.zimbra.cs.redolog.op;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.MailboxIdConflictException;
import com.zimbra.cs.redolog.RedoException;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

public class CreateMailbox extends RedoableOp {

  private String mAccountId;

  public CreateMailbox() {
    super(MailboxOperation.CreateMailbox);
  }

  public CreateMailbox(String accountId) {
    this();
    mAccountId = accountId;
  }

  @Override
  protected String getPrintableData() {
    StringBuffer sb = new StringBuffer("account=").append(mAccountId != null ? mAccountId : "");
    return sb.toString();
  }

  @Override
  protected void serializeData(RedoLogOutput out) throws IOException {
    out.writeUTF(mAccountId);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    mAccountId = in.readUTF();
  }

  @Override
  public void redo() throws Exception {
    int opMboxId = getMailboxId();
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(mAccountId, false);

    if (mbox == null) {
      Account account = Provisioning.getInstance().get(AccountBy.id, mAccountId);
      if (account == null) {
        throw new RedoException("Account " + mAccountId + " does not exist", this);
      }

      mbox = MailboxManager.getInstance().createMailbox(getOperationContext(), account);
      if (mbox == null) {
        // something went really wrong
        throw new RedoException("unable to create mailbox for accountId " + mAccountId, this);
      }
    }

    int mboxId = mbox.getId();
    if (opMboxId == mboxId) {
      mLog.info("Mailbox " + opMboxId + " for account " + mAccountId + " already exists");
      return;
    } else {
      throw new MailboxIdConflictException(mAccountId, opMboxId, mboxId, this);
    }
  }
}
