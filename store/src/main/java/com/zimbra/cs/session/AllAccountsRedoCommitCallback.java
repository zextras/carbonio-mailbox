// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.session;

import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.redolog.CommitId;
import com.zimbra.cs.redolog.RedoCommitCallback;
import java.util.Set;

/**
 * @deprecated AllAccountsWaitSet is being deprecated
 */
public class AllAccountsRedoCommitCallback implements RedoCommitCallback {
  private final String accountId;
  private final Set<MailItem.Type> changeTypes;

  private AllAccountsRedoCommitCallback(String accountId, Set<MailItem.Type> types) {
    this.accountId = accountId;
    changeTypes = types;
  }

  @Override
  public void callback(CommitId cid) {
    AllAccountsWaitSet.mailboxChangeCommitted(cid.encodeToString(), accountId, changeTypes);
  }

  public static final AllAccountsRedoCommitCallback getRedoCallbackIfNecessary(
      String accountId, Set<MailItem.Type> types) {
    if (AllAccountsWaitSet.isCallbackNecessary(types)) {
      return new AllAccountsRedoCommitCallback(accountId, types);
    }
    return null;
  }
}
