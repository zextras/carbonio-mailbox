// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import java.io.IOException;

public class RecoverItem extends CopyItem {

  public RecoverItem() {
    super();
    mOperation = MailboxOperation.RecoverItem;
    setFromDumpster(true);
  }

  public RecoverItem(int mailboxId, MailItem.Type type, int folderId) {
    super(mailboxId, type, folderId);
    mOperation = MailboxOperation.RecoverItem;
    setFromDumpster(true);
  }

  @Override
  protected void deserializeData(RedoLogInput in) throws IOException {
    super.deserializeData(in);
    setFromDumpster(true); // shouldn't be necessary, but let's be absolutely sure
  }
}
