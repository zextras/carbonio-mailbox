// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.PurgeThread;
import java.util.Map;

/**
 * Starts the mailbox purge thread if it is not running and the purge sleep interval is set to a
 * non-zero value.
 */
public class MailboxPurge extends AttributeCallback {

  @Override
  public void preModify(
      CallbackContext context, String attrName, Object attrValue, Map attrsToModify, Entry entry) {}

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {
    Server localServer =
        CallbackUtil.verificationBeforeStartingThread(
            Provisioning.A_zimbraMailPurgeSleepInterval, attrName, entry, "Mailbox Purge");
    if (localServer == null) {
      return;
    }

    ZimbraLog.purge.info(
        "Mailbox purge interval set to %s.",
        localServer.getAttr(Provisioning.A_zimbraMailPurgeSleepInterval, null));
    long interval = localServer.getTimeInterval(Provisioning.A_zimbraMailPurgeSleepInterval, 0);
    if (interval > 0 && !PurgeThread.isRunning()) {
      PurgeThread.startup();
    }
    if (interval == 0 && PurgeThread.isRunning()) {
      PurgeThread.shutdown();
    }
  }
}
